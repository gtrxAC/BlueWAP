package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.*;
import fi.gtrxac.bluewap.Util;

public class Dialog extends Screen implements CommandListener {
    private static final Command DISMISS_COMMAND = new Command("OK", Command.BACK, 0);

    private String text;
    private String[] textLines;
    private Screen lastScreen;
    private Screen nextScreen;

    private int commandCount;
    private CommandListener listener;

//#ifndef MIDP1
    private static Image overlay;
    private Screen behindScreen;
//#endif

    public Dialog(String text) {
        this(text, null);
    }

    public Dialog(String text, Screen nextScreen) {
        super(0);
        super.setCommandListener(this);

//#ifndef MIDP1
        checkInitOverlay();
//#endif

        lastScreen = AppBase.getCurrentScreen();
        this.nextScreen = nextScreen;

        commandCount = 0;
        super.addCommand(DISMISS_COMMAND);

        setText(text);

//#ifndef MIDP1
        // Get the screen that should be drawn behind this one
        // If this Dialog is stacked above another Dialog, get the last non-Dialog screen
        behindScreen = lastScreen;
        while (behindScreen instanceof Dialog) {
            behindScreen = ((Dialog) behindScreen).lastScreen;
        }
//#endif
    }

//#ifndef MIDP1
    private static void checkInitOverlay() {
        if (overlay == null && AppBase.disp.numAlphaLevels() > 2) {
            try {
                overlay = Image.createImage("/o.png");
            }
            catch (Exception e) {}
        }
    }
//#endif

    public int getContentWidth() {
        return Math.min(getWidth(), Fonts.height*20);
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        text = newText;
        needRecalc();
    }

    public void addCommand(Command c) {
        super.removeCommand(DISMISS_COMMAND);
        super.addCommand(c);
        commandCount++;
    }

    public void removeCommand(Command c) {
        super.removeCommand(c);
        commandCount--;
        if (commandCount == 0) {
            super.addCommand(DISMISS_COMMAND);
        }
    }

    public void recalc() {
        this.textLines = Util.wordWrap(text, getContentWidth() - Fonts.height*2, Fonts.plain);
    }

    public void draw(Graphics g) {
        int themeBg = 0xFFFFFF;

//#ifndef MIDP1
        // If possible, draw last screen behind a darkened overlay
        if (overlay != null && behindScreen instanceof Screen) {
            g.setColor(themeBg);
            g.fillRect(0, 0, getWidth(), getHeight());

            try {
                ((Screen) behindScreen).draw(g);
            }
            catch (Exception e) {}

            g.translate(-g.getTranslateX(), -g.getTranslateY());
            g.setClip(0, 0, getWidth(), getHeight());
    
            // Draw overlay (grid of 64×64 black square images with 70% opacity)
            for (int y = 0; y < getHeight(); y += 64) {
                for (int x = 0; x < getWidth(); x += 64) {
                    g.drawImage(overlay, x, y, Graphics.TOP | Graphics.LEFT);
                }
            }
        } else
//#endif
        {
            // Not possible to draw the next screen, or display doesn't support alpha blending:
            // fill background with darkened version of theme background color
            int background =
                (themeBg & 0xFF0000) >> 18 << 16
                | (themeBg & 0xFF00) >> 10 << 8
                | (themeBg & 0xFF) >> 2;

            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Centered card with actual theme background color
        int baseX = (getWidth() - getContentWidth())/2;
        int y = getHeight()/2 - textLines.length*Fonts.height/2;
        
        g.setColor(themeBg);
        g.fillRoundRect(
            baseX + Fonts.height/2,
            y - Fonts.height/2,
            getContentWidth() - Fonts.height,
            textLines.length*Fonts.height + Fonts.height,
            Fonts.height/2,
            Fonts.height/2
        );

        g.setFont(Fonts.plain);
        g.setColor(0x111111);
        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], baseX + Fonts.height, y, Graphics.TOP | Graphics.LEFT);
            y += Fonts.height;
        }
    }

    public void setCommandListener(CommandListener l) {
        listener = l;
    }

    public void commandAction(Command c, Displayable d) {
        if (listener == null) {
            if (nextScreen == null) {
                AppBase.popScreen();
            } else {
                AppBase.replaceScreen(nextScreen);
            }
        } else {
            listener.commandAction(c, d);
        }
    }
}