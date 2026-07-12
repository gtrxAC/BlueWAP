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

        int pad = Fonts.height/10;
        g.drawImage(ticked ? tickedImage : untickedImage, pad, pad, 0);

        int strOffset = Fonts.height*6/5;
        g.translate(strOffset, 0);
        strItem.draw(g, width - strOffset, false);
        g.translate(-strOffset, 0);

        if (selected) {
            drawHighlight(g, width);
        }
    }

    private Image createRadioButtonImage(int size, boolean ticked) {
        int renderSize = (size*4 + 13)/14*14;
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

        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] == 0xFFFFFFFF) rgb[i] = 0x00FFFFFF;
        }

        result = Image.createRGBImage(rgb, renderSize, renderSize, true);

        return ImageUtils.resize(result, size, size, true, true);
    }

    public void sizeChanged(int width) {
        int newImageSize = Fonts.height - Fonts.height/10*2;

        if (imageSize != newImageSize) {
            untickedImage = null;
            tickedImage = null;
            untickedImage = createRadioButtonImage(newImageSize, false);
            tickedImage = createRadioButtonImage(newImageSize, true);
            imageSize = newImageSize;
        }

        strItem.sizeChanged(width - Fonts.height*5/4);
        height = strItem.height;
    }

    public void itemSelected() {
        if (group != null) group.setTicked(this);
    }
}