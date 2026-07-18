package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import java.util.*;

/**
 * A list that can display a vertical scrollable list of Items.
 */
public abstract class ListScreen extends Screen {
    public int scroll;
    public int highlightedIndex;
    public int itemPadding;
    public Vector items;
    
    public ListScreen(int margin, int itemPadding) {
        super(margin);
        this.itemPadding = itemPadding;
        items = new Vector();
        scroll = -itemPadding;
    }

    public void draw(Graphics g) {
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        drawItems(g);
    }

    private void drawItems(Graphics g) {
        g.translate(getMargin(), -scroll);

        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            
            if (g.getTranslateY() + item.height > 0) {
                item.draw(g, getContentWidth(), highlightedIndex == i);
            }
            g.translate(0, item.height + itemPadding);

            if (g.getTranslateY() >= getHeight()) break;
        }
    }

    public void recalc() {
        int y = 0;
        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            item.recalc(getContentWidth());
            item.y = y;
            y += item.height + itemPadding;
        }
    }

    public void keyEvent(int keyCode, int gameAction) {
        if (items.size() == 0) return;

        int oldHighlightedIndex = highlightedIndex;

        switch (gameAction) {
            case Canvas.DOWN: {
                Item selected = (Item) items.elementAt(highlightedIndex);

                if (selected.height > getHeight() && selected.y + selected.height - scroll > getHeight()) {
                    // Item is taller than screen -> scroll down by two lines
                    scroll += Fonts.height*2;
                } else {
                    int newSel = getNextSelectableItem();
                    if (newSel != -1) highlightedIndex = newSel;
                }
                break;
            }
            case Canvas.UP: {
                Item selected = (Item) items.elementAt(highlightedIndex);

                if (selected.height > getHeight() && selected.y - scroll < 0) {
                    // Item is taller than screen -> scroll up by two lines
                    scroll -= Fonts.height*2;
                } else {
                    int newSel = getPreviousSelectableItem();
                    if (newSel != -1) highlightedIndex = newSel;
                }
                break;
            }
            case Canvas.FIRE: {
                selectItem();
                break;
            }
            case Canvas.RIGHT: {
                highlightedIndex = Math.min(highlightedIndex + 3, items.size() - 1);
                break;
            }
            case Canvas.LEFT: {
                highlightedIndex = Math.max(highlightedIndex - 3, 0);
                break;
            }
        }
        if (oldHighlightedIndex != highlightedIndex) {
            makeSelectedItemVisible();
        }
    }

    private boolean itemIsSelectable(int index) {
        Item item = (Item) items.elementAt(index);
        Item selected = (Item) items.elementAt(highlightedIndex);

        // if gap between items is more than the screen height, don't allow jumping to
        // this item because then the contents of some in-between items might never be seen
        if (Math.abs(item.y - selected.y) > getHeight()) return false;

        return (item.isSelectable() || item.height >= getHeight());
    }

    private int getPreviousSelectableItem() {
        for (int i = highlightedIndex - 1; i >= 0; i--) {
            if (itemIsSelectable(i)) return i;
        }
        // no suitable selectable item found - scroll to the previous item
        return Math.max(0, highlightedIndex - 1);
    }

    private int getNextSelectableItem() {
        for (int i = highlightedIndex + 1; i < items.size(); i++) {
            if (itemIsSelectable(i)) return i;
        }
        // no suitable selectable item found - scroll to the next item or bottom-most visible item even if unselectable
        int bottommostVisible = highlightedIndex + 1;
        
        for (int i = bottommostVisible; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            if (item.y + item.height - scroll <= getHeight()) bottommostVisible = i; 
        }
        return Math.min(items.size() - 1, bottommostVisible);
    }

    // from discord j2me, modified
    private void makeSelectedItemVisible() {
        if (items.size() == 0) return;

        recalcIfNeeded();  // calculate item heights

        Item selected = (Item) items.elementAt(highlightedIndex);
        int itemPos = selected.y - scroll;

        if (selected.height > getHeight()) {
            // For items taller than the screen, make sure one screenful of it is visible:
            // Check if item is above the visible area
            if (itemPos + selected.height < 0) {
                scroll -= getHeight() + itemPadding;
            }
            // Check if below the visible area
            else if (itemPos > getHeight()) {
                scroll += getHeight() + itemPadding;
            }
        } else {
            // For shorter items, make sure the entire item is visible:
            // Check if item is above the visible area
            if (itemPos < 0) {
                scroll += itemPos - itemPadding;
            }
            // Check if below the visible area
            else if (itemPos + selected.height > getHeight()) {
                scroll += (itemPos + selected.height) - getHeight() + itemPadding;
            }
        }
    }

    public void addItem(Item i) {
        items.addElement(i);
        needRecalc();
    }

    public void addItem(String str) {
        addItem(new StringItem(str));
    }

    public void removeItem(int index) {
        items.removeElementAt(index);
        needRecalc();
    }

    public void removeItem(Item i) {
        int index = items.indexOf(i);
        if (index == -1) return;
        removeItem(index);
    }

    /**
     * Remove all items from this screen.
     */
    public void removeAllItems() {
        highlightedIndex = 0;
        scroll = -itemPadding;
        items.setSize(0);
        needRecalc();
    }

    /**
     * Get the amount of items currently contained in this screen.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Called when an item in this screen is selected.
     */
    protected void itemSelected(Item i) {}

    /**
     * Select the currently highlighted item.
     */
    public void selectItem() {
        Item selected = (Item) items.elementAt(highlightedIndex);
        selected.itemSelected();
        itemSelected(selected);
    }

    /**
     * Set the currently highlighted item and scroll to it.
     */
    public void setHighlightedItem(Item item) {
        int index = items.indexOf(item);
        if (index == -1) return;
        setHighlightedItem(index);
    }

    public void setHighlightedItem(int index) {
        if (index < 0 || index > getItemCount()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        highlightedIndex = index;
        makeSelectedItemVisible();
        AppBase.repaint();
    }
}