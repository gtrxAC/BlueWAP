import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class StringItem extends Item {
    String text;
    Font font;

    public StringItem(String text) {
        this(text, smallFont);
    }

    public StringItem(String text, Font font) {
        this.text = text;
        this.font = font;
    }

    public void draw(Graphics g, int width, boolean selected) {
        g.setFont(font);
        g.setColor(0x111111);
        g.drawString(text, 0, 0, 0);
    }

    public void sizeChanged(int width) {
        height = font.getHeight();
    }
}