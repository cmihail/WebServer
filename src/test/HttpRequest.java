package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

public interface HttpRequest {
	void run(BufferedReader reader, Writer writer) throws IOException;
}
