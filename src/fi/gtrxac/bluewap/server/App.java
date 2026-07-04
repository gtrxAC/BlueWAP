//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import java.util.*;

public class App extends AppBase {

    public void init() {
        pushScreen(new LogScreen());
    }
}
//#endif