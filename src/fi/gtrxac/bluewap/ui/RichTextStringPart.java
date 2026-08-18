package fi.gtrxac.bluewap.ui;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

public class RichTextStringPart extends RichTextPart {
    private String text;
    private Font font;

    public RichTextStringPart(String text, Font font) {
        this.text = text;
        this.font = font;
        this.width = font.stringWidth(text);
        this.height = font.getHeight();
    }

    public void draw(Graphics g, boolean highlighted) {
        g.setFont(font);
        g.drawString(text, x, y, 0);
    }

    public RichTextPart[] breakToWidth(int width) {
        if ("".equals(text) || font.stringWidth("mm") > width) {
            return new RichTextPart[0];
        }

        Vector resultVec = new Vector();
        String remainingText = text;

        while (remainingText.length() > 0) {
            String curLine = remainingText;

            while (font.stringWidth(curLine) > width) {
                curLine = curLine.substring(0, curLine.length() - 1);
            }
            
            RichTextStringPart newPart = new RichTextStringPart(curLine, font);
            resultVec.addElement(newPart);

            remainingText = remainingText.substring(curLine.length());
        }

        RichTextPart[] result = new RichTextPart[resultVec.size()];
        resultVec.copyInto(result);
        return result;
    }

    public boolean isWhitespace() {
        return "".equals(text.trim());
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        text = newText;
        needRecalc();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font newFont) {
        font = newFont;
        needRecalc();
    }


    // public abstract boolean canMerge(RichTextPart other);
}