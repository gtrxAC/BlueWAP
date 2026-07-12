package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Represents any item that can be shown in a ListScreen.
 */
public abstract class Item {
    public int height;
    public int y;
    private boolean selectable;

    protected Item(boolean selectable) {
        this.selectable = selectable;
    }

     /**
     * Draw this item at the current y position. The width of the screen is provided as a parameter.
     * Screen coordinates are automatically translated so (0, 0) is the top left of the item.
     * Do not use g.translate(), or if you do, undo your translations.
     * @param g Graphics context to draw on
     * @param width Screen width in pixels
     * @param selected Whether this item is currently selected
     */
    public abstract void draw(Graphics g, int width, boolean selected);

    /**
     * Called before this item is first drawn, and when the width of the screen changes.
     * This method should calculate the height of the item and assign a value to the 'height' field.
     * @param width Screen width in pixels
     */
    public abstract void sizeChanged(int width);

    public void itemSelected() {}

    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Light blue highlight which can be drawn by items to indicate that they are selected.
     * Note: Graphics current color is overwritten and not restored
     */
    public void drawHighlight(Graphics g, int x, int y, int width, int height) {
        // int lastColor = g.getColor();
        g.setColor(0x5599FF);
        g.drawRoundRect(x - 1, y - 1, width + 1, height + 1, 2, 2);
        g.drawRect(x, y, width - 1, height - 1);
        // g.setColor(lastColor);
    }

    /**
     * Draw highlight around entire item
     * Note: Graphics current color is overwritten and not restored
     */
    public void drawHighlight(Graphics g, int width) {
        drawHighlight(g, 0, 0, width, height);
    }
}