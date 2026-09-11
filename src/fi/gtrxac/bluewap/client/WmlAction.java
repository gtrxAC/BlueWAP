//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import java.util.*;

public class WmlAction {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_GO = 1;
    public static final int ACTION_PREV = 2;
    public static final int ACTION_REFRESH = 3;

    public int actionType;
    public String targetUrl;
    public Hashtable postfields;
    public Hashtable setvars;
    public boolean isPost;

    public WmlAction(int actionType, String targetUrl, Hashtable postfields, Hashtable setvars, boolean isPost) {
        this.actionType = actionType;
        this.targetUrl = targetUrl;
        this.postfields = postfields;
        this.setvars = setvars;
        this.isPost = isPost;
    }

    public void activate() {
        executeSetvars();

        switch (actionType) {
            case ACTION_GO: {
                History.visit(WmlVariables.parse(targetUrl, true), true, postfields, isPost);
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

    public void executeSetvars() {
        if (setvars == null) return;

		for (Enumeration e = setvars.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String value = (String) setvars.get(key);
			
            WmlVariables.set(WmlVariables.parse(key, false), WmlVariables.parse(value, false));
		}
    }
}
//#endif