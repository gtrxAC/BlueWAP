//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.bt.*;
import fi.gtrxac.bluewap.ui.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.bluetooth.*;

public class BluetoothDeviceScreen extends ListScreen implements BluetoothClientListener, CommandListener {
    private static final int CMD_BACK = 0;
    private static final int CMD_SELECT = 1;

    private ButtonItem searchButton = new ButtonItem("Search devices");
    private ButtonItem autoConnectButton = new ButtonItem("Auto connect");
    private Vector devices = new Vector();
    private Vector deviceItems = new Vector();
    private BluetoothClient client;

    public BluetoothDeviceScreen() {
        super(2, 2);
        addItem(searchButton);
        addItem(autoConnectButton);

        initClient();
        RemoteDevice[] devices = client.getKnownDevices();

        for (int i = 0; i < devices.length; i++) {
            String name = null;
            try {
                name = devices[i].getFriendlyName(false);
            }
            catch (Exception e) {
                name = devices[i].getBluetoothAddress();
            }
            addDeviceItem(name, devices[i]);
        }

        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.SCREEN, CMD_SELECT));
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getPriority() == CMD_BACK) {
            if (client != null) {
                client.stopSearching();
            }
            App.popScreen();
        }
        else if (c.getPriority() == CMD_SELECT) {
            selectItem();
        }
    }

    protected void itemSelected(Item i) {
        if (i == searchButton) {
            searchDevices(false);
        }
        else if (i == autoConnectButton) {
            searchDevices(true);
        }
        else {
            addItem("Connecting...");

            int idx = deviceItems.indexOf(i);
            RemoteDevice dev = (RemoteDevice) devices.elementAt(idx);
            initClient();
            client.connect(dev);
        }
    }

    private void initClient() {
        if (client != null) return;
        client = new BluetoothClient(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);
    }

    private void clearAndRefresh() {
        devices.removeAllElements();
        deviceItems.removeAllElements();
        removeAllItems();
        addItem(searchButton);
        addItem(autoConnectButton);
    }

    private void searchDevices(boolean autoConnect) {
        initClient();
        if (client.isSearching()) return;

        clearAndRefresh();
        addItem("Searching...");

        if (autoConnect) client.autoConnect();
        else client.search();
    }

    private void addDeviceItem(String name, RemoteDevice device) {
        devices.addElement(device);
        ButtonItem item = new ButtonItem(name);
        deviceItems.addElement(item);
        addItem(item);
    }

    public void bluetoothDeviceFound(String name, RemoteDevice device, DeviceClass cod) {
        if (devices.size() == 0) {
            clearAndRefresh();
        }
        addDeviceItem(name, device);
    }

    public void bluetoothSearchCompleted() {
        if (devices.size() == 0) {
            clearAndRefresh();
            addItem("No devices found. Make sure the server device is set to visible, then try again.");
        } else {
            addItem("Search completed.");
        }
    }

    public void bluetoothSearchError(Exception e) {
        e.printStackTrace();
        addItem("An error occurred:");
        addItem(e.toString());
    }

    public void bluetoothConnected(String url) {
        BluetoothHTTP.selectedConnectionUrl = url;
        App.popScreen();
    }

    public void bluetoothConnectError(Exception e) {
        e.printStackTrace();
        addItem("An error occurred:");
        addItem(e.toString());
    }
}
//#endif