package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.TextFieldItem;

public class WmlInputItem extends TextFieldItem {
    String varName;

    public WmlInputItem(String varName, String content) {
        super("Input text", "", 2000, 0);
        this.varName = varName;
    }

    public void valueChanged(String newValue) {
        if (varName == null) return;
        WmlVariables.set(varName, newValue);
    }
}