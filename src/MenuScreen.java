import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class MenuScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;

    TextFieldItem urlField = new TextFieldItem("Address", App.currentUrl, 2000, 0);
    ButtonItem goButton = new ButtonItem("Go!");

    public MenuScreen() {
        super(2, 2);

        addItem(urlField);
        addItem(goButton);

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                App.popScreen();
                break;
            }
            case CMD_SELECT: {
                selectItem();
                break;
            }
        }
    }

    protected void itemSelected(Item i) {
        if (i == goButton) {
            App.popScreen();
            App.currentUrl = urlField.getValue();
            App.currentWml = null;
            new Thread(App.instance).start();
        }
    }
}