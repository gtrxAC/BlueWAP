package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A ListScreen item that shows a basic string of text.
 */
public class StringItem extends Item {
    public String text;

    private Font definedFont;
    private Font font;
    private String[] textLines;

    public StringItem(String text) {
        this(text, null);
        textLines = new String[] { text };
    }

    public StringItem(String text, Font font) {
        super(false);
        this.text = text;
        this.definedFont = font;
    }

    public void draw(Graphics g, int width, boolean selected) {
        g.setFont(font);
        g.setColor(0x111111);
        int y = 0;

        if (textLines == null) sizeChanged(width);

        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], 0, y, 0);
            y += font.getHeight();
        }
    }

    public void sizeChanged(int width) {
        // Allow font size to change if font was not defined in constructor
        font = definedFont;
        if (font == null) font = Fonts.plain;

        textLines = Util.wordWrap(text, width, font);
        height = font.getHeight()*textLines.length;
    }
}