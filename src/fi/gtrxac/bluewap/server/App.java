//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.bt.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.http.*;
import javax.bluetooth.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.io.*;
import java.io.*;
import java.util.*;

public class App extends AppBase implements BluetoothServerListener, BluetoothHTTPProtocol {
    private BluetoothServer server;
    public static Vector connections = new Vector();
    public static Vector iStreams = new Vector();
    public static Vector oStreams = new Vector();

    public static Hashtable discordGateways = new Hashtable();
    private static int discordGatewayCounter = 0;

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

            String addr = server.getLocalAddress();

            if (addr.equals("(unknown)")) {
                LogScreen.log("If the client device does not show device names when searching, find the BT address of this device to identify it from the list.");
                LogScreen.log("(Settings -> About phone -> Status information)");
                LogScreen.log("To prevent connection errors, you may have to disable battery optimizations.");
                LogScreen.log("(Settings -> Apps -> J2ME Loader -> Battery -> Unrestricted)");
            } else {
                LogScreen.log("Device address: " + addr);
            }
        }
        catch (Exception e) {
            bluetoothError(e);
        }
    }

    public void bluetoothError(Exception e) {
        e.printStackTrace();
        LogScreen.log(e.toString());
    }

    public void bluetoothConnected(StreamConnection conn, DataInputStream dis, DataOutputStream dos) {
        String devName = "Device";
        try {
            RemoteDevice dev = RemoteDevice.getRemoteDevice(conn);
            String devFriendlyName = dev.getFriendlyName(true);

            if (devFriendlyName.length() > 0) {
                devName = devFriendlyName;
            }
        }
        catch (Exception e) {}

        LogScreen.log(devName + " connected");

        connections.addElement(conn);
        iStreams.addElement(dis);
        oStreams.addElement(dos);
        Vector ownedDiscordGateways = new Vector();
        
        while (true) {
            RequestData req = null;

            try {
                req = readRequest(dis);
            }
            catch (Exception e) {
                // Error with BT connection - close connection
                break;
            }

            ResponseData resp = null;
            boolean isDiscordProtocol = req.url.startsWith("discord://");
            boolean showLogs = !isDiscordProtocol;

            try {
                if (showLogs) LogScreen.log(req.method + " " + req.url);

                if (req.url.startsWith("http://") || req.url.startsWith("https://")) {
                    resp = handleHttpRequest(req);
                }
                else if (isDiscordProtocol) {
                    resp = handleDiscordRequest(req, ownedDiscordGateways);
                }
                else {
                    throw new Exception("Unsupported protocol");
                }

                if (showLogs) LogScreen.log("Response received");
            }
            catch (Exception e) {
                // Error with HTTP request - send error over BT
                LogScreen.log("Proxy error: " + e.toString());
                try {
                    String errorWml = WmlTemplates.ERROR_BEGIN +
                        "Proxy error: " +
                        Util.sanitizeWml(e.toString()) +
                        WmlTemplates.ERROR_END;

                    resp = new ResponseData(
                        req.url, new Hashtable(), 500, Util.stringToBytes(errorWml));
                }
                catch (Exception ex) {
                    // Error with BT connection while sending error response - close connection
                    break;
                }
            }

            try {
                writeResponse(dos, resp, req.version);
                if (showLogs) LogScreen.log("Response sent");
            }
            catch (Exception e) {
                // Error with BT connection - close connection
                break;
            }
        }

        LogScreen.log(devName + " disconnected");

        connections.removeElement(conn);
        iStreams.removeElement(dis);
        oStreams.removeElement(dos);

        for (int i = 0; i < ownedDiscordGateways.size(); i++) {
            String connectionId = (String) ownedDiscordGateways.elementAt(i);
            DiscordGateway gateway = (DiscordGateway) discordGateways.get(connectionId);
            if (gateway == null) continue;
            gateway.disconnect();
            discordGateways.remove(connectionId);
        }
    }

    private ResponseData handleHttpRequest(RequestData req) throws Exception {
        StandardHTTP http = null;
        try {
            http = new StandardHTTP(req.method, req.url);
            for (Enumeration e = req.headers.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String value = (String) req.headers.get(key);
                http.setHeader(key, value);
            }
            if (req.data != null) {
                http.setData(req.data);
            }

            String resultUrl = http.getUrl();
            Hashtable responseHeaders = http.getResponseHeaders();
            int responseCode = http.getResponseCode();
            byte[] responseBody = http.getResponseBytes();

            return new ResponseData(resultUrl, responseHeaders, responseCode, responseBody);
        }
        finally {
            try { http.close(); } catch (Exception e) {}
        }
    }

    private ResponseData handleDiscordRequest(RequestData req, Vector ownedGateways) throws Exception {
        if (req.url.equals("discord://connect")) {
            String connectionId = Integer.toString(discordGatewayCounter);

            discordGateways.put(connectionId, new DiscordGateway());
            ownedGateways.addElement(connectionId);

            LogScreen.log("Discord gateway connected");
            return new ResponseData("", new Hashtable(), 200, Util.stringToBytes(connectionId));
        }

        String connectionId = req.url.substring("discord://".length());
        DiscordGateway gateway = (DiscordGateway) discordGateways.get(connectionId);

        if (gateway == null) {
            throw new Exception("Invalid connection ID");
        }

        if (req.method.equals("GET")) {
            byte[] data = gateway.getReceivedData();
            return new ResponseData("", new Hashtable(), 200, data);
        }
        else if (req.method.equals("POST")) {
            gateway.handleMessage(Util.bytesToString(req.data));
            return new ResponseData("", new Hashtable(), 200, null);
        }
        else if (req.method.equals("DELETE")) {
            gateway.disconnect();
            return new ResponseData("", new Hashtable(), 200, null);
        }
        
        throw new Exception("Invalid method '" + req.method + "'");
    }

    private RequestData readRequest(DataInputStream input) throws IOException {
        byte version = input.readByte();
        if (version < PROTOCOL_BASE || version > PROTOCOL_CURRENT) {
            throw new IOException("Unsupported Bluetooth protocol version: " + version);
        }

        String method = input.readUTF();
        String url = input.readUTF();

        int headerCount = input.readInt();
        Hashtable headers = new Hashtable();
        for (int i = 0; i < headerCount; i++) {
            headers.put(input.readUTF(), input.readUTF());
        }

        int bodyLength = input.readInt();
        byte[] body = new byte[bodyLength];
        input.readFully(body);
        return new RequestData(method, url, headers, body, version);
    }

    private void writeResponse(DataOutputStream output, ResponseData resp, byte version) throws IOException {
        output.writeByte(version);
        output.writeInt(resp.code);

        if (resp.headers == null) {
            output.writeInt(0);
        } else {
            output.writeInt(resp.headers.size());

            for (Enumeration e = resp.headers.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String value = (String) resp.headers.get(key);
                writeString(output, key);
                writeString(output, value);
            }
        }

        if (version >= PROTOCOL_ADDED_RESULT_URL) {
            writeString(output, resp.resultUrl);
        }

        if (resp.body == null) {
            output.writeInt(0);
        } else {
            output.writeInt(resp.body.length);
            output.write(resp.body, 0, resp.body.length);
        }
        output.flush();
    }

    private void writeString(DataOutputStream output, String value) throws IOException {
        if (value == null) {
            output.writeUTF("");
        } else {
            output.writeUTF(value);
        }
    }
}
//#endif