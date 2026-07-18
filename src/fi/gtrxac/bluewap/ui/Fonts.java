package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.*;

public class Fonts {
    public static Font plain;
    public static Font bold;
    public static Font underlined;

    public static int height;
    public static int boldHeight;
    public static int underlinedHeight;

    public static void loadFonts(int size) {
        plain = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, size);
        bold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, size);
        underlined = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, size);

        height = plain.getHeight();
        boldHeight = bold.getHeight();
        underlinedHeight = underlined.getHeight();

        AppBase.recalcAllScreens();
    }
}