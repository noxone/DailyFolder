package com.hlag.gwrp.work;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class DailyFolder extends Application {
	private static final String FOLDER_PATTERN_STRING = "^([0-9]{4})-([0-9]{2})-([0-9]{2})$";

	private static final Pattern FOLDER_PATTERN = Pattern.compile(FOLDER_PATTERN_STRING);

	private static final FileFilter DATE_FOLDER_FILTER = new DateFolderFilter();

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(final String[] args) {
		launch(args);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final Stage stage) throws Exception {
		doDailyFolderWork(stage);
	}

	private void doDailyFolderWork(final Stage stage) {
		final File desktopFolder = new File(WindowsUtils.getCurrentUserDesktopPath());

		Collection<File> foldersToDeleteEventually = new ArrayList<>();
		try {
			foldersToDeleteEventually = readFoldersToDeleteEventually(desktopFolder);
		} catch (final IOException e) {}
		final Collection<File> notDeletedFolders = removeEmptyDateFolders(desktopFolder);
		createTodaysFolder(desktopFolder);

		if (!notDeletedFolders.isEmpty()) {
			notDeletedFolders.removeAll(foldersToDeleteEventually);
			deleteFolders(foldersToDeleteEventually);
			boolean alwaysOnTop = true;
			final String ontop = getParameters().getNamed().get("alwaysOnTop");
			if (ontop != null) {
				alwaysOnTop = Boolean.parseBoolean(ontop);
			}
			DeleteFoldersDialogFx.showDialog(stage,
					notDeletedFolders,
					getHostServices(),
					folders -> deleteFolders(folders),
					alwaysOnTop);
		}
	}

	private void deleteFolders(final Collection<File> folders) {
		folders//
				.stream()
				.filter(f -> !deepDelete(f))
				.forEach(f -> System.out.println("Unable to delete: " + f.getAbsolutePath()));
	}

	protected Collection<File> readFoldersToDeleteEventually(final File root) throws IOException {
		final Properties p = new Properties();
		p.load(getClass().getResourceAsStream("folders.properties"));
		final List<String> filenames = new ArrayList<>();
		int i = 1;
		while (p.containsKey("fileToDelete." + i)) {
			filenames.add(p.getProperty("fileToDelete." + i));
			++i;
		}
		return filenames.stream().map(fn -> new File(root, fn)).filter(f -> f.exists()).collect(Collectors.toSet());
	}

	protected void createTodaysFolder(final File root) {
		final String name = DATE_FORMAT.format(new Date());
		final File today = new File(root, name);
		if (today.mkdirs()) {
			System.out.println("Created: " + today.getAbsolutePath());
		} else {
			System.out.println("Unable to create: " + today.getAbsolutePath());
		}
	}

	protected Collection<File> removeEmptyDateFolders(final File root) {
		final Collection<File> notDeletedFolders = new ArrayList<>();
		final File[] dateFolders = root.listFiles(DATE_FOLDER_FILTER);
		for (final File folder : dateFolders) {
			if (folder.delete()) {
				System.out.println("Deleted: " + folder.getAbsolutePath());
			} else {
				notDeletedFolders.add(folder);
				System.out.println("Not deleted: " + folder.getAbsolutePath());
			}
		}
		return notDeletedFolders;
	}

	private static class DateFolderFilter implements FileFilter {
		@Override
		public boolean accept(final File file) {
			return file.isDirectory() && FOLDER_PATTERN.matcher(file.getName()).matches();
		}
	}

	protected boolean deepDelete(final File folder) {
		boolean ok = folder.exists();
		if (ok) {
			for (final File file : folder.listFiles()) {
				if (file.isFile()) {
					ok |= file.delete();
				} else if (file.isDirectory()) {
					ok |= deepDelete(file);
					ok |= file.delete();
				}
			}
			folder.delete();
		}
		return ok;
	}
}
