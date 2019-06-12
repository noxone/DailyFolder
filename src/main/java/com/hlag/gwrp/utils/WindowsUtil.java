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
	/** Command to retrieve values from the Windows registry */
	private static final String REGQUERY_UTIL = "reg query ";

	/** Some string used later */
	private static final String REGSTR_TOKEN = "REG_SZ";

	/** Registry path to the information where the Windows desktop is */
	private static final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
			+ "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";

	/** private constructor to not allow instantiation */
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
			final InputStreamCapturingThread reader = new InputStreamCapturingThread(process.getInputStream());

			reader.start();
			process.waitFor();
			reader.join();
			final String result = reader.getResult();
			final int p = result.indexOf(REGSTR_TOKEN);

			if (p == -1) {
				return Optional.empty();
			}
			return Optional.of(Paths.get(result.substring(p + REGSTR_TOKEN.length()).trim()));
		} catch (final InterruptedException | IOException ignore) {
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

	/**
	 * Thread specialized to read process output
	 *
	 * @author Olaf Neumann
	 *
	 */
	private static class InputStreamCapturingThread extends Thread {
		/** The stream to read from */
		private InputStream is;

		/** Buffer to capture output in */
		private StringWriter sw;

		/**
		 * Create a new capturing thread to read from the given stream
		 *
		 * @param is the stream to read from
		 */
		InputStreamCapturingThread(final InputStream is) {
			this.is = is;
			sw = new StringWriter();
		}

		/** {@inheritDoc} */
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

		/**
		 * Retrieve the captured data
		 *
		 * @return the captured data
		 */
		String getResult() {
			return sw.toString();
		}
	}
}
