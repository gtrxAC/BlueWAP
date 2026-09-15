package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.CommandListener;
import java.util.*;

public class OptionsScreen extends Screen implements CommandListener, ListViewItemListener {
    private ListView listView;
    private int viewX;
    private int viewY;
    private int viewWidth;
    private int viewHeight;
    private Vector commands;

    public OptionsScreen(Vector commands) {
        super(0);
        setTitle(AppBase.getCurrentScreen().getTitle());

        listView = new ListView(Fonts.height/6, Fonts.height/6);
        listView.setItemListener(this);

        this.commands = createSortedCommands(commands);

        for (int i = 0; i < this.commands.size(); i++) {
            Command c = (Command) this.commands.elementAt(i);
            listView.addItem(new OptionItem(c));
        }

        addCommand(new Command("Back", Command.BACK, 0));
        setCommandListener(this);
    }

    // sort commands by increasing priority number into a separate vector
    // input vector is cleared
    private Vector createSortedCommands(Vector commands) {
        int numCommands = commands.size();
        Vector result = new Vector(numCommands);

        for (int i = 0; i < numCommands; i++) {
            Command lowestCommand = null;

            for (int j = 0; j < commands.size(); j++) {
                Command c = (Command) commands.elementAt(j);

                if (lowestCommand == null || c.getPriority() < lowestCommand.getPriority()) {
                    lowestCommand = c;
                }
            }
            result.addElement(lowestCommand);
            commands.removeElement(lowestCommand);
        }
        return result;
    }

    public void draw(Graphics g) {
        drawPreviousScreenDimmed(g);
        listView.prepareAndDraw(g);
    }

    public void recalc() {
        viewWidth = Math.min(Fonts.height*12, getWidth()*19/20);
        listView.setPosition(0, 0, viewWidth, 100);
        listView.fullRecalc();

        Item viewLastItem = (Item) listView.items.lastElement();

        int viewContentHeight = (viewLastItem == null) ? 0 :
            (viewLastItem.y + viewLastItem.height + listView.itemPadding*2);

        viewHeight = Math.min(getHeight()*4/5, viewContentHeight);
        viewX = (getWidth() - viewWidth)/2;
        viewY = getHeight() - viewHeight;

        listView.setPosition(viewX, viewY, viewWidth, viewHeight);
        listView.fullRecalc();
    }

    public void itemSelected(Item i) {
        AppBase.popScreen();
        CommandListener l = AppBase.getCurrentScreen().getCommandListener();

        if (l != null) {
            l.commandAction(((OptionItem) i).command, AppCanvas.instance);
        }
    }

    public void commandAction(Command c, Displayable d) {
        AppBase.popScreen();
    }

    public void keyEvent(int keyCode, int gameAction) {
        listView.keyEvent(keyCode, gameAction);
    }

    public void pointerPressed(int x, int y) {
        if (y < viewY || x < viewX) return;
        listView.pointerPressed(x, y);
    }

    public void pointerDragged(int x, int y) {
        if (y < viewY || x < viewX) return;
        listView.pointerDragged(x, y);
    }
    
    public void pointerReleased(int x, int y) {
        if (y < viewY || x < viewX) {
            AppBase.popScreen();
        }
        else {
            listView.pointerReleased(x, y);
        }
    }
}