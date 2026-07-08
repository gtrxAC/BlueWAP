//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;

public class WmlAnchorItem extends LinkItem {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_GO = 1;
    public static final int ACTION_PREV = 2;
    public static final int ACTION_REFRESH = 3;

    int action;
    String target;

    public WmlAnchorItem(String text, int action, String target) {
        super(text);
        this.action = action;
        this.target = target;
    }

    public static void activate(int action, String target) {
        switch (action) {
            case ACTION_GO: {
                History.visit(target, true);
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
}
//#endif