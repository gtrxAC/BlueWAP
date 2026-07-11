package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Represents any kind of screen that is handled by the UI framework and displayed by AppCanvas.
 */
public abstract class Screen implements CommandListener {
    public int width;
    public int height;
    public int contentWidth;
    public int margin;  // if you change this, set needsResize = true
    public boolean needsResize;

    private final Vector commands = new Vector();
    private CommandListener commandListener;
    
    public Screen(int margin) {
        this.margin = margin;
        needsResize = true;
    }

    protected void resizeIfNeeded() {
        if (needsResize) {
            width = AppCanvas.instance.getWidth();
            height = AppCanvas.instance.getHeight();
            contentWidth = width - margin*2;
            sizeChanged();
            needsResize = false;
        }
    }

    public void prepareAndDraw(Graphics g) {
        resizeIfNeeded();
        draw(g);
    }

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

    public abstract void draw(Graphics g);

    public void sizeChanged() {}

    public void keyEvent(int keyCode, int gameAction) {}
}