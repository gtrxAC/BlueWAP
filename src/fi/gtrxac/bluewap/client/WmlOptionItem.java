package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.RadioButtonItem;

public class WmlOptionItem extends RadioButtonItem {
    public String value;

    public WmlOptionItem(WmlOptionGroup group, String text, String value) {
        super(group, text);
        this.value = value;
    }
}