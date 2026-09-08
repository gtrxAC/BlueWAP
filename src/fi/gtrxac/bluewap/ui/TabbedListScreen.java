package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import java.util.*;

public class TabbedListScreen extends Screen {
    public int activeTabIndex;
    public Vector tabNames;
    public Vector tabs;

    private int tabsHeight = 100;

    public TabbedListScreen(int margin) {
        super(margin);
        activeTabIndex = -1;
        tabNames = new Vector();
        tabs = new Vector();
    }

    public TabbedListScreen() {
        this(0);
    }

    public void draw(Graphics g) {
        g.setColor(0xFF0000);
        g.fillRect(0, 0, getWidth(), getHeight());

        ListView tab = getCurrentTab();
        if (tab != null) tab.prepareAndDraw(g);
    }

    public void recalc() {
        for (int i = 0; i < tabs.size(); i++) {
            ListView tab = (ListView) tabs.elementAt(i);
            tab.setPosition(0, tabsHeight, getWidth(), getHeight() - tabsHeight);
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
        ListView tab = getCurrentTab();
        if (tab != null) tab.keyEvent(keyCode, gameAction);
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