//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import java.util.*;
import fi.gtrxac.bluewap.ui.*;

public class WmlAnchorItem extends LinkItem {
    String rawText;
    WmlAction action;

    public WmlAnchorItem(String text, int align, int actionType, String target, Hashtable postfields, Hashtable setvars, boolean isPost) {
        super(text, align);
        this.rawText = text;
        this.action = new WmlAction(actionType, target, postfields, setvars, isPost);
    }

    public void recalc(int width) {
        text = WmlVariables.parse(rawText, false);
        super.recalc(width);
    }
}
//#endif