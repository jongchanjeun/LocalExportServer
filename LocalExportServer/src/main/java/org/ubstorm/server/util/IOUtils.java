package org.ubstorm.server.util;

import java.io.BufferedReader;
import java.io.IOException;

public class IOUtils {
	/**
	 * @param BufferedReader
	 * @param contentLength
	 * @return
	 * @throws IOException
	 */
	public static String readData(BufferedReader br, int contentLength) throws IOException {
		char[] body = new char[contentLength];
		br.read(body, 0, contentLength);
		return String.copyValueOf(body);
	}
}
