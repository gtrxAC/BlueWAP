package fi.gtrxac.bluewap.bt;

import javax.bluetooth.*;

import java.io.IOException;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;

public class BluetoothServer implements Runnable {
    private static LocalDevice localDevice;
    private static String localName;

    private String uuid;
    private String serviceName;
    private BluetoothServerListener listener;

    private boolean running;
    private StreamConnectionNotifier server;

    /**
     * Create a new Bluetooth manager instance.
     * Bluetooth applications are identified using an UUID string and a service name. These should be unique across different apps. The server and the client for the same app should share the same UUID and service name.
     * 
     * @param uuid 32-character string containing the UUID to use for identifying this application, for example generated with uuidgen (with the dashes removed)
     * @param serviceName Name to use for identifying this application's service
     * @param listener Listener to use for Bluetooth event callbacks
     */
    public BluetoothServer(String uuid, String serviceName, BluetoothServerListener listener) {
        this.uuid = uuid;
        this.serviceName = serviceName;
        this.listener = listener;
    }

    /**
     * Start listening for connections from other devices.
     * When a connection is established, the listener's bluetoothConnected callback is called.
     */
    public void start() throws IOException {
        if (running) return;

        validateArgs();
        setupLocalDevice();

        String url = "btspp://localhost:" + uuid + ";name=" + serviceName +
            ";authenticate=false;encrypt=false";

        server = (StreamConnectionNotifier) Connector.open(url);
        new Thread(this).start();

        running = true;
    }

    /**
     * Stop listening for connections from other devices.
     */
    public void stop() {
        if (!running) return;

        try {
            server.close();
        }
        catch (Exception e) {}
        
        running = false;
    }

    /**
     * Gets the current device's Bluetooth name which is shown to other devices.
     */
    public String getLocalName() {
        try {
            setupLocalDevice();
        }
        catch (IOException e) {}

        if (localName == null) return "(unknown)";
        return localName;
    }

    /**
     * Gets the current device's Bluetooth MAC address.
     */
    public String getLocalAddress() {
        try {
            setupLocalDevice();
            String addr = localDevice.getBluetoothAddress();
            if (addr.equals("020000000000")) return "(unknown)";
            return addr;
        }
        catch (IOException e) {
            return "(unknown)";
        }
    }

    private static void setupLocalDevice() throws IOException {
        if (localDevice != null) return;
        localDevice = LocalDevice.getLocalDevice();

        if (localDevice == null) {
            throw new IOException("cannot get local device, make sure Bluetooth is supported and enabled");
        }
        localName = localDevice.getFriendlyName();
        localDevice.setDiscoverable(DiscoveryAgent.GIAC);
    }

    private void validateArgs() {
        if (uuid.length() != 32) {
            throw new IllegalArgumentException("UUID length is not 32");
        }

        try {
            Long.parseLong(uuid.substring(0, 10), 16);
            Long.parseLong(uuid.substring(10, 20), 16);
            Long.parseLong(uuid.substring(20), 16);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("UUID is not in hex format");
        }

        if (serviceName == null) {
            throw new NullPointerException("service name");
        }
    }

    public void run() {
        while (running) {
            try {
                StreamConnection conn = server.acceptAndOpen();
                if (running) listener.bluetoothConnected(conn);
            }
            catch (Exception e) {
                if (running) listener.bluetoothError(e);
            }
        }
    }
}