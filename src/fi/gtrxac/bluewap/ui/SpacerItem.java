package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class SpacerItem extends Item {
    public SpacerItem() {
        super(false);
    }

    public void draw(Graphics g, int width, boolean selected) {
        g.setColor(0x888888);
        g.drawLine(0, height/2, width, height/2);
    }

    public void recalc(int width) {
        height = Math.max(3, Fonts.height/4/2*2 + 1);
    }
}