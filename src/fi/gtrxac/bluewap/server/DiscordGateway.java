//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import java.io.*;
import java.util.*;
import cc.nnproject.json.*;
import fi.gtrxac.bluewap.Util;
import fi.gtrxac.bluewap.http.HTTP;
import tech.alicesworld.ModernConnector.WebSocketClient;

public class DiscordGateway implements Runnable {
    private WebSocketClient websocket;
    private Vector receivedEvents;
    private Vector supportedEvents;
    private boolean showGuildEmoji;
    private String token;
    private boolean running;

    public DiscordGateway() throws Exception {
        receivedEvents = new Vector();
        supportedEvents = new Vector();
        showGuildEmoji = false;
        running = true;
        
        JSONObject hello = new JSONObject();
        hello.put("op", -1);
        hello.put("t", "GATEWAY_HELLO");

        sendToClient(hello);
    }

    public void disconnect() {
        running = false;
        try {
            websocket.close();
        }
        catch (Exception e) {}
    }

    public String getReceivedData() throws Exception {
        String result = getReceivedDataImpl();
        // LogScreen.log("Sending " + result.length() + " bytes to client");
        return result;
    }

    public boolean hasMore() {
        synchronized (receivedEvents) {
            return receivedEvents.size() != 0;
        }
    }

    private String getReceivedDataImpl() throws Exception {
        if (!running) throw new Exception("connection closed");

        synchronized (receivedEvents) {
            if (receivedEvents.size() == 0) return "";
            String result = (String) receivedEvents.elementAt(0);
            receivedEvents.removeElementAt(0);
            return result;
        }
    }

    public void handleMessage(String message) throws Exception {
        // LogScreen.log("Client: '" + message.substring(0, Math.min(message.length(), 200)) + "'");

        JSONObject messageJson = JSON.getObject(message);

        if (messageJson.getInt("op", 0) == -1) {
            handleProxyMessage(messageJson);
        } else {
            // LogScreen.log("Sending to WS");
            
            if (websocket == null) return;

            try {
                if (messageJson.getObject("d").has("token")) {
                    token = messageJson.getObject("d").getString("token");
                }
                if (messageJson.getObject("d").has("properties")) {
                    JSONObject props = messageJson.getObject("d").getObject("properties");
                    props.put("os", "Android");
                    props.put("browser", "Discord Android");
                }
            }
            catch (Exception e) {}

            websocket.sendMessage(messageJson.build());
        }
    }

    private void handleProxyMessage(JSONObject messageJson) {
        String t = messageJson.getString("t", "");

        if ("GATEWAY_CONNECT".equals(t)) {
            supportedEvents = messageJson.getObject("d").getArray("supported_events").toVector();

            // note: ignoring connection url sent by client
            // "?v=9&encoding=json" parameters are in WebSocketClient
            websocket = new WebSocketClient("gateway.discord.gg", 443);
            websocket.connect();
            new Thread(this).start();
        }
        else if ("GATEWAY_DISCONNECT".equals(t)) {
            disconnect();
        }
        else if ("GATEWAY_UPDATE_SUPPORTED_EVENTS".equals(t)) {
            supportedEvents = messageJson.getArray("d").toVector();
        }
        else if ("GATEWAY_SHOW_GUILD_EMOJI".equals(t)) {
            showGuildEmoji = messageJson.getBoolean("d");
        }
        else if ("GATEWAY_SEND_TYPING".equals(t)) {
            if (token == null) return;

            String channelId = messageJson.getString("d");
            if (channelId.length() < 17 || channelId.length() > 30) return;
            try {
                Long.parseLong(channelId);
            }
            catch (Exception e) { return; }

            String typingUrl = "https://discord.com/api/v9/channels/" + channelId + "/typing";

            try {
                HTTP h = HTTP.createRequest("POST", typingUrl);
                h.setHeader("Authorization", token);
                h.setHeader("X-Super-Properties", "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiRGlzY29yZCBBbmRyb2lkIiwiZGV2aWNlIjoiYTIwZSIsInN5c3RlbV9sb2NhbGUiOiJlbi1VUyIsImhhc19jbGllbnRfbW9kcyI6ZmFsc2UsImNsaWVudF92ZXJzaW9uIjoiMjYyLjUgLSBybiIsInJlbGVhc2VfY2hhbm5lbCI6ImFscGhhIiwiZGV2aWNlX3ZlbmRvcl9pZCI6IjE3NTAzOTI5LWE0YjgtNDQ5MC04N2JmLTAyMjJhZGZkYWRjOCIsImRlc2lnbl9pZCI6MiwiYnJvd3Nlcl91c2VyX2FnZW50IjoiIiwiYnJvd3Nlcl92ZXJzaW9uIjoiIiwib3NfdmVyc2lvbiI6IjM0IiwiY2xpZW50X2J1aWxkX251bWJlciI6MzQ2MywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0=");
                h.setHeader("User-Agent", "Discord-Android/262205;RNA");
                h.setHeader("Accept", "*/*");
                h.setHeader("Accept-Language", "en-US,en;q=0.9");
                h.setHeader("Accept-Encoding", "gzip, deflate, br, zstd");
                h.setHeader("Alt-Used", "discord.com");
                h.setHeader("Cookie", "locale=en-US");
                h.getResponseBytes();
            }
            catch (Exception e) {
                LogScreen.log("Failed to send typing: " + e.toString());
            }
        }
        else if ("GATEWAY_CONNECT_REMOTEAUTH".equals(t)) {
            LogScreen.log("QR login not supported");
        }
    }

