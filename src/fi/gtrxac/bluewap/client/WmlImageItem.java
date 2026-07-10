//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

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
        try {
            image = HTTP.createRequest(url.toString(false)).getResponseImage();
            App.resizeAllScreens();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//#endif