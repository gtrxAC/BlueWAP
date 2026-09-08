package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import java.util.*;
import fi.gtrxac.bluewap.Util;

/**
 * A ListScreen that can be embedded as part of other screens.
 * 
 * User input is handled with: public void keyEvent(int keyCode, int gameAction);
 * For touch support, pointerPressed/Dragged/Released should also be passed through as necessary.
 */
public class ListView extends ListScreen {
    private int viewX;
    private int viewY;
    private int width;
    private int height;

    public ListView(int margin, int itemPadding) {
        super(margin, itemPadding);
    }

    public ListView() {
        super();
    }

    public int getContainerWidth() {
        return width;
    }

    public int getContainerHeight() {
        return height;
    }

    public void setPosition(int x, int y, int width, int height) {
        viewX = x;
        viewY = y;
        this.width = width;
        this.height = height;
        needRecalc();
    }

    public void draw(Graphics g) {
        g.translate(viewX, viewY);
        g.setClip(0, 0, width, height);
        super.draw(g);
    }

    public void pointerPressed(int x, int y) {
        super.pointerPressed(x - viewX, y - viewY);
    }

    public void pointerDragged(int x, int y) {
        super.pointerDragged(x - viewX, y - viewY);
    }
    
    public void pointerReleased(int x, int y) {
        super.pointerReleased(x - viewX, y - viewY);
    }

    public void addCommand(Command c) {
        throw new RuntimeException();
    }

    public void removeCommand(Command c) {
        throw new RuntimeException();
    }
}