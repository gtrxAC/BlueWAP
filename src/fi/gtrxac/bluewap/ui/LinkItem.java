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
    private int[] textLineWidths;
    private int maxStringWidth;

    private static Font font;
    private static int fontHeight;

    public LinkItem(String text) {
        super(true);
        this.text = text;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (highlighted) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, maxStringWidth, height);
            g.setColor(0x2244AA);
        } else {
            g.setColor(0x3355CC);
        }

        g.setFont(font);
        int y = 0;
        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], 0, y, 0);
            y += Fonts.underlinedHeight;

            if (!Util.useUnderlinedFont) {
                // Draw the underline manually
                g.drawLine(0, y - 1, textLineWidths[i], y - 1);
            }
        }

        if (highlighted) {
            drawHighlight(g, 0, 0, maxStringWidth, height);
        }
    }

    public void recalc(int width) {
        font = Util.useUnderlinedFont ? Fonts.underlined : Fonts.plain;
        fontHeight = font.getHeight();
        textLines = Util.wordWrap(text, width, font);
        height = fontHeight*textLines.length;

        if (!Util.useUnderlinedFont) {
            textLineWidths = new int[textLines.length];
        }

        maxStringWidth = 0;

        for (int i = 0; i < textLines.length; i++) {
            int stringWidth = font.stringWidth(textLines[i]);

            if (!Util.useUnderlinedFont) {
                textLineWidths[i] = stringWidth;
            }
            if (stringWidth > maxStringWidth) {
                maxStringWidth = stringWidth;
            }
        }
    }
}