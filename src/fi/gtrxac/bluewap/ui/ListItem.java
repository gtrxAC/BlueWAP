package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import tube42.lib.imagelib.ImageUtils;

public class ListItem extends Item {
    public String text;
    private String displayText;

    private static int imageSize;
    private static Image arrowImage;

    public ListItem(String text) {
        super(true);
        this.text = text;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (highlighted) {
            g.setColor(0xAADDFF);
            g.fillRect(0, -screen.itemPadding, width, height + screen.itemPadding - 1);
            g.setColor(0x000000);
        } else {
            g.setColor(0x111111);
        }

        g.setFont(Fonts.plain);
        g.drawString(displayText, Fonts.height/4, Fonts.height/4, 0);

        int prevItemLastPixel = -screen.itemPadding - 1;
        g.setColor(0x888888);
        g.drawLine(0, prevItemLastPixel, width - 1, prevItemLastPixel);
        g.drawLine(0, height - 1, width - 1, height - 1);

        g.drawImage(arrowImage, width - Fonts.height/3, Fonts.height/4,
            Graphics.TOP | Graphics.RIGHT);
    }

    public void recalc(int width) {
        height = Fonts.height + Fonts.height/4*2 + 3;

        int textWidth = width - Fonts.height/4 - Fonts.height - Fonts.height/3;
        displayText = Util.stringToWidth(text, Fonts.plain, textWidth);

        int newImageSize = Fonts.height;

        if (imageSize != newImageSize) {
            arrowImage = null;
            arrowImage = createArrowImage(newImageSize, 0x444444);
            imageSize = newImageSize;
        }
    }

    private Image createArrowImage(int size, int color) {
        int renderSize = (size*9 + 13)/14*14;
        int blockSize = renderSize/14;

        Image result = Image.createImage(renderSize, renderSize);
        Graphics g = result.getGraphics();

        // background
        g.setColor(0xFF0000);
        g.fillRect(0, 0, renderSize, renderSize);

        // outline
        g.setColor(color);
        g.fillArc(blockSize, blockSize, blockSize*12, blockSize*12, 0, 360);

        // fill insides
        g.setColor(0xFF0000);
        g.fillArc(blockSize*2, blockSize*2, blockSize*10, blockSize*10, 0, 360);

        // triangle
        g.setColor(color);
        g.fillTriangle(
            blockSize*6, blockSize*4,
            blockSize*9, blockSize*7,
            blockSize*6, blockSize*10);

        // triangle with background color to make it an arrow
        int thickness = blockSize*3/2;
        g.setColor(0xFF0000);
        g.fillTriangle(
            blockSize*6, blockSize*4 + thickness,
            blockSize*9 - thickness, blockSize*7,
            blockSize*6, blockSize*10 - thickness);
        
        // make transparent
        int[] rgb = new int[renderSize*renderSize];
        result.getRGB(rgb, 0, renderSize, 0, 0, renderSize, renderSize);
        result = null;

        for (int i = 0; i < rgb.length; i++) {
            // Make every unrendered (white) pixel transparent (0 alpha).
            // Using the same color as the button's outline to avoid ugly outlines
            if (rgb[i] == 0xFFFF0000) {
                rgb[i] = color & 0x00FFFFFF;
            }
        }

        result = Image.createRGBImage(rgb, renderSize, renderSize, true);

        return ImageUtils.resize(result, size, size, true, true);
    }
}