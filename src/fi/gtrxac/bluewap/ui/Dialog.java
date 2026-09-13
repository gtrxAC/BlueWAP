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

    public Dialog(String text) {
        this(text, null);
    }

    public Dialog(String text, Screen nextScreen) {
        super(0);
        super.setCommandListener(this);

        lastScreen = AppBase.getCurrentScreen();
        this.nextScreen = nextScreen;

        commandCount = 0;
        super.addCommand(DISMISS_COMMAND);

        setText(text);
    }

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

        drawPreviousScreenDimmed(g);

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