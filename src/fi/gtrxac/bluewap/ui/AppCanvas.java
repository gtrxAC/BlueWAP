package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;
import fi.gtrxac.bluewap.Util;

/**
 * The canvas that shows and handles all screens managed by the UI framework.
 */
public class AppCanvas extends Canvas {
    public static final AppCanvas instance = new AppCanvas();
    private final Vector canvasCommands = new Vector();

    private AppCanvas() {
        super();
    }

    protected void paint(Graphics g) {
        Screen curr = AppBase.getCurrentScreen();
        if (curr != null) curr.prepareAndDraw(g);
    }

    protected void sizeChanged(int w, int h) {
        AppBase.resizeAllScreens();
    }

    protected void keyPressed(int keyCode) {
        Screen curr = AppBase.getCurrentScreen();
        if (curr != null) {
            curr.keyEvent(keyCode, getGameAction(keyCode));
            AppBase.repaint();
        }
    }

    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    void updateCommands() {
        Screen curr = AppBase.getCurrentScreen();

        // Remove canvas commands that do not belong to current screen
        for (int i = 0; i < canvasCommands.size(); ) {
            Command c = (Command) canvasCommands.elementAt(i);
            if (curr == null || curr.getCommands().indexOf(c) == -1) {
                removeCommand(c);
                canvasCommands.removeElementAt(i);
                if (Util.isJ2MELoader) Util.sleep(20);
            }
            else i++;
        }

        if (curr == null) {
            setCommandListener(null);
            return;
        }

        if (curr.getCommandListener() != null) {
            setCommandListener(curr.getCommandListener());
        } else {
            setCommandListener(curr);
        }

        // Add current screen's commands that are not in canvas
        for (int i = 0; i < curr.getCommands().size(); i++) {
            Command c = (Command) curr.getCommands().elementAt(i);
            if (canvasCommands.indexOf(c) == -1) {
                addCommand(c);
                canvasCommands.addElement(c);
                if (Util.isJ2MELoader) Util.sleep(20);
            }
        }
    }

    // protected void keyReleased(int keyCode) {

    // }
}