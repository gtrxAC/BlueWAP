package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

//#ifndef MIDP1
import tube42.lib.imagelib.ImageUtils;
//#endif

public class RadioButtonItem extends Item {
    private RadioButtonGroup group;
    private StringItem strItem;
    public boolean ticked;
    
    private static Image tickedImage;
    private static Image untickedImage;

//#ifndef MIDP1
    private static int imageSize;
//#endif
    
    public RadioButtonItem(RadioButtonGroup group, String text) {
        super(true);
        this.group = group;
        group.addItem(this);
        this.strItem = new StringItem(text);
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (highlighted) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, width, height);
            g.setColor(0x000000);
        } else {
            g.setColor(0x111111);
        }

//#ifndef MIDP1
        int pad = Fonts.height/10;
        g.drawImage(ticked ? tickedImage : untickedImage, pad, pad, 0);
//#else
        g.drawImage(ticked ? tickedImage : untickedImage, Fonts.height/2, Fonts.height/2,
            Graphics.HCENTER | Graphics.VCENTER);
//#endif

        int strOffset = Fonts.height*6/5;
        g.translate(strOffset, 0);
        strItem.draw(g, screen, width - strOffset, false);
        g.translate(-strOffset, 0);

        if (highlighted) {
            drawHighlight(g, width);
        }
    }

//#ifndef MIDP1
    private Image createRadioButtonImage(int size, boolean ticked) {
        int renderSize = (size*Util.vectorRenderScale + 13)/14*14;
        int blockSize = renderSize/14;

        Image result = Image.createImage(renderSize, renderSize);
        Graphics g = result.getGraphics();

        // outline
        g.setColor(0x9A9A9A);
        g.fillArc(blockSize, blockSize, blockSize*12, blockSize*12, 0, 360);

        // fill insides
        g.setColor(0xDDDDDD);
        g.fillArc(blockSize*2, blockSize*2, blockSize*10, blockSize*10, 0, 360);

        // black dot for ticked button
        if (ticked) {
            g.setColor(0x111111);
            g.fillArc(blockSize*4, blockSize*4, blockSize*6, blockSize*6, 0, 360);
        }
        
        // make transparent
        int[] rgb = new int[renderSize*renderSize];
        result.getRGB(rgb, 0, renderSize, 0, 0, renderSize, renderSize);
        result = null;
        int topLeft = rgb[0];  // color of an unpainted pixel

        for (int i = 0; i < rgb.length; i++) {
            // Make every unpainted (white) pixel transparent (0 alpha).
            // Using the same color as the button's outline to avoid ugly outlines
            if (rgb[i] == topLeft) {
                rgb[i] = 0x009A9A9A;
            }
        }

        result = Image.createRGBImage(rgb, renderSize, renderSize, true);

        return ImageUtils.resize(result, size, size, true, true);
    }
//#endif

    public void recalc(int width) {
//#ifndef MIDP1
        int newImageSize = Fonts.height - Fonts.height/10*2;

        if (imageSize != newImageSize) {
            untickedImage = null;
            tickedImage = null;
            untickedImage = createRadioButtonImage(newImageSize, false);
            tickedImage = createRadioButtonImage(newImageSize, true);
            imageSize = newImageSize;
        }
//#else
        if (tickedImage == null) {
            try {
                tickedImage = Image.createImage("/t.png");
            }
            catch (Exception e) {}

            try {
                untickedImage = Image.createImage("/u.png");
            }
            catch (Exception e) {}
        }
//#endif

        strItem.recalc(width - Fonts.height*5/4);
        height = strItem.height;
    }

    public void itemSelected() {
        if (group != null) group.setTicked(this);
    }
}