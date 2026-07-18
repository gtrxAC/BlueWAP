package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Represents any kind of screen that is handled by the UI framework and displayed by AppCanvas.
 */
public abstract class Screen implements CommandListener {
    private int width;
    private int height;
    private int contentWidth;
    private int margin;
    private boolean needsRecalc;

    private final Vector commands = new Vector();
    private CommandListener commandListener;
    
    public Screen(int margin) {
        this.margin = margin;
        needRecalc();
    }

    protected void recalcIfNeeded() {
        if (needsRecalc) {
            width = AppCanvas.instance.getWidth();
            height = AppCanvas.instance.getHeight();
            contentWidth = width - margin*2;
            recalc();
            needsRecalc = false;
        }
    }

    public void prepareAndDraw(Graphics g) {
        recalcIfNeeded();
        draw(g);
    }

    // _________________________________________________________________________
    //
    //  Command management
    // _________________________________________________________________________
    //

    public void addCommand(Command command) {
        if (command == null || commands.contains(command)) return;
        commands.addElement(command);
        updateCanvasCommands();
    }

    public void removeCommand(Command command) {
        if (command == null) return;
        commands.removeElement(command);
        updateCanvasCommands();
    }

    public Vector getCommands() {
        return commands;
    }

    public void setCommandListener(CommandListener listener) {
        commandListener = listener;
        updateCanvasCommands();
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (commandListener != null) {
            commandListener.commandAction(command, displayable);
        }
    }

    protected void updateCanvasCommands() {
        if (AppCanvas.instance != null) {
            AppCanvas.instance.updateCommands();
        }
    }

    // _________________________________________________________________________
    //
    //  Public API
    // _________________________________________________________________________
    //

    public void needRecalc() {
        needsRecalc = true;
        AppBase.repaint();
    }

    public int getWidth() {
        return width;
    }

    public int getContentWidth() {
        return contentWidth;
    }

    public int getHeight() {
        return height;
    }

    public int getMargin() {
        return margin;
    }

    // _________________________________________________________________________
    //
    //  Abstract and callbacks
    // _________________________________________________________________________
    //

    /**
     * Draws the content of this screen.
     */
    public abstract void draw(Graphics g);

    /**
     * Called when a recalculation of the screen's layout is needed, e.g. before
     * the first draw or when the screen is resized.
     */
    public void recalc() {}

    /**
     * Called when a key is pressed or repeated.
     */
    public void keyEvent(int keyCode, int gameAction) {}
}