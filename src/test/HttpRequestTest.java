package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * An HTTP request test.
 * 
 * @author cmihail
 */
public interface HttpRequestTest {
	/**
	 * Run test.
	 * @param reader the client socket reader
	 * @param writer the client socket writer
	 * @throws IOException
	 */
	void run(BufferedReader reader, Writer writer) throws IOException;
}
