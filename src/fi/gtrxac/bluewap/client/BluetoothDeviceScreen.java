//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class BluetoothDeviceScreen extends ListScreen implements BluetoothListener, CommandListener {
    private static final int CMD_BACK = 0;
    private static final int CMD_SELECT = 1;

    private ButtonItem searchButton = new ButtonItem("Search devices");
    private Vector deviceNames = new Vector();
    private Vector deviceUrls = new Vector();
    private Vector deviceItems = new Vector();
    private Bluetooth bluetooth;

    public BluetoothDeviceScreen() {
        super(2, 2);
        addItem(searchButton);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.SCREEN, CMD_SELECT));
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getPriority() == CMD_BACK) {
            App.popScreen();
        }
        else if (c.getPriority() == CMD_SELECT) {
            selectItem();
        }
    }

    protected void itemSelected(Item i) {
        if (i == searchButton) {
            searchDevices();
        }
        else {
            for (int j = 0; j < deviceItems.size(); j++) {
                if (deviceItems.elementAt(j) == i) {
                    BluetoothHTTP.selectedConnectionUrl = (String) deviceUrls.elementAt(j);
                    App.popScreen();
                    break;
                }
            }
        }
    }

    private void clearAndRefresh() {
        deviceNames.removeAllElements();
        deviceUrls.removeAllElements();
        deviceItems.removeAllElements();
        removeAllItems();
        addItem(searchButton);
    }

    private void searchDevices() {
        clearAndRefresh();
        addItem(new StringItem("Searching..."));

        bluetooth = new Bluetooth(Config.BLUETOOTH_UUID, Config.BLUETOOTH_SERVICE, this);
        bluetooth.search();
    }

    public void btSearchCompleted(String[] names, String[] urls) {
        clearAndRefresh();

        if (names.length == 0) {
            addItem(new StringItem("No devices found. Make sure the server device is set to visible, then try again."));
            return;
        }
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && names[i].length() > 0) {
                deviceNames.addElement(names[i]);
                deviceUrls.addElement(urls[i]);
                ButtonItem deviceItem = new ButtonItem(names[i]);
                deviceItems.addElement(deviceItem);
                addItem(deviceItem);
            }
        }
    }

    public void btError(Exception e) {
        e.printStackTrace();
        clearAndRefresh();
        addItem(new StringItem("An error occurred:"));
        addItem(new StringItem(e.toString()));
    }
}
//#endif