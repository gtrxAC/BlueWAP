//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.io.*;
import java.io.*;
import java.util.*;

public class App extends AppBase implements BluetoothListener {
    private Bluetooth bluetooth;
    public static Vector connections = new Vector();

    public void init() {
        pushScreen(LogScreen.instance);
        bluetooth = new Bluetooth(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);
        bluetooth.listen();
        LogScreen.log("Bluetooth server started");
    }

    public void btSearchCompleted(String[] deviceNames, String[] deviceURLs) {

    }

    public void btError(Exception e) {
        LogScreen.log(e.toString());
    }

    /**
     * Called from a separate thread
     * @param conn
     */
    public void btConnected(StreamConnection conn) {
        connections.addElement(conn);

        LogScreen.log("Device connected");
        DataInputStream input = null;
        DataOutputStream output = null;
        try {
            input = conn.openDataInputStream();
            output = conn.openDataOutputStream();

            RequestData request = readRequest(input);
            LogScreen.log("Proxying " + request.method + " " + request.url);

            StandardHTTP http = new StandardHTTP(request.method, request.url);
            for (Enumeration e = request.headers.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String value = (String) request.headers.get(key);
                http.setHeader(key, value);
            }
            if (request.data != null) {
                http.setData(request.data);
            }

            byte[] body = http.getResponseBytes();
            writeResponse(output, http.getResponseCode(), new Hashtable(), body);
            LogScreen.log("Response sent");
        }
        catch (Exception e) {
            LogScreen.log("Proxy error: " + e.toString());
            try {
                writeResponse(output, 500, new Hashtable(), Util.stringToBytes(e.toString()));
            }
            catch (Exception ex) {}
        }
        finally {
            closeQuietly(input);
            closeQuietly(output);
            closeQuietly(conn);
            connections.removeElement(conn);
        }
    }

    private RequestData readRequest(DataInputStream input) throws IOException {
        int version = input.readByte();
        if (version != 1) {
            throw new IOException("Unsupported Bluetooth protocol version: " + version);
        }

        String method = readString(input);
        String url = readString(input);

        int headerCount = input.readInt();
        Hashtable headers = new Hashtable();
        for (int i = 0; i < headerCount; i++) {
            headers.put(readString(input), readString(input));
        }

        int bodyLength = input.readInt();
        byte[] body = new byte[bodyLength];
        input.readFully(body);
        return new RequestData(method, url, headers, body);
    }

    private void writeResponse(DataOutputStream output, int responseCode, Hashtable headers, byte[] body) throws IOException {
        output.writeByte(1);
        output.writeInt(responseCode);

        int headerCount = 0;
        if (headers != null) {
            headerCount = headers.size();
        }
        output.writeInt(headerCount);
        if (headers != null) {
            for (Enumeration e = headers.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String value = (String) headers.get(key);
                writeString(output, key);
                writeString(output, value);
            }
        }

        if (body == null) {
            output.writeInt(0);
        }
        else {
            output.writeInt(body.length);
            output.write(body, 0, body.length);
        }
        output.flush();
    }

    private void writeString(DataOutputStream output, String value) throws IOException {
        if (value == null) {
            output.writeUTF("");
        }
        else {
            output.writeUTF(value);
        }
    }

    private String readString(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    private void closeQuietly(InputStream input) {
        if (input == null) return;
        try { input.close(); } catch (Exception e) {}
    }

    private void closeQuietly(OutputStream output) {
        if (output == null) return;
        try { output.close(); } catch (Exception e) {}
    }

    private void closeQuietly(StreamConnection conn) {
        if (conn == null) return;
        try { conn.close(); } catch (Exception e) {}
    }

    private static class RequestData {
        public String method;
        public String url;
        public Hashtable headers;
        public byte[] data;

        public RequestData(String method, String url, Hashtable headers, byte[] data) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.data = data;
        }
    }
}
//#endif