package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import java.util.*;
import fi.gtrxac.bluewap.*;

public class TabbedListScreen extends Screen {
    public int activeTabIndex;
    public Vector tabNames;
    public Vector truncatedTabNames;
    public Vector tabs;

    private static int tabWidth;
    private static int tabMargin;
    private static int tabBarHeight;

    public TabbedListScreen(int margin) {
        super(margin);
        activeTabIndex = -1;
        tabNames = new Vector();
        truncatedTabNames = new Vector();
        tabs = new Vector();
    }

    public TabbedListScreen() {
        this(0);
    }

    public void draw(Graphics g) {
        g.setColor(0xCCCCCC);
        g.fillRect(0, 0, getWidth(), tabBarHeight);

        drawTabs(g);

        ListView tab = getCurrentTab();
        if (tab != null) tab.prepareAndDraw(g);
    }

    private void drawTabs(Graphics g) {
        int outlineColor = Util.blend(0xDDDDDD, 0x000000, 7);
        int tabDrawWidth = tabWidth - tabMargin;

        for (int i = 0; i < tabs.size(); i++) {
            boolean selected = (i == activeTabIndex);
            String tabName = (String) truncatedTabNames.elementAt(i);

            int tabX = i*tabWidth + tabMargin;
            int tabY = selected ? tabMargin : Fonts.height/4;
            int tabHeight = tabBarHeight - tabY;

            g.setColor(selected ? 0xFFFFFF : 0xDDDDDD);
            g.fillRect(tabX, tabY, tabDrawWidth, tabHeight);

            // outline
            g.setColor(outlineColor);
            g.drawRect(tabX, tabY, tabDrawWidth, tabHeight + 1);

            g.setColor(selected ? 0x000000 : 0x111111);
            g.setFont(Fonts.plain);
            g.drawString(
                tabName,
                tabX + tabDrawWidth/2,
                tabY + tabHeight/2 - Fonts.height/2,
                Graphics.HCENTER | Graphics.TOP);
        }

        // bottom line before and after the currently selected tab
        int selectedTabBegin = tabMargin + tabWidth*activeTabIndex;
        int selectedTabEnd = selectedTabBegin + tabWidth - tabMargin;
        int lineY = tabBarHeight - 1;

        g.setColor(outlineColor);
        g.drawLine(0, lineY, selectedTabBegin, lineY);
        g.drawLine(selectedTabEnd, lineY, getWidth(), lineY);
    }

    public void recalc() {
        tabMargin = Fonts.height/7;
        tabWidth = (getWidth() - tabMargin)/tabs.size();
        tabBarHeight = Fonts.height*3/2;

        truncatedTabNames.setSize(0);

        for (int i = 0; i < tabs.size(); i++) {
            ListView tab = (ListView) tabs.elementAt(i);
            tab.setPosition(0, tabBarHeight, getWidth(), getHeight() - tabBarHeight);

            String tabName = (String) tabNames.elementAt(i);
            String truncatedName = Util.stringToWidth(tabName, Fonts.plain, tabWidth - tabMargin*2);
            truncatedTabNames.addElement(truncatedName);
        }
    }

    public void addTab(String name, ListView view) {
        synchronized (tabs) {
            tabNames.addElement(name);
            tabs.addElement(view);
            if (activeTabIndex == -1) activeTabIndex = 0;
        }
        needRecalc();
        AppBase.repaint();
    }

    public void removeTab(int index) {
        synchronized (tabs) {
            tabNames.removeElementAt(index);
            tabs.removeElementAt(index);
            if (activeTabIndex >= index) activeTabIndex--;
        }
        AppBase.repaint();
    }

    public ListView getCurrentTab() {
        if (activeTabIndex == -1 || activeTabIndex >= tabs.size()) return null;
        return (ListView) tabs.elementAt(activeTabIndex);
    }

    public void keyEvent(int keyCode, int gameAction) {
        if (gameAction == Canvas.LEFT) {
            activeTabIndex--;
            if (activeTabIndex < 0) activeTabIndex = tabs.size() - 1;
            AppBase.repaint();
        }
        else if (gameAction == Canvas.RIGHT) {
            activeTabIndex++;
            if (activeTabIndex >= tabs.size()) activeTabIndex = 0;
            AppBase.repaint();
        }
        else {
            // pass the event through to the tab's listview
            ListView tab = getCurrentTab();
            if (tab != null) tab.keyEvent(keyCode, gameAction);
        }
    }

    public void pointerPressed(int x, int y) {
        ListView tab = getCurrentTab();
        if (tab != null) tab.pointerPressed(x, y);
    }

    public void pointerDragged(int x, int y) {
        ListView tab = getCurrentTab();
        if (tab != null) tab.pointerDragged(x, y);
    }
    
    public void pointerReleased(int x, int y) {
        ListView tab = getCurrentTab();
        if (tab != null) tab.pointerReleased(x, y);
    }
}