package com.hlag.gwrp.work;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hlag.gwrp.utils.WindowsUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main application class creating the application window and handling the
 * folder logic
 *
 * @author neumaol
 *
 */
@SuppressWarnings("restriction")
public class DailyFolder extends Application {
	/** Pattern defining how a <code>DailyFolder</code> folder looks like */
	private static final Pattern FOLDER_PATTERN = Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})$");

	/** Filter restricting the file search to the {@link #FOLDER_PATTERN} */
	private static final FileFilter DATE_FOLDER_FILTER = file -> file != null && file.isDirectory()
			&& FOLDER_PATTERN.matcher(file.getName()).matches();

	/** Used to generate new folder names */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Start the application. <br>
	 * Arguments will be ignored
	 *
	 * @param args The application's command line arguments. Will be ignored
	 */
	public static void main(final String[] args) {
		launch();
	}

	/**
	 * Create a {@link DailyFolder} instance
	 */
	public DailyFolder() {
		// nothing to do
	}

	/** {@inheritDoc} */
	@Override
	public void start(@Nullable final Stage stage) throws Exception {
		doDailyFolderWork(Objects.requireNonNull(stage));
	}

	private void doDailyFolderWork(final Stage stage) {
		final File desktopFolder = WindowsUtils.getCurrentUserDesktopPath()
				.orElseThrow(() -> new RuntimeException("Path to Windows Desktop is unknown."));

		Collection<File> foldersToDeleteEventually = new ArrayList<>();
		try {
			foldersToDeleteEventually = readFoldersToDeleteEventually(desktopFolder);
		} catch (final IOException ignore) {
			/* empty on purpose */
		}
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
			DeleteFoldersDialogFx.showDialog(stage, notDeletedFolders, getHostServices(),
					folders -> deleteFolders(folders), alwaysOnTop);
		}
	}

	private void deleteFolders(final Collection<File> folders) {
		folders//
				.stream().filter(f -> !deepDelete(f))
				.forEach(f -> System.out.println("Unable to delete: " + f.getAbsolutePath()));
	}

	private Collection<File> readFoldersToDeleteEventually(final File root) throws IOException {
		final Properties properties = new Properties();
		try (InputStream in = getClass().getResourceAsStream("folders.properties")) {
			properties.load(in);
		}
		final Pattern pattern = Pattern.compile("^fileToDelete\\.[0-9]+$");
		return properties//
				.stringPropertyNames().stream().map(pattern::matcher) // try to match the keys
				.filter(Matcher::matches) // only use matching values
				.map(matcher -> matcher.group(0)) // extract the matched text
				.map(properties::getProperty) // get the property value for the key
				.map(filename -> new File(root, filename)) // create a file object pointing to that file
				.filter(File::exists) // only return existing files
				.collect(toSet());
	}

	private void createTodaysFolder(final File root) {
		final String name = DATE_FORMAT.format(new Date());
		final File today = new File(root, name);
		if (today.mkdirs()) {
			System.out.println("Created: " + today.getAbsolutePath());
		} else {
			System.out.println("Unable to create: " + today.getAbsolutePath());
		}
	}

	private Collection<File> removeEmptyDateFolders(final File root) {
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

	private boolean deepDelete(final File folder) {
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
