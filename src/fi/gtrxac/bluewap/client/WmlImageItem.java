//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import java.io.*;

public class WmlImageItem extends StringItem implements Runnable {
    private URL url;
    private Image image;
    private boolean haveRequested;
    
    public WmlImageItem(String url, String altText) {
        super(altText);

        try {
            this.url = new URL(url, History.getCurrent().url);
        }
        catch (Exception e) {}
    }

    public void draw(Graphics g, int width, boolean selected) {
        if (image != null) {
            g.drawImage(image, 0, 0, 0);
            return;
        }
        if (!haveRequested && this.url != null) {
            new Thread(this).run();
            haveRequested = true;
        }
        super.draw(g, width, selected);
    }

    public void sizeChanged(int width) {
        if (image != null) {
            height = image.getHeight();
            return;
        }
        super.sizeChanged(width);
    }

    public void run() {
        InputStream is = null;
        try {
            is = HTTP.createRequest(url.toString(false)).getResponseStream();
            image = parseWbmp(is);
            App.resizeAllScreens();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    private Image parseWbmp(InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);
        dis.skip(2);

        int width = dis.readUnsignedByte();
        if (width == 0 || width > 127) {
            throw new IllegalArgumentException("unsupported image width");
        }
        int height = dis.readUnsignedByte();
        if (height == 0 || height > 127) {
            throw new IllegalArgumentException("unsupported image height");
        }

        int bytesPerRow = (width + 7)/8;
        int fullWidth = bytesPerRow*8;
        int[] rgb = new int[width*height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < bytesPerRow; x++) {
                int b = dis.readUnsignedByte();
                for (int i = 0; i < 8; i++) {
                    int destX = x*8 + i;
                    if (destX >= width) continue;

                    int bit = (b >> (7 - i)) & 1;
                    rgb[y*width + destX] = (bit == 1 ? 0xFFFFFFFF : 0xFF000000);
                }
            }
        }

        dis.close();
        return Image.createRGBImage(rgb, width, height, true);
    }
}
//#endif