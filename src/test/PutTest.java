package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;

/**
 * Tests a put request.
 * 
 * @author cmihail
 */
public class PutTest extends HeadersTest {
	
	private final File originFile;
	private final File newFile;
	private final boolean testFiles;
	
	private byte[] originFileBytes;

	/**
	 * Should be used only on small files as it loads all file bytes into memory.
	 * Also sleeps for 200 ms to make sure that the server had enough time
	 * to write the new file.
	 */
	private PutTest(String request, Set<String> expectedLines, File originFile, File newFile,
			boolean testFiles) {
		super(request, expectedLines);
		this.originFile = originFile;
		this.newFile = newFile;
		this.testFiles = testFiles;
	}
	
	/**
	 * Tests files for equality. 
	 */
	public PutTest(String request, Set<String> expectedLines, File originFile, File newFile) {
		this(request, expectedLines, originFile, newFile, true);
	}
	
	/**
	 * Does not test files for equality, but only headers.
	 */
	public PutTest(String request, Set<String> expectedLines) {
		this(request, expectedLines, null, null, false);
	}
	
	@Override
	protected void write(Writer writer) throws IOException {
		writer.write(request);
		
		if (testFiles) {
			originFileBytes = Files.readAllBytes(
					FileSystems.getDefault().getPath(originFile.getPath()));
			writer.write(new String(originFileBytes));
		}
		
		writer.flush();
	}

	@Override
	public void run(BufferedReader reader, Writer writer) throws IOException {
		super.run(reader, writer);
		
		if (!testFiles)
			return;
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			fail();
		}
		
		byte[] destBytes = Files.readAllBytes(
				FileSystems.getDefault().getPath(newFile.getPath()));
		
		assertArrayEquals(originFileBytes, destBytes);
	}
}
