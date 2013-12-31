package server.request;

/**
 * ContentType enum uses the file extension to loosely map the available content type based on common media types:
 * http://en.wikipedia.org/wiki/Internet_media_type
 * 
 * See https://github.com/dasanjos/java-WebServer/blob/master/src/main/java/com/dasanjos/java/http/Status.java
 * for original file.
 */
public enum ContentType {
	CSS("text/css"),
    GIF("image/gif"),
    HTM("text/html"),
    HTML("text/html"),
    ICO("image/gif"),
    JPG("image/jpeg"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    TXT("text/plain"),
    XML("text/xml"),
    OCTET_STREAM("application/octet-stream");
	
	private final String type;
	
	private ContentType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type;
	}
	
	/**
	 * Should be called only for valid files that are not directory.
	 * Does not test if file exits or not. In case of invalid files will return
	 * OCTET_STREAM.
	 * The programmer should make sure the function is called for a valid file.
	 * 
	 * @param filename the filename for which to get the content type
	 * @return the content type
	 */
	public static ContentType getContentType(String filename) {
		try {
			int index = filename.lastIndexOf(".");
			if (index == -1 || index + 1 == filename.length())
				throw new IllegalArgumentException();

			String extension = filename.substring(index + 1).toUpperCase();
			return ContentType.valueOf(extension);
		} catch (IllegalArgumentException e) {
			return OCTET_STREAM;
		}
	}
}
