//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import java.util.*;

public class App extends AppBase {
    public void init() {
        pushScreen(MainScreen.instance);
        History.visit("jar://home.wml", false);
    }
}
//#endif