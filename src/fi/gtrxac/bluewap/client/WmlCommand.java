package fi.gtrxac.bluewap.client;

import javax.microedition.lcdui.*;

public class WmlCommand extends Command {
    int action;
    String target;

    public WmlCommand(String label, int prio, int action, String target) {
        super(label, Command.SCREEN, prio);
        this.action = action;
        this.target = target;
    }
}