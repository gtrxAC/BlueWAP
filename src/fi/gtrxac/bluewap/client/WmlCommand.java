//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import java.util.Hashtable;
import javax.microedition.lcdui.*;

public class WmlCommand extends Command {
    WmlAction action;

    public WmlCommand(String label, int prio, int actionType, String target, Hashtable postfields, Hashtable setvars, boolean isPost) {
        super(label, Command.SCREEN, prio);
        this.action = new WmlAction(actionType, target, postfields, setvars, isPost);
    }
}
//#endif