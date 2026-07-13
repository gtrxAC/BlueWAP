package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import java.util.*;

/**
 * A list that can display a vertical scrollable list of Items.
 */
public abstract class ListScreen extends Screen {
    public int scroll;
    public int selectedIndex;
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
        g.fillRect(0, 0, width, height);
        drawItems(g);
    }

    public void drawItems(Graphics g) {
        g.translate(margin, -scroll);

        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            
            if (g.getTranslateY() + item.height > 0) {
                item.draw(g, contentWidth, selectedIndex == i);
            }
            g.translate(0, item.height + itemPadding);

            if (g.getTranslateY() >= height) break;
        }
    }

    public void sizeChanged() {
        int y = 0;
        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            item.sizeChanged(contentWidth);
            item.y = y;
            y += item.height + itemPadding;
        }
    }

    public void keyEvent(int keyCode, int gameAction) {
        int oldSelectedIndex = selectedIndex;

        switch (gameAction) {
            case Canvas.DOWN: {
                Item selected = (Item) items.elementAt(selectedIndex);

                if (selected.height > height && selected.y + selected.height - scroll > height) {
                    // Item is taller than screen -> scroll down by two lines
                    scroll += Fonts.height*2;
                } else {
                    int newSel = getNextSelectableItem();
                    if (newSel != -1) selectedIndex = newSel;
                }
                break;
            }
            case Canvas.UP: {
                Item selected = (Item) items.elementAt(selectedIndex);

                if (selected.height > height && selected.y - scroll < 0) {
                    // Item is taller than screen -> scroll up by two lines
                    scroll -= Fonts.height*2;
                } else {
                    int newSel = getPreviousSelectableItem();
                    if (newSel != -1) selectedIndex = newSel;
                }
                break;
            }
            case Canvas.FIRE: {
                selectItem();
                break;
            }
            case Canvas.RIGHT: {
                selectedIndex = Math.min(selectedIndex + 3, items.size() - 1);
                break;
            }
            case Canvas.LEFT: {
                selectedIndex = Math.max(selectedIndex - 3, 0);
                break;
            }
        }
        if (oldSelectedIndex != selectedIndex) {
            makeSelectedItemVisible();
        }
    }

    private boolean itemIsSelectable(int index) {
        Item item = (Item) items.elementAt(index);
        Item selected = (Item) items.elementAt(selectedIndex);

        // if gap between items is more than the screen height, don't allow jumping to
        // this item because then the contents of some in-between items might never be seen
        if (Math.abs(item.y - selected.y) > height) return false;

        return (item.isSelectable() || item.height >= height);
    }

    private int getPreviousSelectableItem() {
        for (int i = selectedIndex - 1; i >= 0; i--) {
            if (itemIsSelectable(i)) return i;
        }
        // no suitable selectable item found - scroll to the previous item
        return Math.max(0, selectedIndex - 1);
    }

    private int getNextSelectableItem() {
        for (int i = selectedIndex + 1; i < items.size(); i++) {
            if (itemIsSelectable(i)) return i;
        }
        // no suitable selectable item found - scroll to the next item or bottom-most visible item even if unselectable
        int bottommostVisible = selectedIndex + 1;
        
        for (int i = bottommostVisible; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            if (item.y + item.height - scroll <= height) bottommostVisible = i; 
        }
        return Math.min(items.size() - 1, bottommostVisible);
    }

    // from discord j2me, modified
    private void makeSelectedItemVisible() {
        if (items.size() == 0) return;

        Item selected = (Item) items.elementAt(selectedIndex);
        int itemPos = selected.y - scroll;

        if (selected.height > height) {
            // For items taller than the screen, make sure one screenful of it is visible:
            // Check if item is above the visible area
            if (itemPos + selected.height < 0) {
                scroll -= height + itemPadding;
            }
            // Check if below the visible area
            else if (itemPos > height) {
                scroll += height + itemPadding;
            }
        } else {
            // For shorter items, make sure the entire item is visible:
            // Check if item is above the visible area
            if (itemPos < 0) {
                scroll += itemPos - itemPadding;
            }
            // Check if below the visible area
            else if (itemPos + selected.height > height) {
                scroll += (itemPos + selected.height) - height + itemPadding;
            }
        }
    }

    public void addItem(Item i) {
        items.addElement(i);
        needsResize = true;
        AppBase.repaint();
    }

    public void addItem(String str) {
        addItem(new StringItem(str));
    }

    public void removeAllItems() {
        selectedIndex = 0;
        scroll = -itemPadding;
        items.setSize(0);
        needsResize = true;
        AppBase.repaint();
    }

    protected void itemSelected(Item i) {}

    public void selectItem() {
        Item selected = (Item) items.elementAt(selectedIndex);
        selected.itemSelected();
        itemSelected(selected);
    }

    public void setHighlightedItem(Item item) {
        int index = items.indexOf(item);
        if (index == -1) return;
        setHighlightedItem(index);
    }

    public void setHighlightedItem(int index) {
        selectedIndex = index;
        resizeIfNeeded();
        makeSelectedItemVisible();
        AppBase.repaint();
    }
}