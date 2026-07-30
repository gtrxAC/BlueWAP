package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.Util;
import fi.gtrxac.bluewap.ui.*;

public class SingleLineLinkItem extends LinkItem {
    public String fullText;

    public SingleLineLinkItem(String text) {
        super(text);
        fullText = text;
    }

    public void recalc(int width) {
        text = Util.stringToWidth(fullText, Fonts.plain, width);
        super.recalc(width);
    }
}