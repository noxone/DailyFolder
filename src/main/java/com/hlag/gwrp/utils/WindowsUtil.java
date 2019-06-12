package com.hlag.gwrp.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Helper class containing methods to access Windows specific functionality
 *
 * @author neumaol
 *
 */
public final class WindowsUtil {
	private static final String REGQUERY_UTIL = "reg query ";

	private static final String REGSTR_TOKEN = "REG_SZ";

	private static final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
			+ "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";

	private WindowsUtil() {
		throw new RuntimeException();
	}

	/**
	 * Retrieve the path to the current user's Windows desktop
	 *
	 * @return the path to user's Windows desktop
	 */
	public static Optional<Path> getCurrentUserDesktopPath() {
		try {
			final Process process = Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD);
			final StreamReader reader = new StreamReader(process.getInputStream());

			reader.start();
			process.waitFor();
			reader.join();
			final String result = reader.getResult();
			final int p = result.indexOf(REGSTR_TOKEN);

			if (p == -1) {
				return Optional.empty();
			}
			return Optional.of(Paths.get(result.substring(p + REGSTR_TOKEN.length()).trim()));
		} catch (final Exception ignore) {
			return Optional.empty();
		}
	}

	/**
	 * Retrieve the path to the current user's Windows desktop
	 *
	 * @return the path to the user's Windows desktop
	 */
	public static Optional<File> getCurrentUserDesktopFile() {
		return getCurrentUserDesktopPath().map(Path::toFile);
	}

	private static class StreamReader extends Thread {
		private InputStream is;

		private StringWriter sw;

		StreamReader(final InputStream is) {
			this.is = is;
			sw = new StringWriter();
		}

		@Override
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1) {
					sw.write(c);
				}
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		String getResult() {
			return sw.toString();
		}
	}
}
