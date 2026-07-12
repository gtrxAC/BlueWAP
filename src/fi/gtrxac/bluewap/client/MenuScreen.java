//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class MenuScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;

    public static final boolean supportsBluetooth = Util.checkClass("javax.bluetooth.RemoteDevice");

    String url = History.getCurrent().url.toString(false);
    TextFieldItem urlField = new TextFieldItem("Address", url, 2000, 0);
    ButtonItem goButton = new ButtonItem("Go!");
    RadioButtonItem standardItem;
    RadioButtonItem bluetoothItem;
    private int connectionMode = HTTP.CONNECTION_TYPE;
    RadioButtonGroup fontSizeGroup;

    public MenuScreen() {
        super(2, 2);

        addItem("Address:");
        addItem(urlField);
        addItem(goButton);

        if (supportsBluetooth) {
            addItem("Connection mode:");
            RadioButtonGroup g = new RadioButtonGroup();
            addItem(standardItem = new RadioButtonItem(g, "Cellular/Wi-Fi"));
            addItem(bluetoothItem = new RadioButtonItem(g, "Bluetooth"));
            g.setTickedIndex(connectionMode);
        } else {
            addItem("Connecting via cellular/Wi-Fi. This device does not support Java Bluetooth API.");
        }

        addItem("Font size:");
        fontSizeGroup = new RadioButtonGroup();
        addItem(new RadioButtonItem(fontSizeGroup, "Small"));
        addItem(new RadioButtonItem(fontSizeGroup, "Medium"));
        addItem(new RadioButtonItem(fontSizeGroup, "Large"));

        int sizeIndex = Settings.fontSize == Font.SIZE_SMALL ? 0 :
            Settings.fontSize == Font.SIZE_MEDIUM ? 1 : 2;

        fontSizeGroup.setTickedIndex(sizeIndex);

        addItem("History:");

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
                int[] fontSizes = { Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE };
                Settings.fontSize = fontSizes[fontSizeGroup.getTickedIndex()];
                Settings.save();
                Fonts.loadFonts(Settings.fontSize);

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
        if (i == standardItem) {
            setConnectionMode(HTTP.CONNECTION_TYPE_STANDARD);
        }
        else if (i == bluetoothItem) {
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
    }
}
//#endif