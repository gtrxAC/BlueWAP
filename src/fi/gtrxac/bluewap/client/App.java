//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.Settings;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import java.util.*;

public class App extends AppBase implements CommandListener {
    private static final int CMD_YES = 0;
    private static final int CMD_NO = 1;

    public void init() {
        Fonts.loadFonts(Settings.fontSize);
        pushScreen(MainScreen.instance);
        History.visit("jar://home.wml", false, null, false);
    }

    public static void askQuit() {
        Dialog d = new Dialog("Are you sure you want to quit BlueWAP?");
        d.addCommand(new Command("Yes", Command.OK, CMD_YES));
        d.addCommand(new Command("No", Command.BACK, CMD_NO));
        d.setCommandListener((App) instance);
        instance.pushScreen(d);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getPriority() == CMD_YES) {
            notifyDestroyed();
        }
        else {
            popScreen();
        }
    }
}
//#endif