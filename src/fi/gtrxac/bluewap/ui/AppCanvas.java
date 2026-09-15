package fi.gtrxac.bluewap.ui;

import java.util.*;
import javax.microedition.lcdui.*;
import fi.gtrxac.bluewap.Util;

/**
 * The canvas that shows and handles all screens managed by the UI framework.
 */
public class AppCanvas extends Canvas {
    public static final AppCanvas instance = new AppCanvas();
    private final Vector canvasCommands = new Vector();

    private String leftSoftkeyLabel;
    private String rightSoftkeyLabel;
    private Command leftSideCommand;
    private Command rightSideCommand;
    private boolean softkeyAreaPressed;
    private boolean leftSoftkeyPressed;
    private boolean rightSoftkeyPressed;
    private boolean optionsCommandShown;

    private String currentClock;
    private String currentTitle;

    private AppCanvas() {
        super();

        if (Util.useCustomSoftkeys) {
            setFullScreenMode(true);
            new ClockThread().start();
            updateClock();
        }
    }

    protected void paint(Graphics g) {
        Screen curr = AppBase.getCurrentScreen();
        
        if (Util.useCustomSoftkeys) {
            drawStatusBar(g);
            g.translate(0, getStatusBarHeight());
        }

        if (curr != null) {
            g.setClip(0, 0, getWidth(), getHeight());
            curr.prepareAndDraw(g);
        }

        if (Util.useCustomSoftkeys) {
            int softkeyBarY = getStatusBarHeight() + getHeight();
            g.translate(-g.getTranslateX(), -g.getTranslateY() + softkeyBarY);
            g.setClip(0, 0, getWidth(), getSoftkeyBarHeight());
            drawSoftkeys(g);
        }
    }

    private void drawStatusBar(Graphics g) {
        int barHeight = getStatusBarHeight();

        g.setColor(0xDDDDDD);
        g.fillRect(0, 0, getWidth(), barHeight);

        g.setColor(0x9B9B9B);
        g.drawLine(0, barHeight - 1, getWidth(), barHeight - 1);

        g.setColor(0x111111);
        g.setFont(Fonts.bold);
        g.drawString(
            currentTitle,
            Fonts.boldHeight/4, 
            (barHeight - Fonts.boldHeight)/2,
            0);

        g.setFont(Fonts.plain);
        g.drawString(
            currentClock,
            getWidth() - Fonts.boldHeight/4,
            (barHeight - Fonts.height)/2,
            Graphics.TOP | Graphics.RIGHT);
    }

    private void drawSoftkeys(Graphics g) {
        int barHeight = getSoftkeyBarHeight();

        g.setColor(0xEEEEEE);
        g.fillRect(0, 0, getWidth(), barHeight);

        int margin = Math.max(3, Fonts.boldHeight/7);
        int softkeyWidth = (getWidth() - margin*3)/2;
        int softkeyHeight = barHeight - margin*2;
        int textY = barHeight/2 - Fonts.boldHeight/2;
        int leftSoftkeyX = margin;
        int rightSoftkeyX = getWidth() - margin - softkeyWidth;

        g.setFont(Fonts.bold);
        drawSoftkey(g, leftSoftkeyLabel, leftSoftkeyX, margin, softkeyWidth, softkeyHeight, textY, leftSoftkeyPressed);
        drawSoftkey(g, rightSoftkeyLabel, rightSoftkeyX, margin, softkeyWidth, softkeyHeight, textY, rightSoftkeyPressed);
    }

    private void drawSoftkey(Graphics g, String label, int x, int y, int width, int height, int textY, boolean pressed) {
        if (label == null) return;

        g.setColor(pressed ? 0xBBBBBB : 0xDDDDDD);
        g.fillRect(x, y, width, height);

        // darkened border colors equal to Util.blend(fillRectColor, 0x000000, 7)
        g.setColor(pressed ? 0x838383 : 0x9B9B9B);
        g.drawRect(x, y, width, height);

        g.setColor(pressed ? 0x000000 : 0x111111);
        g.drawString(label, x + width/2, textY, Graphics.TOP | Graphics.HCENTER);
    }

    private static int getStatusBarHeight() {
        return Fonts.boldHeight + Fonts.boldHeight/4*2;
    }

    private static int getSoftkeyBarHeight() {
        return Fonts.boldHeight*2;
    }

    public static int getBaseY() {
        return Util.useCustomSoftkeys ? getStatusBarHeight() : 0;
    }

    public int getHeight() {
        if (!Util.useCustomSoftkeys) {
            return super.getHeight();
        }
        return super.getHeight() - getSoftkeyBarHeight() - getStatusBarHeight();
    }

