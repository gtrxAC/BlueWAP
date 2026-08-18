package fi.gtrxac.bluewap.ui;

import javax.microedition.lcdui.*;

public class Fonts {
    private static int currentSize;
    private static int largerSize;
    private static int smallerSize;

    public static Font plain;
    public static Font bold;
    public static Font italic;
    public static Font underlined;

    public static int height;
    public static int boldHeight;
    public static int italicHeight;
    public static int underlinedHeight;

    public static void loadFonts(int size) {
        currentSize = size;
        largerSize = (size == Font.SIZE_SMALL) ? Font.SIZE_MEDIUM : Font.SIZE_LARGE;
        smallerSize = (size == Font.SIZE_LARGE) ? Font.SIZE_MEDIUM : Font.SIZE_SMALL;

        plain = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, size);
        bold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, size);
        italic = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, size);
        underlined = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, size);

        height = plain.getHeight();
        boldHeight = bold.getHeight();
        italicHeight = italic.getHeight();
        underlinedHeight = underlined.getHeight();

        AppBase.recalcAllScreens();
    }
    
    public static Font get(int size, boolean bold, boolean italic, boolean underlined) {
        if (size == -1) {
            size = smallerSize;
        }
        else if (size == 1) {
            size = largerSize;
        }
        else {
            size = currentSize;
        }

        int style = 0;
        if (bold) style = Font.STYLE_BOLD;
        if (italic) style = style | Font.STYLE_ITALIC;
        if (underlined) style = style | Font.STYLE_UNDERLINED;

        return Font.getFont(Font.FACE_PROPORTIONAL, style, size);
    }
}