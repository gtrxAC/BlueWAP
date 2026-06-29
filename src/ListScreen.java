import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import java.util.*;

public abstract class ListScreen extends Screen {
    public int scroll;
    public int selectedIndex;

    public int itemPadding;

    private Vector items;

    // private static final int MARGIN = Item.smallFont.getHeight()/7;  // left and right margin of the screen
    // private static final int PADDING = Item.smallFont.getHeight()/8;  // space between items
    
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
                    scroll += Item.smallFont.getHeight()*2;
                } else {
                    selectedIndex = Math.min(selectedIndex + 1, items.size() - 1);
                }
                break;
            }
            case Canvas.UP: {
                Item selected = (Item) items.elementAt(selectedIndex);

                if (selected.height > height && selected.y - scroll < 0) {
                    // Item is taller than screen -> scroll up by two lines
                    scroll -= Item.smallFont.getHeight()*2;
                } else {
                    selectedIndex = Math.max(selectedIndex - 1, 0);
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

    // from discord j2me, modified
    private void makeSelectedItemVisible() {
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
        App.repaint();
    }

    public void removeAllItems() {
        items.setSize(0);
        needsResize = true;
        App.repaint();
    }

    protected void itemSelected(Item i) {}

    public void selectItem() {
        Item selected = (Item) items.elementAt(selectedIndex);
        selected.itemSelected();
        itemSelected(selected);
    }
}