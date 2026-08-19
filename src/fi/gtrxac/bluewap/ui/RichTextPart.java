package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;

public abstract class RichTextPart {
    public int x;
    public int y;
    public int width;
    public int height;
    public boolean needsRecalc;

    public abstract void draw(Graphics g, boolean highlighted);

    /**
     * Break this part into multiple parts that fit into the given screen width in pixels.
     */
    public abstract RichTextPart[] breakToWidth(int width);

    /**
     * Determine if this part only consists of whitespace; if it's safe to discard at the beginning/end of lines.
     */
    public abstract boolean isWhitespace();

    /**
     * Try to merge this part with another part (this part on the left and the other part on the right).
     * Returns a new part without modifying the existing ones.
     * The result should be functionally identical to the two separate parts.
     * Returns null if the parts cannot be merged.
     */
    public abstract RichTextPart merge(RichTextPart other);

    public void needRecalc() {
        needsRecalc = true;
        AppBase.instance.repaint();
    }
}