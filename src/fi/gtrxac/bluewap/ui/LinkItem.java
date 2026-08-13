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
    private int align;
    private int x;

    private static Font font;
    private static int fontHeight;

    public LinkItem(String text) {
        this(text, Graphics.TOP | Graphics.LEFT);
    }

    public LinkItem(String text, int align) {
        super(true);
        this.text = text;
        this.align = align;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        int beginX = getLineX(maxStringWidth);

        if (highlighted) {
            g.setColor(0xEEF8FF);
            g.fillRect(beginX, 0, maxStringWidth, height);
            g.setColor(0x2244AA);
        } else {
            g.setColor(0x3355CC);
        }

        g.setFont(font);
        int y = 0;
        for (int i = 0; i < textLines.length; i++) {
            // calculate text alignment manually as it's also needed for the highlight (and underline on some devices)
            int lineX = getLineX(textLineWidths[i]);
            g.drawString(textLines[i], lineX, y, 0);
            y += Fonts.underlinedHeight;

            if (!Util.useUnderlinedFont) {
                // Draw the underline manually
                g.drawLine(lineX, y - 1, lineX + textLineWidths[i], y - 1);
            }
        }

        if (highlighted) {
            drawHighlight(g, beginX, 0, maxStringWidth, height);
        }
    }

    protected int getLineX(int lineWidth) {
        return x - getAlignedX(align, lineWidth);
    }

    public void recalc(int width) {
        font = Util.useUnderlinedFont ? Fonts.underlined : Fonts.plain;
        fontHeight = font.getHeight();
        textLines = Util.wordWrap(text, width, font);
        height = fontHeight*textLines.length;

        textLineWidths = new int[textLines.length];

        maxStringWidth = 0;

        for (int i = 0; i < textLines.length; i++) {
            int stringWidth = font.stringWidth(textLines[i]);

            textLineWidths[i] = stringWidth;

            if (stringWidth > maxStringWidth) {
                maxStringWidth = stringWidth;
            }
        }

        x = getAlignedX(align, width);
    }
}