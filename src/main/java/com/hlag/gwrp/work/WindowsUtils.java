package com.hlag.gwrp.work;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Helper class containing methods to access Windows specific functionality
 *
 * @author neumaol
 *
 */
final class WindowsUtils {
	private static final String REGQUERY_UTIL = "reg query ";

	private static final String REGSTR_TOKEN = "REG_SZ";

	private static final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
			+ "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";

	private WindowsUtils() {
		throw new RuntimeException();
	}

	/**
	 * Retrieve the path to the current users Windows desktop
	 *
	 * @return the
	 */
	static Optional<File> getCurrentUserDesktopPath() {
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
			return Optional.of(new File(result.substring(p + REGSTR_TOKEN.length()).trim()));
		} catch (@SuppressWarnings("unused") final Exception ignore) {
			return Optional.empty();
		}
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
