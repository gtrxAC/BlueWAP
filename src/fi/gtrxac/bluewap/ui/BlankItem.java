package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class BlankItem extends Item {
    int definedHeight;

    public BlankItem(int height) {
        super(false);
        definedHeight = height;
    }

    public void draw(Graphics g, int width, boolean selected) {
    }

    public void recalc(int width) {
        height = definedHeight;
    }
}