    protected void sizeChanged(int w, int h) {
        AppBase.recalcAllScreens();
        updateTitle();
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

    protected void pointerPressed(int x, int y) {
        y -= getBaseY();
        if (handleSoftkeyPress(x, y, false)) return;

        Screen curr = AppBase.getCurrentScreen();
        if (curr != null) {
            curr.pointerPressed(x, y);
            AppBase.repaint();
        }
    }

    protected void pointerDragged(int x, int y) {
        if (softkeyAreaPressed) return;

        Screen curr = AppBase.getCurrentScreen();
        if (curr != null) {
            curr.pointerDragged(x, y - getBaseY());
            AppBase.repaint();
        }
    }

    protected void pointerReleased(int x, int y) {
        y -= getBaseY();
        if (softkeyAreaPressed && handleSoftkeyPress(x, y, true)) return;

        Screen curr = AppBase.getCurrentScreen();
        if (curr != null) {
            curr.pointerReleased(x, y);
            AppBase.repaint();
        }
    }

    protected boolean handleSoftkeyPress(int x, int y, boolean runCommand) {
        if (!Util.useCustomSoftkeys) return false;

        leftSoftkeyPressed = false;
        rightSoftkeyPressed = false;
        softkeyAreaPressed = (y >= getHeight());

        if (!softkeyAreaPressed) {
            AppBase.repaint();
            return false;
        }

        boolean pressedLeft = (x < getWidth()/2);

        if (runCommand) {
            if (pressedLeft && optionsCommandShown) {
                AppBase.pushScreen(new OptionsScreen(remainingCommands));
            }
            else {
                Command c = pressedLeft ? leftSideCommand : rightSideCommand;

                if (c != null) {
                    CommandListener l = AppBase.getCurrentScreen().getCommandListener();
                    l.commandAction(c, this);
                }
            }
        }
        else {
            if (pressedLeft) leftSoftkeyPressed = true;
            else rightSoftkeyPressed = true;
        }

        AppBase.repaint();
        return true;
    }

    void updateCommands() {
        Screen curr = AppBase.getCurrentScreen();

        // Remove canvas commands that do not belong to current screen
        for (int i = 0; i < canvasCommands.size(); ) {
            Command c = (Command) canvasCommands.elementAt(i);
            if (curr == null || curr.getCommands().indexOf(c) == -1) {
                if (!Util.useCustomSoftkeys) removeCommand(c);
                canvasCommands.removeElementAt(i);
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
                if (!Util.useCustomSoftkeys) addCommand(c);
                canvasCommands.addElement(c);
            }
        }

        if (Util.useCustomSoftkeys) assignCommandPositions();
    }

    private final Vector remainingCommands = new Vector();

    private void assignCommandPositions() {
        // reset
        leftSideCommand = null;
        rightSideCommand = null;
        leftSoftkeyLabel = null;
        rightSoftkeyLabel = null;
        optionsCommandShown = false;

        if (canvasCommands.size() == 0) return;

        remainingCommands.setSize(0);

        for (int i = 0; i < canvasCommands.size(); i++) {
            remainingCommands.addElement(canvasCommands.elementAt(i));
        }

        // right softkey goes to the command with lowest priority number (and with command type BACK if there are any)
        boolean seenBackCommand = false;

        for (int i = 0; i < remainingCommands.size(); i++) {
            Command c = (Command) remainingCommands.elementAt(i);
            boolean isBackCommand = (c.getCommandType() == Command.BACK);

            if (seenBackCommand && !isBackCommand) {
                continue;
            }
            if (rightSideCommand == null || c.getPriority() < rightSideCommand.getPriority() || (!seenBackCommand && isBackCommand)) {
                rightSideCommand = c;
                if (isBackCommand) seenBackCommand = true;
            }
        }

        if (rightSideCommand != null) {
            remainingCommands.removeElement(rightSideCommand);
            rightSoftkeyLabel = rightSideCommand.getLabel();
        }

        // if that was the only command, then we're done
        if (remainingCommands.size() == 0) return;

        // if there is one command remaining, then it goes to left softkey
        if (remainingCommands.size() == 1) {
            leftSideCommand = (Command) remainingCommands.elementAt(0);
            leftSoftkeyLabel = leftSideCommand.getLabel();
            return;
        }

        // if there's more commands, show them in an options menu
        optionsCommandShown = true;
        leftSoftkeyLabel = "Options";
    }

    public void updateTitle() {
        int availableWidth = getWidth() - Fonts.boldHeight/4*2 - Fonts.plain.stringWidth("88:88");

        String title = AppBase.getCurrentScreen().getTitle();
        currentTitle = Util.stringToWidth(title, Fonts.bold, availableWidth);
        setTitle(currentTitle);
    }

    public void updateClock() {
        Calendar now = Calendar.getInstance();
        currentClock = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);

        if (AppCanvas.instance != null) {
            AppBase.repaint();
        }
    }
}