package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A ListScreen item that visually resembles a hyperlink.
 */
public class LinkItem extends Item {
    public String text;

    private String[] textLines;
    private int maxStringWidth;

    public LinkItem(String text) {
        super(true);
        this.text = text;
    }

    public void draw(Graphics g, int width, boolean selected) {
        if (selected) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, maxStringWidth, height);
            g.setColor(0x2244AA);
        } else {
            g.setColor(0x3355CC);
        }

        if (textLines == null) recalc(width);
        
        g.setFont(Fonts.underlined);
        int y = 0;
        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], 0, y, 0);
            y += Fonts.underlinedHeight;
        }

        if (selected) {
            drawHighlight(g, 0, 0, maxStringWidth, height);
        }
    }

    public void recalc(int width) {
        textLines = Util.wordWrap(text, width, Fonts.underlined);
        height = Fonts.underlinedHeight*textLines.length;

        maxStringWidth = 0;

        for (int i = 0; i < textLines.length; i++) {
            int stringWidth = Fonts.underlined.stringWidth(textLines[i]);

            if (stringWidth > maxStringWidth) {
                maxStringWidth = stringWidth;
            }
        }
    }
}