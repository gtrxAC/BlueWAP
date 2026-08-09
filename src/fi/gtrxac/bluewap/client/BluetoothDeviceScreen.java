//#ifdef BLUEWAP_CLIENT
//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.http.*;
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

    private RemoteDevice[] knownDevices;
    private boolean threadIsForKnownDevices;

    public BluetoothDeviceScreen() {
        super();
        addItem(searchButton);
        addItem(autoConnectButton);
        addItem(new BlankItem(Fonts.height/8));

        initClient();
        knownDevices = client.getKnownDevices();

        // start getting device names
        if (knownDevices.length > 0) {
            threadIsForKnownDevices = true;
            new Thread(this).start();
        }

        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.SCREEN, CMD_SELECT));
        setCommandListener(this);
    }

    public void run() {
        if (!threadIsForKnownDevices) {
            // Thread is for kinetic scrolling
            super.run();
            return;
        }
        threadIsForKnownDevices = false;

        StringItem loadingItem = new StringItem("Loading...");
        addItem(loadingItem);

        // Wait for the screen to show up
        while (App.getCurrentScreen() != this) {
            Util.sleep(10);
        }

        // Allow up to 2 seconds for the paired device names to be fetched
        // (may take especially long on Samsung; 15 sec for every device that can't be reached)
        long timeLimit = System.currentTimeMillis() + 2000;

        for (int i = 0; i < knownDevices.length; i++) {
            String name = knownDevices[i].getBluetoothAddress();

            if (Settings.getPairedDeviceNames && System.currentTimeMillis() < timeLimit) {
                try {
                    String friendlyName = knownDevices[i].getFriendlyName(false);

                    if (friendlyName != null && friendlyName.length() != 0) {
                        name = friendlyName;
                    }
                }
                catch (Exception e) {}
            }
            removeItem(loadingItem);
            addDeviceItem(name, knownDevices[i]);
        }

        // took too long -> don't fetch device names next time
        if (System.currentTimeMillis() > timeLimit) {
            Settings.getPairedDeviceNames = false;
            Settings.save();
        }
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
        int lastSel = getHighlightedIndex();

        devices.removeAllElements();
        deviceItems.removeAllElements();
        removeAllItems();
        addItem(searchButton);
        addItem(autoConnectButton);
        addItem(new BlankItem(Fonts.height/5));

        setHighlightedItem(Math.min(getItemCount() - 1, lastSel));
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
        ListItem item = new ListItem(name);
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
//#endif