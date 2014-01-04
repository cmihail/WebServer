package test.helper;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;

import server.Constants;
import server.request.helper.FileProcessor;

/**
 * Tests a Get request.
 *
 * @author cmihail
 */
public class GetTest extends ResponseTest {

	private String filename;
	
	public GetTest(String request, Set<String> expectedLines, String filename) {
		super(request, expectedLines);
		this.filename = filename;
	}

	@Override
	public void run(BufferedReader reader, Writer writer) throws IOException {
		super.run(reader, writer);

		File file = FileProcessor.getFile(filename);
		
		// Test directory.
		if (file.isDirectory()) {
			char[] buffer = new char[Constants.FILE_BUFFER_SIZE];

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
		byte[] bytes = Files.readAllBytes(
				FileSystems.getDefault().getPath(file.getPath()));
		char[] expectedBytes = new char[bytes.length];
		
		int read = 0;
		while (read < expectedBytes.length) {
			read += reader.read(expectedBytes, read, expectedBytes.length - read);	
		}
		
		assertArrayEquals(bytes, new String(expectedBytes).getBytes());
	}
}
