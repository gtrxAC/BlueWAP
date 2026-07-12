package fi.gtrxac.bluewap.ui;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public abstract class AppBase extends MIDlet {
    // _________________________________________________________________________
    //
    //  MIDlet lifecycle
    // _________________________________________________________________________
    //
	public static Display disp;
    public static AppBase instance;
    
    private boolean started = false;

    public AppBase() {
        instance = this;
    }

    public void startApp() {
        if (started) return;
        started = true;
        disp = Display.getDisplay(this);
        Fonts.loadFonts(Font.SIZE_SMALL);

        init();

        disp.setCurrent(AppCanvas.instance);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    // _________________________________________________________________________
    //
    //  Screen management
    // _________________________________________________________________________
    //
    private static Stack screens = new Stack();

    // _________________________________________________________________________
    //
    //  Public API
    // _________________________________________________________________________
    //

    /**
     * Implemented by the app to show the first screen of the app.
     */
    public abstract void init();

    /**
     * Add a new screen to the top of the screen stack and show it.
     */
    public static void pushScreen(Screen s) {
        screens.push(s);
        AppCanvas.instance.updateCommands();
        repaint();
    }

    /**
     * Close the screen from the top of the screen stack and go back to the previous screen. 
     */
    public static void popScreen() {
        screens.pop();
        AppCanvas.instance.updateCommands();
        repaint();
    }

    /**
     * Replace the currently shown screen with another screen.
     */
    public static void replaceScreen(Screen s) {
        screens.pop();
        pushScreen(s);
    }

    /**
     * Get the currently shown screen.
     */
    public static Screen getCurrentScreen() {
        if (screens.empty()) return null;
        return (Screen) screens.peek();
    }

    public static void resizeAllScreens() {
        for (int i = 0; i < screens.size(); i++) {
            Screen s = (Screen) screens.elementAt(i);
            s.needsResize = true;
        }
        repaint();
    }

    public static void repaint() {
        AppCanvas.instance.repaint();
    }
}