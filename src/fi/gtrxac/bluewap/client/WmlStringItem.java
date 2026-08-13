package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;

public class WmlStringItem extends StringItem {
    private String rawText;

    public WmlStringItem(String text, int align) {
        super(text, null, align);
        rawText = text;
    }
    
    public void recalc(int width) {
        text = WmlVariables.parse(rawText, false);
        super.recalc(width);
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String newRawText) {
        rawText = newRawText;
        needRecalc();
    }
}