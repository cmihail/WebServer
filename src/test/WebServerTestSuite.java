package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ InvalidWebServerTest.class, ValidWebServerTest.class,
	PersistentConnectionTest.class })
public class WebServerTestSuite {

}
