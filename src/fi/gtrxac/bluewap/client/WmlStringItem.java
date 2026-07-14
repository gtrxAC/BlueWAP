package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;

public class WmlStringItem extends StringItem {
    String rawText;

    public WmlStringItem(String text) {
        super(text);
        rawText = text;
    }
    
    public void sizeChanged(int width) {
        text = WmlVariables.parse(rawText, false);
        super.sizeChanged(width);
    }
}