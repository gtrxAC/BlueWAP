//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import java.util.*;

public class App extends AppBase {
    static final String WML_BEGIN =
        "<?xml version=\"1.0\" encoding='utf-8'?>" +
        "<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">" +
        "<wml>" +
        "<head>" +
        "</head>";

    static final String WML_END = 
        "</card>" +
        "</wml>";

    static final String LOADING_WML =
        WML_BEGIN +
        "<card title=\"Loading\">" +
        "<p>Loading...</p>" +
        WML_END;

    static final String ERROR_WML_PREFIX =
        WML_BEGIN +
        "<card title=\"Error\">" +
        "<p>An error occurred:</p>" +
        "<p>";

    static final String ERROR_WML_SUFFIX =
        "</p>" +
        WML_END;

    public void init() {
        pushScreen(MainScreen.instance);
        History.visit("jar://home.wml", false);
    }
}
//#endif