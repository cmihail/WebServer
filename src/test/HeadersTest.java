package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class HeadersTest implements HttpRequestTest {
	
	String request;
	Set<String> expectedLines;
	
	HeadersTest(String request, Set<String> expectedLines) {
		this.request = request;
		this.expectedLines = expectedLines;
	}
	
	/**
	 * Override this in children if request has a body too.
	 * Call this method first in the overridden method so the request is written
	 * the first.
	 */
	protected void write(Writer writer) throws IOException  {
		writer.write(request);
		writer.flush();
	}
	
	@Override
	public void run(BufferedReader reader, Writer writer) throws IOException {
		write(writer);
		
		String str = reader.readLine();
		while (str != null && !"".equals(str)) {
			if (!expectedLines.contains(str))
				System.out.println("\nUnexpected header: " + str);

			assertTrue(expectedLines.contains(str));
			expectedLines.remove(str);
			
			str = reader.readLine();
		}
		
		assertTrue(expectedLines.isEmpty());
	}
}
