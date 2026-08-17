package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.Graphics;

public abstract class RichTextPart {
    public int x;
    public int y;
    public int width;
    public int height;
    public boolean needsRecalc;

    public abstract void draw(Graphics g, boolean highlighted);
    public abstract RichTextPart[] breakToWidth(int width);
    public abstract boolean isWhitespace();

    public void needRecalc() {
        needsRecalc = true;
        AppBase.instance.repaint();
    }

    // public abstract boolean canMerge(RichTextPart other);
}