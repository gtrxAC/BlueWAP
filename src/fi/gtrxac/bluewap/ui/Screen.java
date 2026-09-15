package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * Represents any kind of screen that is handled by the UI framework and displayed by AppCanvas.
 */
public abstract class Screen implements CommandListener {
    private int width;
    private int height;
    protected int contentWidth;
    private int margin;
    private boolean needsRecalc;

    private final Vector commands = new Vector();
    private CommandListener commandListener;
    
    public Screen(int margin) {
        this.margin = margin;
        needRecalc();
    }

    public void prepareAndDraw(Graphics g) {
        if (needsRecalc) fullRecalc();
        draw(g);
    }

    public void fullRecalc() {
        width = getContainerWidth();
        height = getContainerHeight();
        contentWidth = width - margin*2;
        recalc();
        needsRecalc = false;
    }

    public int getContainerWidth() {
        return AppCanvas.instance.getWidth();
    }

    public int getContainerHeight() {
        return AppCanvas.instance.getHeight();
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

    /**
     * Called when a touchscreen press is detected.
     */
    public void pointerPressed(int x, int y) {}

    /**
     * Called when a touch pointer is dragged.
     */
    public void pointerDragged(int x, int y) {}

    /**
     * Called when a touch pointer is released.
     */
    public void pointerReleased(int x, int y) {}

    // _________________________________________________________________________
    //
    //  Drawing utilities
    // _________________________________________________________________________
    //

    private static Image overlay;
    private static boolean drawingDimmed = false;

    private static void checkInitOverlay() {
        if (overlay == null && AppBase.disp.numAlphaLevels() > 2) {
            try {
                overlay = Image.createImage("/o.png");
            }
            catch (Exception e) {}
        }
    }

    public void drawPreviousScreenDimmed(Graphics g) {
        // only allow this function to be called once per draw, to avoid dimming being stacked
        if (drawingDimmed) return;
        drawingDimmed = true;

        int themeBg = 0xFFFFFF;
        
        checkInitOverlay();

        Screen behindScreen = AppBase.getPreviousScreen();

        // If possible, draw last screen behind a darkened overlay
        if (overlay != null && behindScreen != null) {
            g.setColor(themeBg);
            g.fillRect(0, 0, getWidth(), getHeight());

            try {
                behindScreen.draw(g);
            }
            catch (Exception e) {}

            g.translate(-g.getTranslateX(), -g.getTranslateY() + AppCanvas.getBaseY());
            g.setClip(0, 0, getWidth(), getHeight());
    
            // Draw overlay (grid of 64×64 black square images with 70% opacity)
            for (int y = 0; y < getHeight(); y += 64) {
                for (int x = 0; x < getWidth(); x += 64) {
                    g.drawImage(overlay, x, y, Graphics.TOP | Graphics.LEFT);
                }
            }
        } else {
            // Not possible to draw the next screen, or display doesn't support alpha blending:
            // fill background with darkened version of theme background color
            int background =
                (themeBg & 0xFF0000) >> 18 << 16
                | (themeBg & 0xFF00) >> 10 << 8
                | (themeBg & 0xFF) >> 2;

            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        drawingDimmed = false;
    }
}