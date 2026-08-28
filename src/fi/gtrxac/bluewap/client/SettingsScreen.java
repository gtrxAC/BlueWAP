//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.http.*;
import fi.gtrxac.bluewap.ui.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.TextField;
import com.gtrxac.discord.HTTPQueue;

public class SettingsScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;

    RadioButtonGroup fontSizeGroup;
    TextFieldItem connLimitField = new TextFieldItem("Max. connections", "" + HTTPQueue.maxSlots, 3, TextField.NUMERIC);

    public SettingsScreen() {
        super();

        addItem("Font size:");
        fontSizeGroup = new RadioButtonGroup();
        addItem(new RadioButtonItem(fontSizeGroup, "Small"));
        addItem(new RadioButtonItem(fontSizeGroup, "Medium"));
        addItem(new RadioButtonItem(fontSizeGroup, "Large"));

        int sizeIndex = Settings.fontSize == Font.SIZE_SMALL ? 0 :
            Settings.fontSize == Font.SIZE_MEDIUM ? 1 : 2;

        fontSizeGroup.setTickedIndex(sizeIndex);

        addItem("Max. connections:");
        addItem(connLimitField);

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        
        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                int[] fontSizes = { Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE };
                Settings.fontSize = fontSizes[fontSizeGroup.getTickedIndex()];

                HTTPQueue.maxSlots = Integer.parseInt(connLimitField.getValue());
                if (HTTPQueue.maxSlots < 1) HTTPQueue.maxSlots = 1;
                
                Settings.save();
                Fonts.loadFonts(Settings.fontSize);
                History.onFontSizeChange();

                App.popScreen();
                break;
            }
            case CMD_SELECT: {
                selectItem();
                break;
            }
        }
    }
}
//#endif