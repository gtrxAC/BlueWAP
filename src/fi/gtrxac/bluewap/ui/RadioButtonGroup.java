package fi.gtrxac.bluewap.ui;

import java.util.Vector;

public class RadioButtonGroup {
    private Vector buttons;
    private int tickedIndex = -1;

    public RadioButtonGroup() {
        buttons = new Vector();
    }

    public void addItem(RadioButtonItem i) {
        buttons.addElement(i);
        if (tickedIndex == -1) setTickedIndex(0);
    }

    public void setTickedIndex(int index) {
        for (int i = 0; i < buttons.size(); i++) {
            RadioButtonItem btn = (RadioButtonItem) buttons.elementAt(i);
            btn.ticked = (i == index);
        }
        tickedIndex = index;
        AppBase.repaint();
    }

    public void setTicked(RadioButtonItem i) {
        int index = buttons.indexOf(i);
        if (index == -1) return;
        setTickedIndex(index);
    }

    public int getTickedIndex() {
        return tickedIndex;
    }

    public RadioButtonItem getTicked() {
        return (RadioButtonItem) buttons.elementAt(tickedIndex);
    }
}