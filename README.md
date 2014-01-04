WebServer
=========

This project implements a multi-threaded file based web server.
It supports persistent connections for both HTTP version 1.0 and version 1.1
(keeping the connection alive is based on the request version dependent headers).

It supports basic HEAD, GET, PUT and DELETE methods. HEAD returns the same result as
GET, with the exception of the body. PUT adds new files or overwrites existing files.
DELETE removes an existing file.

The Server is based on the Factory Pattern in order to make it easy to change
implementations of methods requests.
Thread-pooling is implemented using "Executors.newCachedThreadPool()". This is a
"ExecutorService" that reuses threads that have ended their tasks in order to avoid
the overhead of thread creation.

Running:
- import the project inside Eclipse
- if there are any dependecy problems make sure the *.jar libraries are in the
  building path
- the server can be run by using "server.Main"

Testing:
- for testing the JUnit framework was used
- the tests were run using Eclipe; for tests run test.WebServerTestSuite
- it tests server configuration, server responses to both valid and invalid requests
  and persistent connection functionality
- tests are NOT unit tests, but they test the whole functionality of the server,
  from processing a request to creating the response and also closing the connection
  if necessary; in general they should behave well because the server treats every
  request in a separate thread, with the exception of the persistent connections

Limitations:
- there is no support for multiple line headers (see RFC 2616, Section 2.2);
  a header can be present on only one line
- ignores all headers that doesn't know how to process, even if some or necessary;
  should return "501 (Not Implemented)" for the ones that are necessary;
  for example see PUT method in RFC, section 9.6, that requires all "Content-*"
  headers to be processed; this should be taken in consideration in next versions
- for now there is no support to add or remove folders; these must be manually
  added or removed
- there is no limitation to the number of incomming connections and no load balancing;
  the server will allow connections as long as the system permits it and if there
  are too many connections it might fail
- the methods that are implemented offer only basic functionality and for now they
  don't respect the RFC 100%

Sources:
- https://github.com/dasanjos/java-WebServer
- https://github.com/jrudolph/Pooling-web-server
- http://www.ietf.org/rfc/rfc2616.txt
- http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol

