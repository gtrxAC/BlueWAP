//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.io.*;
import java.util.*;

public class App extends AppBase implements BluetoothListener {

    public void init() {
        pushScreen(new LogScreen());
    }

    public void btSearchCompleted(String[] deviceNames, String[] deviceURLs) {

    }

    public void btError(Exception e) {
        LogScreen.log(e.toString());
    }

    /**
     * Called from a separate thread
     * @param conn
     */
    public void btConnected(StreamConnection conn) {
        LogScreen.log("Device connected");
    }
}
//#endif