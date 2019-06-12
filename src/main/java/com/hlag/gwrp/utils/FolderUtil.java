package com.hlag.gwrp.utils;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Utility class containing methods for reading and manipulating folders
 *
 * @author Olaf Neumann
 *
 */
public final class FolderUtil {
	/** DateFormatter to create a name for a daily folder */
	private static final DateTimeFormatter FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/** Pattern to recognize a daily folder */
	private static final Pattern FOLDER_PATTERN
			= Pattern.compile("^(?<year>[0-9]{4})-(?<month>[0-9]{2})-(?<day>[0-9]{2})$");

	private FolderUtil() {
		throw new RuntimeException();
	}

	private static String getTodaysFolderName() {
		return FOLDER_FORMAT.format(LocalDate.now());
	}

	/**
	 * Resolves the name to the folder of the current day
	 *
	 * @param root the folder which will be used as the base path for todays folder
	 * @return the full path to todays folder
	 */
	public static Path getPathToTodaysFolder(final Path root) {
		return root.resolve(getTodaysFolderName());
	}

	/**
	 * Checks whether the given path is possibly a "daily folder"
	 *
	 * @param path the path to check
	 * @return <code>true</code> if the folder is possibly a "daily folder"
	 */
	public static boolean isConformToDailyFolderPattern(final Path path) {
		return isConformToDailyFolderPattern(path.getFileName().toString());
	}

	/**
	 * Checks whether the given name conforms to the pattern of a "daily folder"
	 *
	 * @param name the name to check
	 * @return <code>true</code> if the name possibly denotes a "daily folder"
	 */
	public static boolean isConformToDailyFolderPattern(final String name) {
		return FOLDER_PATTERN.matcher(name).matches();
	}

	/**
	 * Creates the todays folder
	 *
	 * @param root the folder which will be used as the base path for todays folder
	 * @return the path to todays folder
	 * @throws IOException if something fails while creating the folder
	 */
	public static Optional<Path> createTodaysFolder(final Path root) {
		try {
			final Path todaysFolder = getPathToTodaysFolder(root);
			Files.createDirectories(todaysFolder);
			return Optional.of(todaysFolder);
		} catch (final IOException ignore) {
			return Optional.empty();
		}
	}

	/**
	 * Finds all "daily folders" in the given root directory
	 *
	 * @param root the folder which will be used as the base path for todays folder
	 * @return a possibly empty {@link List} of folders that conform to the daily
	 *         folder pattern. This method does not return <code>null</code>.
	 */
	public static List<Path> findDailyFolders(final Path root) {
		try (Stream<Path> path = Files.list(root)) {
			return path.filter(Files::isDirectory) // only have a look at folders
					.filter(FolderUtil::isConformToDailyFolderPattern) // retain only daily folders
					.collect(toList());
		} catch (final IOException ignore) {
			// TODO log exception
			return new ArrayList<>();
		}
	}

	/**
	 * Checks if the given directory is empty.
	 *
	 * @param path the folder to check
	 * @return <code>true</code> if the directory is empty. <code>false</code> if
	 *         the folder is not empty or if the given path is not a directory.
	 */
	public static boolean isFolderEmpty(final Path path) {
		if (!Files.isDirectory(path)) {
			return false;
		}
		try (Stream<Path> stream = Files.list(path)) {
			return stream.count() == 0;
		} catch (final IOException ignore) {
			// TODO log exception
			return false;
		}
	}

	/**
	 * Delete a given path and if it is a directory also delete its content
	 *
	 * @param path the file system item to delete
	 * @return <code>true</code> if the deletion was successful. <code>false</code>
	 *         if the path did not exists or a problem occurred while deleting.
	 */
	public static boolean deleteRecursivly(final Path path) {
		// check if there is something to delete
		if (!Files.exists(path)) {
			return false;
		}

		try {
			// if it is a directory delete the content
			try (Stream<Path> stream = Files.list(path)) {
				if (!stream.allMatch(FolderUtil::deleteRecursivly)) {
					return false;
				}
			}

			// finally delete this thing
			return Files.deleteIfExists(path);
		} catch (final IOException ignore) {
			// TODO log exception
			return false;
		}
	}

	/**
	 * Deletes multiple paths recursivly and returns all paths that could not be
	 * deleted
	 *
	 * @param paths the paths to be deleted
	 * @return the paths that could not be deleted
	 */
	public static List<Path> deleteMultipleRecursivly(final Collection<Path> paths) {
		return paths//
				.stream()
				.filter(path -> !FolderUtil.deleteRecursivly(path))
				.collect(toList());
	}
}
