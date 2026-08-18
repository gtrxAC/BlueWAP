package fi.gtrxac.bluewap.client;

import java.util.Vector;
import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

/**
 * Wrapper for RichTextItem that adds WML variable parsing.
 */
public class WmlStringItem extends Item {
    private RichTextItem richText;
    private Vector rawParts;
    private int align;

    public WmlStringItem(int align) {
        super(false);
        this.rawParts = new Vector();
        this.align = align;  // unused
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        richText.draw(g, screen, width, highlighted);
    }
    
    public void recalc(int width) {
        richText = new RichTextItem();

        for (int i = 0; i < rawParts.size(); i++) {
            RichTextStringPart part = (RichTextStringPart) rawParts.elementAt(i);
            String text = WmlVariables.parse(part.getText(), false);
            richText.addStringPart(text, part.getFont());
        }

        richText.recalc(width);
        height = richText.height;
    }

    public void addStringPart(String text, Font font) {
        rawParts.addElement(new RichTextStringPart(text, font));
    }

    public void trimLastPart() {
        if (rawParts.size() == 0) return;
        RichTextStringPart last = (RichTextStringPart) rawParts.lastElement();
        last.setText(Util.trimRight(last.getText()));
    }
}