package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import java.util.*;
import fi.gtrxac.bluewap.Util;

/**
 * A list that can display a vertical scrollable list of Items.
 */
public abstract class ListScreen extends Screen implements Runnable {
    public int scroll;
    public int maxScroll;
    public int highlightedIndex;
    public int itemPadding;
    public Vector items;
    private String bannerText;

    private static int bannerHeight;
    private int scrollbarHandleHeight;
    
    public ListScreen(int margin, int itemPadding) {
        super(margin);
        this.itemPadding = itemPadding;
        items = new Vector();
        scroll = -itemPadding;
    }

    public ListScreen() {
        this(Math.max(2, Fonts.height/8), Math.max(2, Fonts.height/8));
    }

    public int getHeight() {
        if (bannerText == null) return super.getHeight();
        return super.getHeight() - bannerHeight;
    }

    public void draw(Graphics g) {
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, getWidth(), super.getHeight());

        if (bannerText != null) {
            drawBanner(g);
            g.translate(0, bannerHeight);
            g.setClip(0, 0, getWidth(), getHeight());
        }
        drawScrollbar(g);
        drawItems(g);
    }

    private void drawBanner(Graphics g) {
        g.setColor(0xDDDDDD);
        g.fillRect(0, 0, getWidth(), bannerHeight);

        g.setColor(0xAAAAAA);
        g.drawLine(0, bannerHeight, getWidth(), bannerHeight);

        g.setColor(0x000000);
        g.setFont(Fonts.plain);
        g.drawString(bannerText, getWidth()/2, Fonts.height/5, Graphics.TOP | Graphics.HCENTER);
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
                item.draw(g, this, contentWidth, highlightedIndex == i);
            }
            g.translate(0, item.height + itemPadding);

            if (g.getTranslateY() >= super.getHeight()) break;
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
        scrollbarHandleHeight = scrollbarHeight*(getHeight()*1000/scrollableHeight)/1000 - margin*2;
        int handleY = (scrollbarHeight - scrollbarHandleHeight)*(curScroll*1000/scrollRange)/1000 + margin;

        g.setColor(usingScrollBar ? 0x888888 : 0xCCCCCC);
        g.fillRect(x, handleY, scrollbarWidth, scrollbarHandleHeight);
    }

    public void recalc() {
        bannerHeight = Fonts.height + Fonts.height/5*2;

        recalcItems(0, true);

        if (maxScroll > 0) {
            // if screen is scrollable, make space for the scrollbar
            contentWidth -= Fonts.height/2 - getMargin()*2/3;
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
                Item highlighted = (Item) items.elementAt(highlightedIndex);

                if (highlighted.height > getHeight() && highlighted.y + highlighted.height - scroll > getHeight()) {
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
                Item highlighted = (Item) items.elementAt(highlightedIndex);

                if (highlighted.height > getHeight() && highlighted.y - scroll < 0) {
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
        Item highlighted = (Item) items.elementAt(highlightedIndex);

        // if gap between items is more than the screen height, don't allow jumping to
        // this item because then the contents of some in-between items might never be seen
        if (Math.abs(item.y - highlighted.y) > getHeight()) return false;

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

        Item highlighted = (Item) items.elementAt(highlightedIndex);
        int itemPos = highlighted.y - scroll;

        if (highlighted.height > getHeight()) {
            // For items taller than the screen, make sure one screenful of it is visible:
            // Check if item is above the visible area
            if (itemPos + highlighted.height < 0) {
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
            else if (itemPos + highlighted.height > getHeight()) {
                scroll += (itemPos + highlighted.height) - getHeight() + itemPadding;
            }
        }
    }

    // _________________________________________________________________________
    //
    //  Touch handling and kinetic scrolling
    // _________________________________________________________________________
    //

    private int totalScroll;
    private int totalScrollAbs;

    public static int scrollUnit;
    protected int velocity;
    private long lastPointerTime;
    private int lastPointerY;

    public boolean usingScrollBar;
    private int lastScrollBarY;
    private boolean pressedOnBlank;
    
    protected int getMinScroll() {
        return -itemPadding;
    }

    protected int getMaxScroll() {
        return maxScroll;
    }

    protected void checkScrollInRange() {
        scroll = Math.min(Math.max(scroll, getMinScroll()), getMaxScroll());
    }

    private boolean isScrollable() {
        return getMaxScroll() - getMinScroll() > 0;
    }

    protected boolean pointerWasTapped() {
        return totalScrollAbs < Fonts.height/2 && Math.abs(totalScroll) < Fonts.height/4;
    }

    private void handleScrollBar(int y) {
        lastScrollBarY = y;
        int height = getHeight() - scrollbarHandleHeight;
        y = Math.max(Math.min(y, getHeight() - scrollbarHandleHeight/2), scrollbarHandleHeight/2);
        y -= scrollbarHandleHeight/2;
        int ratio = y*1000/height;

        scroll = (getMaxScroll() - getMinScroll())*ratio;
        // if (scroll%1000 >= 500) scroll += 500;
        scroll = scroll/1000 + getMinScroll();
        checkScrollInRange();
        AppBase.repaint();
    }

    public void pointerPressed(int x, int y) {
        if (bannerText != null) y -= bannerHeight;

        // Use scrollbar if the content is tall enough to be scrollable and the user pressed on the right edge of the screen
        // Note: Scrollbar hitbox is wider than the actual rendered scrollbar
        usingScrollBar = isScrollable() && x > super.getWidth() - Fonts.height*4/3;

        if (usingScrollBar) {
            velocity = 0;  // stop any kinetic scrolling
            totalScrollAbs = 65500;
            handleScrollBar(y);
            return;
        }
        lastPointerY = y;
        totalScroll = 0;
        totalScrollAbs = 0;

        velocity = 0;
        lastPointerTime = System.currentTimeMillis();

        int tappedItemIndex = getItemIndexAtY(y);
        if (tappedItemIndex != -1) highlightedIndex = tappedItemIndex;

        AppBase.repaint();
    }

    public void pointerDragged(int x, int y) {
        if (bannerText != null) y -= bannerHeight;

        // Scroll position fix on S40 touch (e.g. Asha 300)
        if (y > 65500) y = 0;
        
        if (usingScrollBar) {
            handleScrollBar(y);
            return;
        }
        int deltaY = y - lastPointerY;
        scroll -= deltaY;
        checkScrollInRange();

        // Keep track of velocity
        long currentTime = System.currentTimeMillis();
        int timeDelta = (int) (currentTime - lastPointerTime);
        if (timeDelta > 0) {
            velocity = deltaY * 1000 / timeDelta;  // Pixels per second
        }
        lastPointerY = y;
        lastPointerTime = currentTime;
        totalScroll += deltaY;
        totalScrollAbs += Math.abs(deltaY);
        AppBase.repaint();
    }
    
    public void pointerReleased(int x, int y) {
        if (bannerText != null) y -= bannerHeight;

        if (usingScrollBar) {
            usingScrollBar = false;
            AppBase.repaint();
            return;
        }

        if (!pointerWasTapped()) {
            // scrolled -> start kinetic scrolling thread if finger was not held in place for too long
            if (System.currentTimeMillis() <= lastPointerTime + 110 && Math.abs(velocity) > Fonts.height*4) {
                new Thread(this).start();
            }
        } else {
            // Not scrolled: select item if pressed on one
            int tappedItemIndex = getItemIndexAtY(y);
            if (tappedItemIndex != -1) selectItem();
        }
        AppBase.repaint();
    }
    
    // Kinetic scrolling thread
    public void run() {
        int maxVel = Fonts.height*30;

        while (Math.abs(velocity) > 1) {
            velocity = Math.min(Math.max(velocity, -maxVel), maxVel);
            scroll -= velocity/30;
            velocity = velocity*19/20;
            checkScrollInRange();
            AppBase.repaint();
            Util.sleep(16);
        }
    }

    private int getItemIndexAtY(int y) {
        y += scroll;
        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.elementAt(i);
            if (y - item.y < item.height) return i;
        }
        return -1;
    }

    // _________________________________________________________________________
    //
    //  Public API
    // _________________________________________________________________________
    //

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
        if (!selected.isSelectable()) return;
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
        if (index < 0 || index >= getItemCount()) {
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

    /**
     * Get the banner text that is currently being shown; null if none shown
     */
    public String getBannerText() {
        return bannerText;
    }

    /**
     * Set the banner text that will be shown on the top of the screen.
     * Specify null to hide the banner text.
     */
    public void setBannerText(String newBannerText) {
        bannerText = newBannerText;
        needRecalc();
    }
}