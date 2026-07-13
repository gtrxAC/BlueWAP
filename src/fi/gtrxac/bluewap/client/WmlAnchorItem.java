//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import java.util.*;
import fi.gtrxac.bluewap.ui.*;

public class WmlAnchorItem extends LinkItem {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_GO = 1;
    public static final int ACTION_PREV = 2;
    public static final int ACTION_REFRESH = 3;

    int action;
    String target;
    Hashtable postfields;
    Hashtable setvars;

    public WmlAnchorItem(String text, int action, String target, Hashtable postfields, Hashtable setvars) {
        super(text);
        this.action = action;
        this.target = target;
        this.postfields = postfields;
        this.setvars = setvars;
    }

    public static void activate(int action, String target, Hashtable postfields, Hashtable setvars) {
        executeSetvars(setvars);

        switch (action) {
            case ACTION_GO: {
                History.visit(target, true, postfields);
                break;
            }
            case ACTION_PREV: {
                History.back();
                break;
            }
            case ACTION_REFRESH: {
                History.getCurrent().refresh();
                break;
            }
        }
    }

    public static void executeSetvars(Hashtable setvars) {
        if (setvars == null) return;

		for (Enumeration e = setvars.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String value = (String) setvars.get(key);
			
            WmlVariables.set(WmlVariables.parse(key), WmlVariables.parse(value));
		}
    }
}
//#endif