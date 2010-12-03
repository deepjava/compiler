package ch.ntb.inf.deep.loader;

/**
 * Download Exception
 */
public class DownloaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7783915136427481365L;

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            Message
	 */
	public DownloaderException(String msg) {
		super(msg);
	}

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            Message
	 * @param e
	 *            Other exception
	 */
	public DownloaderException(String msg, Exception e) {
		super(msg, e);
	}
}
