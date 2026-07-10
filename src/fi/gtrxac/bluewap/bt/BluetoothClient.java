package fi.gtrxac.bluewap.bt;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;

public class BluetoothClient implements DiscoveryListener {
	private UUID[] uuids;
	private DiscoveryAgent discAgent;
	// private LocalDevice localDevice;
    private String serviceName;

	// private Vector discovered;
	// private Vector serviceRecords;
	// private int searchedDevices;
	private boolean searching;
	private boolean discoveringServices;

    private BluetoothClientListener listener;

    /**
     * Create a new Bluetooth manager instance.
     * Bluetooth applications are identified using an UUID string and a service name. These should be unique across different apps. The server and the client for the same app should share the same UUID and service name.
     * 
     * @param uuid 32-character string containing the UUID to use for identifying this application, for example generated with uuidgen (with the dashes removed)
     * @param serviceName Name to use for identifying this application's service
     * @param listener Listener to use for Bluetooth event callbacks
     */
	public BluetoothClient(String uuid, String serviceName, BluetoothClientListener listener) {
        this.uuids = new UUID[] { new UUID(uuid, false) };
        this.serviceName = serviceName;
        this.listener = listener;
	}

    /**
     * Get a list of previously known devices returned by the system (e.g. paired devices).
     */
    public RemoteDevice[] getKnownDevices() {
        try {
            setupDiscoveryAgent();
            RemoteDevice[] result = discAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
            if (result != null) return result;
        }
        catch (Exception e) {}

        return new RemoteDevice[]{}; 
    }

    /**
     * Start searching for other devices.
     * The bluetoothDeviceFound callback will be run for every device that was found.
     */
    public void search() {
        if (searching) return;

        try {
            setupDiscoveryAgent();
            discAgent.startInquiry(DiscoveryAgent.GIAC, this);
            searching = true;
        }
        catch (Exception e) {
            listener.bluetoothSearchError(e);
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
     * Attempt to connect to a device that was returned by getKnownDevices or the listener's bluetoothDeviceFound callback.
     * The other device must be running a BluetoothServer with the same service name and UUID.
     * When connected, the bluetoothConnected callback is called with an URL which can be used to open a StreamConnection.
     */
    public void connect(RemoteDevice device) {
        try {
            discoveringServices = true;
            discAgent.searchServices(null, uuids, device, this);
        }
        catch (Exception e) {
            listener.bluetoothConnectError(e);
        }
    }

    private void setupDiscoveryAgent() throws Exception {
        if (discAgent != null) return;
        LocalDevice local = LocalDevice.getLocalDevice();

        if (local == null) {
            throw new Exception("cannot get local device, make sure Bluetooth is supported and enabled");
        }
        discAgent = local.getDiscoveryAgent();
    }

	public void deviceDiscovered(RemoteDevice device, DeviceClass cod) {
        String name = null;
        try {
            name = device.getFriendlyName(false);
        }
        catch (Exception e) {
            name = device.getBluetoothAddress();
        }
		listener.bluetoothDeviceFound(name, device, cod);
	}

    public void inquiryCompleted(int discType) {
        searching = false;
        listener.bluetoothSearchCompleted();
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (servRecord.length == 0 || servRecord[0] == null) {
            discoveringServices = false;
            Exception e = new Exception("cannot connect, make sure the app is running on the other device");
            listener.bluetoothConnectError(e);
            return;
        }
        String url = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        listener.bluetoothConnected(url);
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        if (discoveringServices) {
            discoveringServices = false;

            if (respCode == SERVICE_SEARCH_DEVICE_NOT_REACHABLE) {
                Exception e = new Exception("device unreachable");
                listener.bluetoothConnectError(e);
            }
            else if (respCode != SERVICE_SEARCH_COMPLETED) {
                Exception e = new Exception("cannot connect, make sure the app is running on the other device");
                listener.bluetoothConnectError(e);
            }
        }
    }
}