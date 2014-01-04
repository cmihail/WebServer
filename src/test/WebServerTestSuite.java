package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Run all tests.
 * 
 * @author cmihail
 */
@RunWith(Suite.class)
@SuiteClasses({ InvalidWebServerTest.class, ValidWebServerTest.class,
	PersistentConnectionTest.class })
public class WebServerTestSuite {

}
