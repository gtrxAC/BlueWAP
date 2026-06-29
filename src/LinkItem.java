import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class LinkItem extends Item {
    String text;
    private int stringWidth;

    public LinkItem(String text) {
        super(true);
        this.text = text;
    }

    public void draw(Graphics g, int width, boolean selected) {
        if (selected) {
            g.setColor(0xEEF8FF);
            g.fillRect(0, 0, stringWidth, height);
            g.setColor(0x2244AA);
        } else {
            g.setColor(0x3355CC);
        }
        
        g.setFont(smallUnderlinedFont);
        g.drawString(text, 0, 0, 0);

        if (selected) {
            drawHighlight(g, 0, 0, stringWidth, height);
        }
    }

    public void sizeChanged(int width) {
        height = SMALL_UNDERLINED_FONT_HEIGHT;
        stringWidth = smallUnderlinedFont.stringWidth(text);
    }
}