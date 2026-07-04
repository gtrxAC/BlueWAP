package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;

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
        updateCommands();
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

        for (int i = 0; i < canvasCommands.size(); i++) {
            removeCommand((Command) canvasCommands.elementAt(i));
        }
        canvasCommands.removeAllElements();
        setCommandListener(null);

        if (curr == null) {
            return;
        }

        if (curr.getCommandListener() != null) {
            setCommandListener(curr.getCommandListener());
        } else {
            setCommandListener(curr);
        }

        for (int i = 0; i < curr.getCommands().size(); i++) {
            Command command = (Command) curr.getCommands().elementAt(i);
            addCommand(command);
            canvasCommands.addElement(command);
        }
    }

    // protected void keyReleased(int keyCode) {

    // }
}