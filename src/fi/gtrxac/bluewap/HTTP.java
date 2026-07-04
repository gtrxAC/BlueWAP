package fi.gtrxac.bluewap;

import fi.gtrxac.bluewap.client.*;
import java.io.*;
import java.util.*;
import javax.microedition.lcdui.Image;

public abstract class HTTP {
	private static final String DEFAULT_ACCEPT = "text/vnd.wap.wml, image/vnd.wap.wbmp";
	private static final String DEFAULT_CONTENT_TYPE = "application/json";

//#ifdef BLUEWAP_CLIENT
	public static final int CONNECTION_TYPE_STANDARD = 0;
	public static final int CONNECTION_TYPE_BLUETOOTH = 1;
	public static int CONNECTION_TYPE = CONNECTION_TYPE_STANDARD;
//#endif

	protected String method;
	protected String url;
	protected byte[] data;
	protected Hashtable headers;
	protected int responseCode;
	protected byte[] responseBytes;
	protected InputStream is;
	private boolean requestMade;

	protected HTTP(String method, String url) {
		this.method = method;
		this.url = url;
		this.headers = new Hashtable();
		headers.put("Accept", DEFAULT_ACCEPT);
	}

	public static HTTP createRequest(String method, String url) {
//#ifdef BLUEWAP_CLIENT
		if (CONNECTION_TYPE == CONNECTION_TYPE_BLUETOOTH) {
			return new BluetoothHTTP(method, url);
		}
//#endif
		return new StandardHTTP(method, url);
	}

	public static HTTP createRequest(String url) {
		return createRequest("GET", url);
	}

	public static void setConnectionType(int connectionType) {
		if (connectionType == CONNECTION_TYPE_STANDARD || connectionType == CONNECTION_TYPE_BLUETOOTH) {
			CONNECTION_TYPE = connectionType;
		}
	}

	protected abstract InputStream makeRequest() throws Exception;

	protected abstract void closeTransport();

	protected void checkMakeRequest() throws Exception {
		if (!requestMade) {
			is = makeRequest();
			requestMade = true;
		}
	}

	/**
	 * Public API
	 */

	/**
	 * Set a HTTP request header that will be sent to the server with this request.
	 */
	public HTTP setHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	/**
	 * Set the data that will be sent to the server with this request.
	 */
	public HTTP setData(byte[] data) {
		this.data = data;

		if (data != null) {
			headers.put("Content-Length", String.valueOf(data.length));
		} else {
			headers.remove("Content-Length");
		}

		if (!headers.containsKey("Content-Type")) {
			headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
		}
		return this;
	}

	/**
	 * Set the data that will be sent to the server with this request.
	 */
	public HTTP setData(String data) {
		return setData(Util.stringToBytes(data));
	}

	/**
	 * Get the HTTP response code returned by the server.
	 */
	public int getResponseCode() throws Exception {
		checkMakeRequest();
		return responseCode;
	}

	/**
	 * Get server response as an input stream that can be used to read streamed data.
	 * Stream must be closed with `close()` after use.
	 */
	public InputStream getResponseStream() throws Exception {
		checkMakeRequest();
		return is;
	}

	/**
	 * Get server response as a byte array.
	 */
	public byte[] getResponseBytes() throws Exception {
		checkMakeRequest();
		if (responseBytes != null) {
			return responseBytes;
		}
		return Util.readBytes(is, 0, 1024, 2048);
	}

	/**
	 * Get server response as a string.
	 */
	public String getResponseString() throws Exception {
		return Util.bytesToString(getResponseBytes());
	}

	/**
	 * Get server response as an image.
	 */
	public Image getResponseImage() throws Exception {
		return Image.createImage(getResponseStream());
	}
	
	/**
	 * Close all streams related to this request.
	 */
	public void close() {
		closeTransport();
		try { is.close(); } catch (Exception e) {}
	}
}