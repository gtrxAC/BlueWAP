//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.bt.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.io.*;
import java.io.*;
import java.util.*;

public class App extends AppBase implements BluetoothServerListener {
    private BluetoothServer server;
    public static Vector connections = new Vector();

    public static final boolean supportsBluetooth = Util.checkClass("javax.bluetooth.RemoteDevice");

    public void init() {
        pushScreen(LogScreen.instance);

        if (!supportsBluetooth) {
            LogScreen.log("This device does not support Java Bluetooth API (JSR-82). BlueWAP Server cannot run.");
            return;
        }
        server = new BluetoothServer(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);

        try {
            server.start();
            LogScreen.log("BlueWAP server started");
            LogScreen.log("Device name: " + server.getLocalName());
            LogScreen.log("Device address: " + server.getLocalAddress());
        }
        catch (Exception e) {
            bluetoothError(e);
        }
    }

    public void bluetoothError(Exception e) {
        e.printStackTrace();
        LogScreen.log(e.toString());
    }

    public void bluetoothConnected(StreamConnection conn) {
        LogScreen.log("Device connected");

        BluetoothConnection bc = new BluetoothConnection(conn);
        connections.addElement(bc);
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            bc.open();
            input = bc.input;
            output = bc.output;
        }
        catch (Exception e) {
            // Error with BT connection - close connection
            bc.close();
            return;
        }
        
        while (true) {
            RequestData request = null;

            try {
                request = readRequest(input);
                LogScreen.log(request.method + " " + request.url);
            }
            catch (Exception e) {
                // Error with BT connection - close connection
                bc.close();
                return;
            }

            StandardHTTP http = null;
            String resultUrl = request.url;
            byte[] body = null;
            int respCode;

            try {
                http = new StandardHTTP(request.method, request.url);
                for (Enumeration e = request.headers.keys(); e.hasMoreElements(); ) {
                    String key = (String) e.nextElement();
                    String value = (String) request.headers.get(key);
                    http.setHeader(key, value);
                }
                if (request.data != null) {
                    http.setData(request.data);
                }

                body = http.getResponseBytes();
                respCode = http.getResponseCode();
                resultUrl = http.getUrl();
                LogScreen.log("Response received");
            }
            catch (Exception e) {
                // Error with HTTP request - send error over BT
                LogScreen.log("Proxy error: " + e.toString());
                try {
                    String errorWml = WmlTemplates.ERROR_BEGIN +
                        "Proxy error: " +
                        Util.sanitizeWml(e.toString()) +
                        WmlTemplates.ERROR_END;

                    writeResponse(
                        output, 500, new Hashtable(), resultUrl,
                        Util.stringToBytes(errorWml), request.version);
                }
                catch (Exception ex) {
                    // Error with BT connection while sending error response - close connection
                    bc.close();
                    return;
                }
                continue;
            }
            finally {
                try { http.close(); } catch (Exception e) {}
            }

            try {
                writeResponse(
                    output, respCode, new Hashtable(), resultUrl,
                    body, request.version);
                    
                LogScreen.log("Response sent");
            }
            catch (Exception e) {
                // Error with BT connection - close connection
                bc.close();
                return;
            }
        }
    }

    private RequestData readRequest(DataInputStream input) throws IOException {
        byte version = input.readByte();
        if (
            version < BluetoothConnection.PROTOCOL_BASE ||
            version > BluetoothConnection.PROTOCOL_CURRENT
        ) {
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
        return new RequestData(method, url, headers, body, version);
    }

    private void writeResponse(DataOutputStream output, int responseCode, Hashtable headers, String resultUrl, byte[] body, byte version) throws IOException {
        output.writeByte(version);
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

        if (version >= BluetoothConnection.PROTOCOL_ADDED_RESULT_URL) {
            writeString(output, resultUrl);
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

    private static class RequestData {
        public String method;
        public String url;
        public Hashtable headers;
        public byte[] data;
        public byte version;

        public RequestData(String method, String url, Hashtable headers, byte[] data, byte version) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.data = data;
            this.version = version;
        }
    }
}
//#endif