package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import tube42.lib.imagelib.ImageUtils;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class RadioButtonItem extends Item {
    private RadioButtonGroup group;
    private StringItem strItem;
    public boolean ticked;

    private static int imageSize;
    private static Image tickedImage;
    private static Image untickedImage;
    
    public RadioButtonItem(RadioButtonGroup group, String text) {
        super(true);
        this.group = group;
        group.addItem(this);
        this.strItem = new StringItem(text);
    }

    public void draw(Graphics g, int width, boolean selected) {
        if (selected) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, width, height);
            g.setColor(0x000000);
        } else {
            g.setColor(0x111111);
        }

        int pad = 0;//Fonts.height/7;
        g.drawImage(ticked ? tickedImage : untickedImage, pad, pad, 0);

        int strOffset = Fonts.height*5/4;
        g.translate(strOffset, 0);
        strItem.draw(g, width - strOffset, false);
        g.translate(-strOffset, 0);

        if (selected) {
            drawHighlight(g, width);
        }
    }

    public void sizeChanged(int width) {
        int newImageSize = Fonts.height;// - Fonts.height/7*2;

        if (imageSize != newImageSize) {
            try {
                untickedImage = null;
                untickedImage = Image.createImage("/u.png");
                untickedImage = ImageUtils.resize(untickedImage, newImageSize, newImageSize, true, true);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                tickedImage = null;
                tickedImage = Image.createImage("/t.png");
                tickedImage = ImageUtils.resize(tickedImage, newImageSize, newImageSize, true, true);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            imageSize = newImageSize;
        }

        strItem.sizeChanged(width - Fonts.height*5/4);
        height = strItem.height;
    }

    public void itemSelected() {
        if (group != null) group.setTicked(this);
    }
}