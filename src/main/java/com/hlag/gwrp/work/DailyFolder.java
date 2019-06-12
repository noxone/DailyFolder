package com.hlag.gwrp.work;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hlag.gwrp.utils.FolderUtil;
import com.hlag.gwrp.utils.WindowsUtil;

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
		final Path desktopFolder = WindowsUtil.getCurrentUserDesktopPath()
				.orElseThrow(() -> new RuntimeException("Path to Windows Desktop is unknown."));

		final Collection<Path> foldersToDeleteEventually = readFoldersToDeleteEventually(desktopFolder);
		final Collection<Path> notDeletedFolders = removeEmptyDateFolders(desktopFolder);
		FolderUtil.createTodaysFolder(desktopFolder);

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

	private void deleteFolders(final Collection<Path> paths) {
		FolderUtil.deleteMultipleRecursivly(paths)
				.forEach(path -> System.out.println("Unable to delete: " + path.toAbsolutePath().toString()));
	}

	private Collection<Path> readFoldersToDeleteEventually(final Path root) {
		final Properties properties = new Properties();
		try (InputStream in = getClass().getResourceAsStream("folders.properties")) {
			properties.load(in);
		} catch (final IOException ignore) {
			return new ArrayList<>();
		}
		final Pattern pattern = Pattern.compile("^fileToDelete\\.[0-9]+$");
		return properties//
				.stringPropertyNames()
				.stream()
				.map(pattern::matcher) // try to match the keys
				.filter(Matcher::matches) // only use matching values
				.map(matcher -> matcher.group(0)) // extract the matched text
				.map(properties::getProperty) // get the property value for the key
				.map(root::resolve) // create a file object pointing to that file
				.filter(Files::exists) // only return existing files
				.collect(toSet());
	}

	private Collection<Path> removeEmptyDateFolders(final Path root) {
		return FolderUtil.findDailyFolders(root)//
				.stream()
				.filter(path -> {
					try {
						if (FolderUtil.isFolderEmpty(path)) {
							Files.delete(path);
							System.out.println("Deleted: " + path.toAbsolutePath().toString());
						}
					} catch (final IOException ignore) {
						return false;
					}
					System.out.println("Not deleted: " + path.toAbsolutePath().toString());
					return true;
				})
				.collect(toList());
	}
}
