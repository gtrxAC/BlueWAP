package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A ListScreen item that shows a basic string of text.
 */
public class StringItem extends Item {
    protected String text;
    
    private Font definedFont;
    private Font font;
    private String[] textLines;
    private int align;
    private int x;

    public StringItem(String text) {
        this(text, null);
    }

    public StringItem(String text, Font font) {
        this(text, font, Graphics.TOP | Graphics.LEFT);
    }

    public StringItem(String text, Font font, int align) {
        super(false);
        this.text = text;
        this.definedFont = font;
        this.align = align;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        g.setFont(font);
        g.setColor(0x111111);
        int y = 0;

        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], x, y, align);
            y += font.getHeight();
        }
    }

    public void recalc(int width) {
        // Allow font size to change if font was not defined in constructor
        font = definedFont;
        if (font == null) font = Fonts.plain;

        textLines = Util.wordWrap(text, width, font);
        height = font.getHeight()*textLines.length;

        x = ((align & Graphics.LEFT) != 0) ? 0 :
            ((align & Graphics.HCENTER) != 0) ? width/2 :
            width;
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        text = newText;
        needRecalc();
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int newAlign) {
        align = newAlign;
        needRecalc();
    }

    public int getX() {
        return x;
    }
}