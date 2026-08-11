//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import com.gtrxac.discord.*;
import fi.gtrxac.bluewap.URL;
import fi.gtrxac.bluewap.http.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import java.io.*;
import java.util.*;

//#ifndef MIDP1
import tube42.lib.imagelib.ImageUtils;
//#endif

public class WmlImageItem extends StringItem implements Runnable {
    private URL url;
    private String localsrc;
    private Image image;
    private boolean haveRequested;
    private String altText;

    private static final int IMAGE_CACHE_SIZE = 10;
    private static Hashtable imageCache = new Hashtable();
    
    public WmlImageItem(String url, String localsrc, String altText) {
        super(altText);

        try {
            this.url = new URL(url, History.getCurrent().url);
        }
        catch (Exception e) {}

        this.localsrc = localsrc;
        this.altText = altText;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (image != null) {
            g.drawImage(image, 0, 0, 0);
            return;
        }
        if (!haveRequested && this.url != null) {
            new Thread(this).start();
            haveRequested = true;
        }
        super.draw(g, screen, width, highlighted);
    }

    public void recalc(int width) {
        setText(WmlVariables.parse(altText, false));
        super.recalc(width);
        if (image != null) height = image.getHeight();
    }

    public void run() {
        image = getOrFetchImage(url);
        needRecalc();
    }

    private Image getOrFetchImage(URL url) {
//#ifndef MIDP1
        try {
            Image img = getLocalsrcImage(localsrc);
            if (img != null) return scaleImage(img);
        }
        catch (Exception e) {}
//#endif

        String urlStr = url.toString(false);

        CachedImage result = (CachedImage) imageCache.get(urlStr);
        if (result != null) return result.getImage();

        HTTP http = null;
        InputStream is = null;
        try {
            http = HTTP.createRequest(urlStr);
            String type = http.getResponseHeader("Content-Type");
            is = http.getResponseStream();

            Image img = null;
            
            if ("image/vnd.wap.wbmp".equals(type)) {
                img = parseWbmp(is);
            } else {
//#ifndef MIDP1
                img = Image.createImage(is);
//#else
                byte[] data = fi.gtrxac.bluewap.Util.readBytes(is);
                img = Image.createImage(data, 0, data.length);
//#endif
            }

//#ifndef MIDP1
            img = scaleImage(img);
//#endif

            result = new CachedImage(img);
            Util.hashtablePutCachedImageWithLimit(imageCache, urlStr, result, IMAGE_CACHE_SIZE);
        }
        catch (Exception e) {
            e.printStackTrace();

            // uncomment for error reporting
            // altText = e.toString();
            // needRecalc();
        }
        finally {
            if (http != null) http.close();
        }

        if (result == null) return null;
        return result.getImage();
    }

//#ifndef MIDP1
	private Image getLocalsrcImage(String path) throws Exception {
		if (!path.startsWith("pict:///")) {
			throw new Exception();
		}
		path = path.substring("pict:///".length());
		
		// find this image's spritesheet position from the csv
        HTTP h = HTTP.createRequest("jar://s.csv");
        String csv = h.getResponseString();
        String[] csvLines = fi.gtrxac.bluewap.Util.split(csv, "\n");

        for (int i = 0; i < csvLines.length; i++) {
            String[] columns = fi.gtrxac.bluewap.Util.split(csvLines[i], ",");
            if (columns.length != 5) continue;  // empty or malformed line
            if (!columns[0].equals(path)) continue;  // this line isn't the requested image

			int x = Integer.parseInt(columns[1]);
			int y = Integer.parseInt(columns[2]);
			int width = Integer.parseInt(columns[3]);
			int height = Integer.parseInt(columns[4]);

			HTTP h2 = HTTP.createRequest("jar://s.png");
			Image sheet = h2.getResponseImage();
			return ImageUtils.crop(sheet, x, y, x + width - 1, y + height - 1);
        }

		throw new Exception();  // not found
	}
//#endif

//#ifndef MIDP1
    private Image scaleImage(Image img) {
        int screenWidth = AppCanvas.instance.getWidth();
        int scaleMultiplier = Math.max(1, Math.min(Fonts.height/16, screenWidth/128));
        int scaleWidth = img.getWidth()*scaleMultiplier;

        // Upscale if possible (for high-res screens)
        if (scaleMultiplier > 1 && scaleWidth < screenWidth) {
            int scaleHeight = img.getHeight()*scaleMultiplier;
            img = ImageUtils.resize(img, scaleWidth, scaleHeight, false, false);
        }
        // Downscale if image too big for the screen
        else if (img.getWidth() > screenWidth) {
            int scaleRatio = screenWidth*1000/img.getWidth();
            img = ImageUtils.resize(img, screenWidth, img.getHeight()*scaleRatio/1000, true, true);
        }
        return img;
    }
//#endif

    private Image parseWbmp(InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);

        try {
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

//#ifndef MIDP1
            // MIDP2: create color array and create image from it

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

            return Image.createRGBImage(rgb, width, height, true);
//#else
            // MIDP1: createRGBImage unavailable - instead create an image and draw each pixel on it

            Image result = Image.createImage(width, height);
            Graphics g = result.getGraphics();
            g.setColor(0x000000);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < bytesPerRow; x++) {
                    int b = dis.readUnsignedByte();
                    for (int i = 0; i < 8; i++) {
                        int destX = x*8 + i;
                        if (destX >= width) continue;

                        int bit = (b >> (7 - i)) & 1;
                        if (bit == 0) g.fillRect(destX, y, 1, 1);
                    }
                }
            }

            return result;
//#endif
        }
        finally {
            try { dis.close(); } catch (Exception e) {}
        }
    }
}
//#endif