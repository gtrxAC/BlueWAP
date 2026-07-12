//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class MenuScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;

    public static final boolean supportsBluetooth = Util.checkClass("javax.bluetooth.RemoteDevice");

    String url = History.getCurrent().url.toString(false);
    TextFieldItem urlField = new TextFieldItem("Address", url, 2000, 0);
    ButtonItem goButton = new ButtonItem("Go!");
    ButtonItem standardButton = new ButtonItem("Standard");
    ButtonItem bluetoothButton = new ButtonItem("Bluetooth");
    private int connectionMode = HTTP.CONNECTION_TYPE;

    public MenuScreen() {
        super(2, 2);

        addItem(new StringItem("Address:"));
        addItem(urlField);
        addItem(goButton);

        if (supportsBluetooth) {
            addItem(new StringItem("Connection mode:"));
            addItem(standardButton);
            addItem(bluetoothButton);
            updateConnectionButtons();
        } else {
            addItem(new StringItem("Connecting via cellular/Wi-Fi. This device does not support Java Bluetooth API."));
        }
            RadioButtonGroup g = new RadioButtonGroup();
            addItem(new StringItem("Connection mode:"));
            addItem(new RadioButtonItem(g, "Standard"));
            addItem(new RadioButtonItem(g, "Bluetoth tete test test test fhsdjkfhjkdsfh dsjfhjkdshf jhjfs djfhk"));
        addItem(new StringItem("History:"));

        for (int i = History.menuUrls.size() - 1; i >= 0; i--) {
            String url = (String) History.menuUrls.elementAt(i);
            addItem(new LinkItem(url));
        }

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                App.popScreen();
                break;
            }
            case CMD_SELECT: {
                selectItem();
                break;
            }
        }
    }

    protected void itemSelected(Item i) {
        if (i instanceof LinkItem) {
            visit(((LinkItem) i).text);
        }
        if (i == standardButton) {
            setConnectionMode(HTTP.CONNECTION_TYPE_STANDARD);
        }
        else if (i == bluetoothButton) {
            setConnectionMode(HTTP.CONNECTION_TYPE_BLUETOOTH);
            App.pushScreen(new BluetoothDeviceScreen());
        }
        else if (i == goButton) {
            visit(urlField.getValue());
        }
    }

    private void visit(String url) {
        if (connectionMode == HTTP.CONNECTION_TYPE_BLUETOOTH && BluetoothHTTP.selectedConnectionUrl == null) {
            App.pushScreen(new BluetoothDeviceScreen());
            return;
        }
        App.popScreen();
        History.visit(url, false);
    }

    private void setConnectionMode(int mode) {
        connectionMode = mode;
        HTTP.setConnectionType(mode);
        if (mode == HTTP.CONNECTION_TYPE_BLUETOOTH) {
            BluetoothHTTP.selectedConnectionUrl = null;
        }
        updateConnectionButtons();
    }

    private void updateConnectionButtons() {
        standardButton.text = connectionMode == HTTP.CONNECTION_TYPE_STANDARD ? "Standard (active)" : "Standard";
        bluetoothButton.text = connectionMode == HTTP.CONNECTION_TYPE_BLUETOOTH ? "Bluetooth (active)" : "Bluetooth";
        App.repaint();
    }
}
//#endif