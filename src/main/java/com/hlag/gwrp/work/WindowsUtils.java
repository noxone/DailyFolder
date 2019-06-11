package com.hlag.gwrp.work;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

class WindowsUtils {
	private static final String REGQUERY_UTIL = "reg query ";

	private static final String REGSTR_TOKEN = "REG_SZ";

	private static final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
			+ "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";

	private WindowsUtils() {}

	// static File getLinksFolder(){
	// return new File(System.getenv("userprofile"));
	// }
	//
	// static void createShortcutTo(File folder){
	// }

	static String getCurrentUserDesktopPath() {
		try {
			final Process process = Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD);
			final StreamReader reader = new StreamReader(process.getInputStream());

			reader.start();
			process.waitFor();
			reader.join();
			final String result = reader.getResult();
			final int p = result.indexOf(REGSTR_TOKEN);

			if (p == -1) {
				return null;
			}
			return result.substring(p + REGSTR_TOKEN.length()).trim();
		} catch (final Exception e) {
			return null;
		}
	}

	static class StreamReader extends Thread {
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
				;
			}
		}

		String getResult() {
			return sw.toString();
		}
	}
}
