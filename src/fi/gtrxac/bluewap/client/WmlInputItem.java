package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.TextFieldItem;

public class WmlInputItem extends TextFieldItem {
    String varName;

    public WmlInputItem(String varName, String content, int maxlength) {
        super("Input text", WmlVariables.parse(content, false), maxlength, 0);
        this.varName = varName;
    }

    public void valueChanged(String newValue) {
        if (varName == null) return;
        WmlVariables.set(varName, newValue);
    }
}