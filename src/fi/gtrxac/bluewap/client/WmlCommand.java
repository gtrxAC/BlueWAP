package fi.gtrxac.bluewap.client;

import java.util.Hashtable;

import javax.microedition.lcdui.*;

public class WmlCommand extends Command {
    int action;
    String target;
    Hashtable postfields;
    Hashtable setvars;
    boolean isPost;

    public WmlCommand(String label, int prio, int action, String target, Hashtable postfields, Hashtable setvars, boolean isPost) {
        super(label, Command.SCREEN, prio);
        this.action = action;
        this.target = target;
        this.postfields = postfields;
        this.setvars = setvars;
        this.isPost = isPost;
    }
}