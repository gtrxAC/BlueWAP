package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import fi.gtrxac.bluewap.*;

public class RichTextItem extends Item {
    private Vector parts;
    private RichTextPart[] calculatedParts;
    private int align;

    public RichTextItem(int align) {
        super(false);
        this.parts = new Vector();
        this.align = align;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        g.setColor(0x111111);

        for (int i = 0; i < calculatedParts.length; i++) {
            if (calculatedParts[i].needsRecalc) {
                needRecalc();
            }
            else {
                calculatedParts[i].draw(g, highlighted);
            }
        }
    }

    public void recalc(int width) {
        calculatedParts = null;
        Vector calculatedPartsVec = new Vector();

        int x = 0;
        int y = 0;
        int currLineHeight = 0;
        int currLineFirstIndex = 0;

        for (int i = 0; i < parts.size(); i++) {
            RichTextPart part = (RichTextPart) parts.elementAt(i);

            // 1. part is too wide to fit the screen -> split it into lines, add them, done
            if (part.width > width) {
                y += currLineHeight;

                RichTextPart[] brokenParts = part.breakToWidth(width);

                if (brokenParts.length > 0) {
                    for (int j = 0; j < brokenParts.length; j++) {
                        brokenParts[j].x = 0;
                        brokenParts[j].y = y;

                        if (j < brokenParts.length - 1) {
                            // not last part -> this line will only contain this part, so go to next line
                            y += brokenParts[j].height;
                        }
                        calculatedPartsVec.addElement(brokenParts[j]);
                    }

                    // further items will be placed to the right of the last broken part
                    RichTextPart lastPart = brokenParts[brokenParts.length - 1];
                    x = lastPart.width;
                    currLineHeight = lastPart.height;
                }

                currLineFirstIndex = calculatedPartsVec.size();
                continue;
            }

            int availableWidth = width - x;

            // 2. part is too wide for the available width -> calculate alignment for this line, and add a new line
            if (part.width > availableWidth) {
                x = 0;
                y += currLineHeight;
                currLineHeight = 0;
                currLineFirstIndex = calculatedPartsVec.size();

                // whitespace can be skipped at the start of a line
                if (part.isWhitespace()) continue;
            }

            // 3. place this part at the end of the current line
            part.x = x;
            part.y = y;
            calculatedPartsVec.addElement(part);
            currLineHeight = Math.max(currLineHeight, part.height);
            x += part.width;
        }

        calculatedParts = new RichTextPart[calculatedPartsVec.size()];
        calculatedPartsVec.copyInto(calculatedParts);
        calculatedPartsVec = null;

        // calculate alignment for each part (for center or right alignment)
        if ((align & Graphics.LEFT) == 0 && calculatedParts.length > 0) {
            int lineBegin = 0;
            int lineEnd = 1;

            while (lineBegin < calculatedParts.length) {
                RichTextPart lineFirst = calculatedParts[lineBegin];
                int currentY = lineFirst.y;

                // find where this line ends, and thus which parts (begin-end range) belong to the same line
                if (lineEnd < calculatedParts.length) {
                    if (calculatedParts[lineEnd].y == currentY) {
                        lineEnd++;
                        continue;
                    }
                }

                // calculate the line's X offset based on the alignment, then offset each part
                RichTextPart lineLast = calculatedParts[lineEnd - 1];
                int availableWidth = width - lineLast.x - lineLast.width;
                int offset = getAlignedX(align, availableWidth);

                for (int i = lineBegin; i < lineEnd; i++) {
                    calculatedParts[i].x += offset;
                }

                // next line
                lineBegin = lineEnd;
                lineEnd++;
            }
        }

        // height of the item is the height of all accumulated lines
        height = y + currLineHeight;
    }

    public void addPart(RichTextPart part) {
        if (part instanceof RichTextStringPart) {
            RichTextStringPart strPart = (RichTextStringPart) part;
            addStringPart(strPart.getText(), strPart.getFont());
        } else {
            parts.addElement(part);
            needRecalc();
        }
    }

    public void addStringPart(String text, Font font) {
        if (text == null || text.length() == 0) return;
        
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