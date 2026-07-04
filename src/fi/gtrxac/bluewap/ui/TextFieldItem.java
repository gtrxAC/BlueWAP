package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;

/**
 * A ListScreen item that contains and handles text input from the user.
 * When the item is selected, text can be entered via LCDUI TextBox.
 */
public class TextFieldItem extends Item implements CommandListener {
    private String title;
    private String value;
    private int maxSize;
    private int constraints;

    public TextFieldItem(String title, String value, int maxSize, int constraints) {
        super(true);
        this.title = title;
        this.value = (value != null) ? value : "";
        this.maxSize = maxSize;
        this.constraints = constraints;
    }

    public void draw(Graphics g, int width, boolean selected) {
        if (selected) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, width, height);
            drawHighlight(g, width);
            g.setColor(0x000000);
        } else {
            g.setColor(0x999999);
            g.drawRect(0, 0, width - 1, height - 1);
            g.setColor(0x222222);
        }

        g.setFont(smallFont);
        g.drawString(
            value,
            smallFont.getHeight()/6,
            (height - smallFont.getHeight())/2,
            0
        );
    }

    public void sizeChanged(int width) {
        height = smallFont.getHeight()*4/3;
    }

    public void itemSelected() {
        TextBox t = new TextBox(title, value, maxSize, constraints);
        t.addCommand(new Command("OK", Command.OK, 0));
        t.addCommand(new Command("Cancel", Command.BACK, 1));
        t.setCommandListener(this);
        AppBase.disp.setCurrent(t);
    }

    public void commandAction(Command c, Displayable d) {
        if (c.getPriority() == 0) {
            TextBox t = (TextBox) d;
            value = t.getString();
        }
        AppBase.disp.setCurrent(AppCanvas.instance);
    }

    /**
     * Get the text currently contained in this item.
     */
    public String getValue() {
        return value;
    }
}