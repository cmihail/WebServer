package server.request;

import java.io.File;
import java.nio.file.AccessDeniedException;

import server.Constants;

public class FileProcessor {
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
	
	
	public static String getDirectoryContentAsHtml(File file, String uri) {
		StringBuilder dirContent = new StringBuilder("<html><head><title>Index of ");
		dirContent.append(uri);
		dirContent.append("</title></head><body><h1>Index of ");
		dirContent.append(uri);
		dirContent.append("</h1><hr><pre>");
		
		File[] files = file.listFiles();
        for (File subfile : files) {
        	dirContent.append(" <a href=\"" + subfile.getPath() + "\">" + subfile.getPath() + "</a>\n");
        }
        dirContent.append("<hr></pre></body></html>");
        
        return dirContent.toString();
	}
}
