import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class ButtonItem extends Item {
    String text;
    int bgColor;
    int darkerBgColor;
    int selectedBgColor;
    int textColor;
    int selectedTextColor;

    public ButtonItem(String text) {
        this(text, 0xDDDDDD, 0xAADDFF);
    }

    public ButtonItem(String text, int bgColor) {
        this(text, bgColor, bgColor, 0, 0);
        textColor = Util.higherContrast(0x000000, 0xFFFFFF, bgColor);
        selectedTextColor = textColor;
    }

    public ButtonItem(String text, int bgColor, int selectedBgColor) {
        this(text, bgColor, selectedBgColor, 0, 0);
        textColor = Util.higherContrast(0x000000, 0xFFFFFF, bgColor);
        selectedTextColor = Util.higherContrast(0x000000, 0xFFFFFF, selectedBgColor);
    }

    public ButtonItem(String text, int bgColor, int selectedBgColor, int textColor, int selectedTextColor) {
        super(true);
        this.text = text;
        this.bgColor = bgColor;
        this.darkerBgColor = Util.blend(bgColor, 0x000000, 7);
        this.selectedBgColor = selectedBgColor;
        this.textColor = textColor;
        this.selectedTextColor = selectedTextColor;
    }

    public void draw(Graphics g, int width, boolean selected) {
        g.setColor(selected ? selectedBgColor : bgColor);
        g.fillRect(0, 0, width, height);

        if (selected) {
            drawHighlight(g, width);
        } else {
            g.setColor(darkerBgColor);
            g.drawRect(0, 0, width - 1, height - 1);
        }
        
        g.setFont(smallBoldFont);
        g.setColor(selected ? selectedTextColor : textColor);
        g.drawString(text, width/2, (height - smallBoldFont.getHeight())/2, Graphics.HCENTER | Graphics.TOP);
    }

    public void sizeChanged(int width) {
        height = smallBoldFont.getHeight()*3/2;
    }
}