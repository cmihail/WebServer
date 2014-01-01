package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;

import server.Constants;
import server.request.FileProcessor;

public class Get extends Headers {

	private File file;
	private String filename;
	
	public Get(String request, Set<String> expectedLines, File file, String filename) {
		super(request, expectedLines);
		this.filename = filename;
		this.file = file;
	}

	@Override
	public void run(BufferedReader reader, Writer writer) throws IOException {
		super.run(reader, writer);

		char[] buffer = new char[Constants.FILE_BUFFER_SIZE];
		
		// Test directory.
		if (file.isDirectory()) {
			String result = FileProcessor.getDirectoryContentAsHtml(file, filename);
			int readUntilNow = 0;
			int read;
			while ((read = reader.read(buffer)) >= 0) {
				assertEquals(new String(buffer).subSequence(0,  read),
						result.substring(readUntilNow, readUntilNow + read));
				readUntilNow += read;
			}
			assertEquals(readUntilNow, result.length());
			
			return;
		}
		
		
		// Test normal file.
		InputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);

			byte[] bufferExpected = new byte[Constants.FILE_BUFFER_SIZE];
			while (fileInputStream.available() > 0) {
				int readExpected = fileInputStream.read(bufferExpected);
				int read = 0;
				while (read < readExpected) {
					read = reader.read(buffer, read, readExpected - read);	
				}
				
				assertArrayEquals(bufferExpected, new String(buffer).getBytes());
			}
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
		}
	}
}