    private void handleWebsocketMessage(String message) throws Exception {
        // LogScreen.log("WS: '" + message.substring(0, Math.min(message.length(), 200)) + "'");

        JSONObject messageJson = JSON.getObject(message);
        String t = messageJson.getString("t");

        if ("READY".equals(t)) {
            handleWebsocketReadyMessage(messageJson);
        }
        else if (
            "MESSAGE_CREATE".equals(t) && supportedEvents.indexOf("J2ME_MESSAGE_CREATE") != -1 ||
            "MESSAGE_UPDATE".equals(t) && supportedEvents.indexOf("J2ME_MESSAGE_UPDATE") != -1
        ) {
            JSONObject msg = new JSONObject();
            msg.put("op", -1);
            msg.put("s", messageJson.getInt("s"));
            msg.put("t", "J2ME_" + t);
            msg.put("d", parseMessage(messageJson.getObject("d")));

            sendToClient(msg);
        }
        else if (t == null || supportedEvents.size() == 0 || supportedEvents.indexOf(t) != -1) {
            sendToClient(message + "\n");
        }

        // if ("MESSAGE_CREATE".equals(t) && supportedEvents.indexOf("J2ME_DM_INFO") != -1) {
        // }
    }

    private void handleWebsocketReadyMessage(JSONObject messageJson) throws Exception {
        String userId = messageJson.getObject("d").getObject("user").getString("id");
        JSONObject readyData = new JSONObject();
        readyData.put("id", userId);

        JSONObject ready = new JSONObject();
        ready.put("op", -1);
        ready.put("s", messageJson.getInt("s"));
        ready.put("t", "J2ME_READY");
        ready.put("d", readyData);

        sendToClient(ready);

        if (supportedEvents.indexOf("J2ME_READ_STATES") != -1) {
            JSONArray inEntries = messageJson
                .getObject("d").getObject("read_state").getArray("entries");

            JSONArray outEntries = new JSONArray();

            for (int i = 0; i < inEntries.size(); i++) {
                JSONObject entry = inEntries.getObject(i);
                if (entry.getString("last_message_id", null) == null) continue;

                outEntries.add(entry.getString("id"));
                outEntries.add(entry.getString("last_message_id"));
            }

            JSONObject readStates = new JSONObject();
            readStates.put("op", -1);
            readStates.put("s", messageJson.getInt("s"));
            readStates.put("t", "J2ME_READ_STATES");
            readStates.put("d", outEntries);

            sendToClient(readStates);
        }

        if (supportedEvents.indexOf("READY") != -1) {
            JSONObject fullReady = new JSONObject();
            fullReady.put("op", -1);
            fullReady.put("s", messageJson.getInt("s"));
            fullReady.put("t", "READY");
            fullReady.put("d", messageJson.getObject("d"));

            sendToClient(fullReady);
        }

        // if (supportedEvents.indexOf("J2ME_DM_INFO") != -1) {
        // }
    }

    private JSONObject parseMessage(JSONObject message) {
        // TODO: parse emojis and remove unused fields like the original proxy does
        return message;
    }

    private void sendToClient(JSONObject json) throws Exception {
        sendToClient(json.build() + "\n");
    }

    private void sendToClient(String message) throws Exception {
        // LogScreen.log("Send to client: '" + message.substring(0, Math.min(message.length(), 200)) + "'");

        synchronized (receivedEvents) {
            receivedEvents.addElement(message);
        }
    }

    public void run() {
        while (running) {
            try {
                String msg = websocket.receiveMessageString();
                if (msg == null || msg.trim().length() == 0) continue;
                handleWebsocketMessage(msg);
            }
            catch (Exception e) {
                LogScreen.log("WebSocket read error: " + e.toString());
                disconnect();
            }
        }
    }
}
//#endif