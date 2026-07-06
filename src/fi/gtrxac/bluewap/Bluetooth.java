package fi.gtrxac.bluewap;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;

/**
 * Basic wrapper for Java Bluetooth API.
 */
public class Bluetooth implements DiscoveryListener, Runnable {
    private UUID uuid;
	private UUID[] uuids;
	private DiscoveryAgent discAgent;
	private LocalDevice local;
	public String localName;
    private String serviceName;

	private Vector discovered;
	private Vector serviceRecords;
	private int searchedDevices;
	private boolean searching;

    private boolean serverRunning;
    private StreamConnectionNotifier server;

    private BluetoothListener listener;

    /**
     * Create a new Bluetooth manager instance.
     * @param uuid 32-character string containing the UUID to use for identifying this application.
     * This should be unique for this app, for example generated with uuidgen (with the dashes removed)
     * @param serviceName Name to use for identifying this application's service
     * @param listener Listener to use for Bluetooth event callbacks
     */
	public Bluetooth(String uuid, String serviceName, BluetoothListener listener) {
        this.uuid = new UUID(uuid, false);
        this.serviceName = serviceName;
        this.listener = listener;
        uuids = new UUID[] {this.uuid};
	}

//#ifdef BLUETOOTH_SERVER
    /**
     * Start listening for connections from other devices.
     * When a connection is established, the app's btConnected callback is called.
     */
    public void listen() {
        try {
            init();
            String url = "btspp://localhost:" + uuid + ";name=" + serviceName + ";authenticate=false;encrypt=false";
            server = (StreamConnectionNotifier) Connector.open(url);
            serverRunning = true;
            new Thread(this).start();
        }
        catch (Exception e) {
            listener.btError(e);
        }
    }
//#endif

//#ifdef BLUETOOTH_SERVER
    /**
     * Stop listening for connections from other devices.
     * After this is called, the app is no longer notified by the btConnected callback.
     */
    public void stopListening() {
        try {
            server.close();
        }
        catch (Exception e) {}
        
        serverRunning = false;
    }
//#endif

    /**
     * Start searching for other devices that are running this application and are listening for connections.
     * The list of found devices is returned via the btSearchCompleted callback.
     */
    public void search() {
        if (searching) return;

        discovered = new Vector();
        serviceRecords = new Vector();
        searchedDevices = 0;

        try {
            // Start discovering devices
            init();
            discAgent.startInquiry(DiscoveryAgent.GIAC, this);
            searching = true;
        }
        catch (Exception e) {
            listener.btError(e);
        }
	}

    /**
     * Cancel a running search for other devices.
     */
	public void stopSearching() {
		if (!searching) return;
		discAgent.cancelInquiry(this);
		searching = false;
	}

    /**
     * END OF PUBLIC API
     */

    /**
     * Initialize local device settings if needed
     */
    private void init() throws Exception {
        if (local != null) return;
        local = LocalDevice.getLocalDevice();

        if (local == null) {
            throw new Exception("Cannot get local device. Make sure Bluetooth is supported and enabled.");
        }

//#ifdef BLUETOOTH_SERVER
        localName = local.getFriendlyName();
        if (localName == null) localName = "(unknown)";
//#endif

        discAgent = local.getDiscoveryAgent();

//#ifdef BLUETOOTH_SERVER
        local.setDiscoverable(DiscoveryAgent.GIAC);
//#endif
    }

    public void run() {
//#ifdef BLUETOOTH_SERVER
        while (serverRunning) {
            try {
                StreamConnection conn = server.acceptAndOpen();
                listener.btConnected(conn);
            }
            catch (Exception e) {
                listener.btError(e);
            }
        }
//#endif
    }

	public void deviceDiscovered(RemoteDevice device, DeviceClass cod) {
		discovered.addElement(device);
	}

    public void inquiryCompleted(int discType) {
        if (discovered.size() == 0) {
            // No devices found - call app's callback with empty arrays
            listener.btSearchCompleted(new String[]{}, new String[]{});
			searching = false;
            return;
        }
		try {
            // Found some devices - start searching the first device for services
            // The rest of the devices are searched afterwards in serviceSearchCompleted
			searchedDevices = 0;
			RemoteDevice device = (RemoteDevice) discovered.elementAt(searchedDevices);
			discAgent.searchServices(null, uuids, device, this);
		}
		catch (Exception e) {
            listener.btError(e);
		}
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        serviceRecords.addElement(servRecord[0]);
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        try {
            searchedDevices++;
            if (searchedDevices == discovered.size()) {
                // All devices have been searched -> get info about each device and call the app's callback
                int totalDevices = serviceRecords.size();
                String[] deviceNames = new String[totalDevices];
                String[] deviceURLs = new String[totalDevices];

                for (int i = 0; i < totalDevices; i++) {
                    ServiceRecord record = (ServiceRecord) serviceRecords.elementAt(i);
                    try {
                        deviceNames[i] = record.getHostDevice().getFriendlyName(false);
                    }
                    catch (Exception e) {
                        // deviceNames[i] = record.getHostDevice().getBluetoothAddress();
                        deviceNames[i] = "Device " + i;
                    }
                    deviceURLs[i] = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                }
                searching = false;
                discovered = null;
                serviceRecords = null;
                listener.btSearchCompleted(deviceNames, deviceURLs);
            } else {
                // Search the next device
                RemoteDevice device = (RemoteDevice) discovered.elementAt(searchedDevices);
                discAgent.searchServices(null, uuids, device, this);
            }
        }
        catch (Exception e) {
            listener.btError(e);
        }
    }
}