//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.io.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class LogScreen extends ListScreen implements CommandListener {
    public static final int CMD_QUIT = 0;
    public static final int CMD_DISCONNECT = 1;

    public static final LogScreen instance = new LogScreen();

    Vector lines = new Vector();

    public LogScreen() {
        super(2, 2);

        setCommandListener(this);
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));
        addCommand(new Command("Disconnect", Command.BACK, CMD_DISCONNECT));
    }

    public static void log(String item) {
        instance.addLogItem(item);
    }

    private void addLogItem(String item) {
        lines.addElement(item);
        while (lines.size() > 50) {
            lines.removeElementAt(0);
        }
        refresh();
    }

    private void refresh() {
        removeAllItems();

        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            StringItem lineItem = new StringItem(line);
            addItem(lineItem);
        }

        setHighlightedItem(lines.size() - 1);
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_QUIT: {
                App.instance.notifyDestroyed();
                break;
            }
            case CMD_DISCONNECT: {
                for (int i = 0; i < App.connections.size(); i++) {
                    StreamConnection sc = (StreamConnection) App.connections.elementAt(i);
                    InputStream is = (InputStream) App.iStreams.elementAt(i);
                    OutputStream os = (OutputStream) App.oStreams.elementAt(i);
                    try { sc.close(); } catch (Exception e) {}
                    try { is.close(); } catch (Exception e) {}
                    try { os.close(); } catch (Exception e) {}
                    log("Closed connection");
                }
                App.connections.setSize(0);
                App.iStreams.setSize(0);
                App.oStreams.setSize(0);
                log("Closed all connections");
                break;
            }
        }
    }

    protected void itemSelected(Item i) {
    }
}
//#endif