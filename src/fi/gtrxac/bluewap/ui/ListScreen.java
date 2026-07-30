package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import java.util.*;

/**
 * A list that can display a vertical scrollable list of Items.
 */
public abstract class ListScreen extends Screen {
    public int scroll;
    public int maxScroll;
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
        drawScrollbar(g);
        drawItems(g);
    }

    private void drawItems(Graphics g) {
        g.translate(getMargin(), -scroll);

        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
        
            if (item.needsRecalc) {
                recalcItems(i, false);
            }
            if (g.getTranslateY() + item.height > 0) {
                // g.setColor(0xFF0000);
                // g.drawRect(0, 0, contentWidth, item.height);
                item.draw(g, contentWidth, highlightedIndex == i);
            }
            g.translate(0, item.height + itemPadding);

            if (g.getTranslateY() >= getHeight()) break;
        }
    }

    private void drawScrollbar(Graphics g) {
        if (maxScroll <= 0) return;

        int scrollbarAreaWidth = Fonts.height/2;
        int margin = Fonts.height/7;
        int scrollbarWidth = scrollbarAreaWidth - margin*2;
        int scrollbarHeight = getHeight() - margin*2;
        int x = getWidth() - scrollbarAreaWidth + margin;

        // +itemPadding because -itemPadding is the minimum scroll
        int curScroll = scroll + itemPadding;
        int scrollRange = maxScroll + itemPadding;
        int scrollableHeight = maxScroll + getHeight() + itemPadding;

        // graphics programming is fun,, trust me :D
        int handleHeight = scrollbarHeight*(getHeight()*1000/scrollableHeight)/1000 - margin*2;
        int handleY = (scrollbarHeight - handleHeight)*(curScroll*1000/scrollRange)/1000 + margin;

        g.setColor(0xCCCCCC);
        g.fillRect(x, handleY, scrollbarWidth, handleHeight);
    }

    public void recalc() {
        recalcItems(0, true);

        if (maxScroll > 0) {
            // if screen is scrollable, make space for the scrollbar
            contentWidth -= Fonts.height/2;
            recalcItems(0, true);
        }
        makeSelectedItemVisible();
    }

    private synchronized void recalcItems(int startIndex, boolean forceAll) {
        if (items.size() == 0) {
            maxScroll = scroll = -itemPadding;
            return;
        }

        int y = 0;
        if (startIndex > 0) {
            Item prevItem = (Item) items.elementAt(startIndex);
            y = prevItem.y;
        }

        for (int i = startIndex; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            if (forceAll || item.needsRecalc) {
                item.recalc(contentWidth);
                item.needsRecalc = false;
            }
            item.y = y;
            y += item.height + itemPadding;
        }

        maxScroll = Math.max(-itemPadding, y - getHeight());
        if (scroll > maxScroll) scroll = maxScroll;
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
                    if (scroll > maxScroll) scroll = maxScroll;
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
                    if (scroll < -itemPadding) scroll = -itemPadding;
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
            recalcItems(0, false);
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

    public synchronized void addItem(Item i) {
        items.addElement(i);
        needRecalc();
    }

    public void addItem(String str) {
        addItem(new StringItem(str));
    }

    public synchronized void removeItem(int index) {
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
    public synchronized void removeAllItems() {
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
        recalcItems(0, false);
        makeSelectedItemVisible();
        AppBase.repaint();
    }

    public int getHighlightedIndex() {
        return highlightedIndex;
    }
}