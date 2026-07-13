//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import com.gtrxac.discord.*;
import fi.gtrxac.bluewap.URL;
import fi.gtrxac.bluewap.HTTP;
import fi.gtrxac.bluewap.ui.*;
import tube42.lib.imagelib.ImageUtils;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import java.io.*;
import java.util.*;

public class WmlImageItem extends StringItem implements Runnable {
    private URL url;
    private Image image;
    private boolean haveRequested;

    private static final int IMAGE_CACHE_SIZE = 10;
    private static Hashtable imageCache = new Hashtable();
    
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
        super.sizeChanged(width);
        if (image != null) height = image.getHeight();
    }

    public void run() {
        image = getOrFetchImage(url);
        App.resizeAllScreens();
    }

    private Image getOrFetchImage(URL url) {
        String urlStr = url.toString(false);

        CachedImage result = (CachedImage) imageCache.get(urlStr);
        if (result != null) return result.getImage();

        InputStream is = null;
        try {
            is = HTTP.createRequest(urlStr).getResponseStream();

            Image img = parseWbmp(is);

            int scaleMultiplier = Math.max(1, Fonts.height/16);

            if (scaleMultiplier > 1) {
                int scaleWidth = img.getWidth()*scaleMultiplier;
                int scaleHeight = img.getHeight()*scaleMultiplier;
                img = ImageUtils.resize(img, scaleWidth, scaleHeight, false, false);
            }

            result = new CachedImage(img);
            Util.hashtablePutCachedImageWithLimit(imageCache, urlStr, result, IMAGE_CACHE_SIZE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try { is.close(); } catch (Exception e) {}
        }

        if (result == null) return null;
        return result.getImage();
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