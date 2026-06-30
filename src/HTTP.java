import java.io.*;
import javax.microedition.io.*;
import cc.nnproject.json.*;
import javax.microedition.lcdui.Image;

//#ifdef MODERNCONNECTOR
import tech.alicesworld.ModernConnector.*;
//#endif

public class HTTP {
	public static final Exception requestMethodException = new Exception();

	private static byte[] requestWrapped2(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
		HttpConnection hc = null;
		OutputStream os = null;
		// url = App.getPlatformSpecificUrl(url);

		try {
			hc = (HttpConnection)
//#ifdef J2ME_LOADER
				(Settings.useModcon ? ModernConnector.open(url) : Connector.open(url));
//#else
//#ifdef MODERNCONNECTOR
				ModernConnector
//#else
				Connector
//#endif
				.open(url);
//#endif
				
			try {
				hc.setRequestMethod(method);
			}
			catch (Exception e) {
				throw requestMethodException;
			}

			hc.setRequestProperty("Accept", "text/vnd.wap.wml, image/vnd.wap.wbmp");
			
			if (data != null) {
				if (contentType == null) contentType = "application/json";
				hc.setRequestProperty("Content-Type", contentType);
					
				byte[] dataBytes = (data instanceof String) ? Util.stringToBytes((String) data) : (byte[]) data;
				hc.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));

				os = hc.openOutputStream();
				os.write(dataBytes);
			}

			return sendRequest(hc);
		}
		finally {
			try { os.close(); } catch (Exception e) {}
			try { hc.close(); } catch (Exception e) {}
		}
	}

	private static byte[] requestWrapped(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
//#ifndef NO_HTTP_REDIRECT_SUPPORT
		int redirects = 0;

		while (redirects < 5) {
			try {
				return requestWrapped2(method, url, data, contentType, authorize);
			}
			catch (Exception e) {
				if (e instanceof HTTPRedirectException) {
					redirects++;
					String redirUrl = ((HTTPRedirectException) e).getUrl();
					url = new URL(redirUrl, new URL(url)).toString(false);
					continue;
				}
				throw e;
			}
		}
		throw new Exception("Too many redirects while fetching '" + url + "'");
//#else
		return requestWrapped2(method, url, data, contentType, authorize);
//#endif
	}

	public static byte[] sendRequest(HttpConnection hc) throws Exception {
		InputStream is = null;
		try {
			int respCode = hc.getResponseCode();
			is = hc.openInputStream();
			byte[] result = Util.readBytes(is, (int) hc.getLength(), 1024, 2048);

//#ifndef NO_HTTP_REDIRECT_SUPPORT
			if (respCode >= 300 && respCode < 400) {
				String loc = hc.getHeaderField("Location");
				if (loc != null) throw new HTTPRedirectException(loc);
			}
//#endif
			if (respCode == HttpConnection.HTTP_OK) {
				return result;
			}

            throw new Exception("HTTP error " + respCode);
		}
		finally {
			try { is.close(); } catch (Exception e) {}
		}
	}

	public static byte[] request(String method, String url, Object data, String contentType, boolean authorize) throws Exception {
//#ifdef MODERNCONNECTOR
//#ifndef J2ME_LOADER
		try {
//#endif
//#endif

//#ifdef SYMBIAN
			int attempts = 0;

			while (true) {
				try {
					return requestWrapped(method, url, data, contentType, authorize);
				}
				catch (IOException e) {
					// Automatic retry if we get IOException -36 which randomly occurs on Symbian
					if (e.toString().indexOf("-36") != -1) {
						attempts++;
						if (attempts == 3) throw e;
					}
					else throw e;
				}
			}
//#else
//#ifdef J2ME_LOADER
			// J2ME Loader: if proxyless enabled, and HTTPS request (using system TLS) fails, start using java-based TLS instead
			// Android below 5 does not have TLS 1.2 by default
			try {
				return requestWrapped(method, url, data, contentType, authorize);
			}
			catch (IOException e) {
				if (e.toString().indexOf("Failure in SSL library") != -1 || e.toString().indexOf("unsupported protocol") != -1) {
					Settings.useModcon = true;
					Settings.save();
					return requestWrapped(method, url, data, contentType, authorize);
				}
				else throw e;
			}
//#else
			return requestWrapped(method, url, data, contentType, authorize);
//#endif
//#endif

//#ifdef MODERNCONNECTOR
//#ifndef J2ME_LOADER
		}
		catch (Exception e) {
			if (Settings.proxyless && e instanceof SecurityException) {
				// More descriptive error message for when port 443 socket connection cannot be opened because the app is not signed
				throw new SecurityException(Locale.get(PROXYLESS_ERROR_UNSIGNED));
			} else {
				throw e;
			}
		}
//#endif
//#endif
	}

	public static String apiRequest(String method, String url, Object data, boolean useProxy) throws Exception {
		// String fullUrl = getFullUrl(url, useProxy);
		return Util.bytesToString(request(method, url, data, null, true));
	}

	public static byte[] getBytes(String url) throws Exception {
		return request("GET", url, null, null, false);
	}
	public static String get(String url, boolean useProxy) throws Exception {
		return apiRequest("GET", url, null, useProxy);
	}
	public static String post(String url, String data, boolean useProxy) throws Exception {
		return apiRequest("POST", url, data, useProxy);
	}
	public static String post(String url, JSONObject data, boolean useProxy) throws Exception {
		return post(url, data.build(), useProxy);
	}
	
	public static Image getImage(String url) throws Exception {
		byte[] b = getBytes(url);
		return Image.createImage(b, 0, b.length);
	}
}