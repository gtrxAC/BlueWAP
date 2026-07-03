import java.io.*;
import java.util.*;
import javax.microedition.io.*;

public class BluetoothHTTP extends HTTP {
	private static final byte PROTOCOL_VERSION = 1;
	private static final String BLUETOOTH_SCHEME_PREFIX = "btspp://";

	public BluetoothHTTP(String method, String url) {
		super(method, url);
	}

	protected InputStream makeRequest() throws Exception {
		BluetoothResponse response = execute(method, url, headers, data);
		responseCode = response.getResponseCode();
		responseBytes = response.getBody();
		if (responseBytes == null) responseBytes = new byte[0];

		if (responseCode >= 300 && responseCode < 400) {
			String redir = (String) response.getHeaders().get("Location");
			if (redir == null) throw new Exception("received redirect with no location");

			url = new URL(redir, new URL(url)).toString(false);
			throw redirectException;
		}
		return new ByteArrayInputStream(responseBytes);
	}

	protected void closeTransport() {
	}

	private BluetoothResponse execute(String method, String url, Hashtable headers, byte[] data) throws Exception {
		String connectionUrl = buildConnectionUrl(url);
		StreamConnection connection = (StreamConnection) Connector.open(connectionUrl);
		OutputStream output = null;
		InputStream input = null;
		try {
			output = connection.openOutputStream();
			DataOutputStream dos = new DataOutputStream(output);
			writeRequest(dos, method, url, headers, data);
			dos.flush();

			input = connection.openInputStream();
			DataInputStream dis = new DataInputStream(input);
			return readResponse(dis);
		}
		finally {
			closeQuietly(input);
			closeQuietly(output);
			closeQuietly(connection);
		}
	}

	private String buildConnectionUrl(String url) {
		String endpoint = url;
		if (endpoint.startsWith("bthttp://")) {
			endpoint = endpoint.substring("bthttp://".length());
		}
		else if (endpoint.startsWith("bt://")) {
			endpoint = endpoint.substring("bt://".length());
		}

		int slash = endpoint.indexOf('/');
		if (slash >= 0) {
			endpoint = endpoint.substring(0, slash);
		}
		if (endpoint.length() == 0) {
			endpoint = "localhost";
		}
		return BLUETOOTH_SCHEME_PREFIX + endpoint + ";name=BlueWAP;authenticate=false;encrypt=false";
	}

	private void writeRequest(DataOutputStream dos, String method, String url, Hashtable headers, byte[] data) throws IOException {
		dos.writeByte(PROTOCOL_VERSION);
		writeString(dos, method);
		writeString(dos, url);

		int headerCount = 0;
		if (headers != null) {
			headerCount = headers.size();
		}
		dos.writeInt(headerCount);
		if (headers != null) {
			for (Enumeration e = headers.keys(); e.hasMoreElements(); ) {
				String key = (String) e.nextElement();
				String value = (String) headers.get(key);
				writeString(dos, key);
				writeString(dos, value);
			}
		}

		if (data == null) {
			dos.writeInt(0);
		}
		else {
			dos.writeInt(data.length);
			dos.write(data, 0, data.length);
		}
	}

	private BluetoothResponse readResponse(DataInputStream dis) throws IOException {
		int version = dis.readByte();
		if (version != PROTOCOL_VERSION) {
			throw new IOException("Unsupported Bluetooth protocol version: " + version);
		}

		int responseCode = dis.readInt();
		int headerCount = dis.readInt();
		Hashtable responseHeaders = new Hashtable();
		for (int i = 0; i < headerCount; i++) {
			String key = readString(dis);
			String value = readString(dis);
			responseHeaders.put(key, value);
		}

		int bodyLength = dis.readInt();
		byte[] body = new byte[bodyLength];
		dis.readFully(body);
		return new BluetoothResponse(responseCode, responseHeaders, body);
	}

	private void writeString(DataOutputStream dos, String value) throws IOException {
		if (value == null) {
			dos.writeUTF("");
		}
		else {
			dos.writeUTF(value);
		}
	}

	private String readString(DataInputStream dis) throws IOException {
		return dis.readUTF();
	}

	private void closeQuietly(InputStream input) {
		if (input == null) return;
		try {
			input.close();
		}
		catch (Exception e) {}
	}

	private void closeQuietly(OutputStream output) {
		if (output == null) return;
		try {
			output.close();
		}
		catch (Exception e) {}
	}

	private void closeQuietly(StreamConnection connection) {
		if (connection == null) return;
		try {
			connection.close();
		}
		catch (Exception e) {}
	}

	private static class BluetoothResponse {
		private int responseCode;
		private Hashtable headers;
		private byte[] body;

		public BluetoothResponse(int responseCode, Hashtable headers, byte[] body) {
			this.responseCode = responseCode;
			this.headers = headers;
			this.body = body;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public Hashtable getHeaders() {
			return headers;
		}

		public byte[] getBody() {
			return body;
		}
	}
}
