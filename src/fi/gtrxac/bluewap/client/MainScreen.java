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

public class MainScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;
    public static final int CMD_MENU = 2;
    public static final int CMD_FORWARD = 3;
    public static final int CMD_REFRESH = 4;
    public static final int CMD_WARNINGS = 5;

    public static String warningsWml;
    public static final MainScreen instance = new MainScreen();

    public static final ButtonItem systemBrowserButton =
        new ButtonItem("Open in browser");

    public MainScreen() {
        super(2, 2);
        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Menu", Command.SCREEN, CMD_MENU));
        addCommand(new Command("Forward", Command.SCREEN, CMD_FORWARD));
        addCommand(new Command("Refresh", Command.SCREEN, CMD_REFRESH));
        addCommand(new Command("Warnings", Command.SCREEN, CMD_WARNINGS));
    }

    public void displayWml(String wml, String card, String contentType) {
        for (int i = 0; i < WmlParser.commands.size(); i++) {
            WmlCommand c = (WmlCommand) WmlParser.commands.elementAt(i);
            removeCommand(c);
        }
        WmlParser.displayWml(instance, wml, card, contentType);
    }

    public void commandAction(Command c, Displayable d) {
        if (c instanceof WmlCommand) {
            WmlCommand wc = (WmlCommand) c;
            WmlAnchorItem.activate(wc.action, wc.target);
            return;
        }
        switch (c.getPriority()) {
            case CMD_BACK: {
                History.back();
                break;
            }
            case CMD_SELECT: {
                selectItem();
                break;
            }
            case CMD_FORWARD: {
                History.forward();
                break;
            }
            case CMD_MENU: {
                App.pushScreen(new MenuScreen());
                break;
            }
            case CMD_REFRESH: {
                History.getCurrent().refresh();
                break;
            }
            case CMD_WARNINGS: {
                History.visit("warnings://", false);
                break;
            }
        }
    }

    protected void itemSelected(Item i) {
        if (i == systemBrowserButton) {
            try {
                if (App.instance.platformRequest(History.getCurrent().url.toString(false))) {
                    App.instance.notifyDestroyed();
                }
            }
            catch (Exception e) {
                addItem(e.toString());
            }
        }
        if (i instanceof WmlAnchorItem) {
            WmlAnchorItem anchor = (WmlAnchorItem) i;
            WmlAnchorItem.activate(anchor.action, anchor.target);
        }
    }
}
//#endif