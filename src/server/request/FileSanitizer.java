package server.request;

import java.io.File;
import java.nio.file.AccessDeniedException;

import server.Constants;

public class FileSanitizer {
	/**
	 * @param filename the file name
	 * @return the correspondent file for filename
	 * @throws AccessDeniedException if filename contains ".."
	 */
	public static File getFile(String filename) throws AccessDeniedException {
		if (filename.contains("..")) // Filename is not allowed to contain ".."
			throw new AccessDeniedException(filename);
		if (filename.startsWith("/"))
			filename = filename.substring(1);
		return new File(Constants.ROOT, filename);
	}
}
