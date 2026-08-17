package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import fi.gtrxac.bluewap.*;

public class RichTextItem extends Item {
    private Vector parts;
    private Vector calculatedParts;

    public RichTextItem() {
        super(false);
        parts = new Vector();
        calculatedParts = new Vector();
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        g.setColor(0x111111);

        for (int i = 0; i < calculatedParts.size(); i++) {
            RichTextPart part = (RichTextPart) calculatedParts.elementAt(i);
            
            if (part.needsRecalc) {
                needRecalc();
            }
            else {
                part.draw(g, highlighted);
            }
        }
    }

    public void recalc(int width) {
        calculatedParts.setSize(0);

        int x = 0;
        int y = 0;
        int currLineHeight = 0;

        for (int i = 0; i < parts.size(); i++) {
            RichTextPart part = (RichTextPart) parts.elementAt(i);
                System.out.println("'"+((RichTextStringPart)part).text+"' '" + width + "'");

            // 1. part is too wide to fit the screen -> split it into lines, add them, done
            if (part.width > width) {
                RichTextPart[] brokenParts = part.breakToWidth(width);

                y += currLineHeight;

                for (int j = 0; j < brokenParts.length; j++) {
                    brokenParts[j].x = 0;
                    brokenParts[j].y = y;
                    calculatedParts.addElement(brokenParts[j]);

                    if (j < brokenParts.length - 1) y += brokenParts[j].height;
                }

                if (brokenParts.length > 0) {
                    RichTextPart lastPart = brokenParts[brokenParts.length - 1];
                    x = lastPart.width;
                    currLineHeight = lastPart.height;
                }
                continue;
            }

            int availableWidth = width - x;

            // 2. part is too wide for the available width -> new line
            if (part.width > availableWidth) {
                x = 0;
                y += currLineHeight;
                currLineHeight = 0;

                // whitespace can be skipped at the start of a line
                if (part.isWhitespace()) continue;
            }

            // 3. place this part at the end of the current line
            part.x = x;
            part.y = y;
            calculatedParts.addElement(part);
            currLineHeight = Math.max(currLineHeight, part.height);
            x += part.width;
        }

        // height of the item is the height of all accumulated lines
        height = y + currLineHeight;
    }

    public void addPart(RichTextPart part) {
        if (part instanceof RichTextStringPart) {
            RichTextStringPart strPart = (RichTextStringPart) part;
            addStringPart(strPart.text, strPart.font);
        } else {
            parts.addElement(part);
            needRecalc();
        }
    }

    public void addStringPart(String text, Font font) {
        String remainingText = text;
        boolean prevCharWhitespace;
        boolean thisCharWhitespace = Util.charIsWhitespace(remainingText.charAt(0));
        int i = 1;

        while (i < remainingText.length()) {
            prevCharWhitespace = thisCharWhitespace;
            thisCharWhitespace = Util.charIsWhitespace(remainingText.charAt(i));

            if (prevCharWhitespace == thisCharWhitespace) {
                i++;
                continue;
            }

            String substr = remainingText.substring(0, i);
            RichTextStringPart newPart = new RichTextStringPart(substr, font);
            parts.addElement(newPart);
            remainingText = remainingText.substring(i);
            i = 1;
        }
        if (remainingText.length() > 0) {
            RichTextStringPart newPart = new RichTextStringPart(remainingText, font);
            parts.addElement(newPart);
        }
        needRecalc();
    }
}