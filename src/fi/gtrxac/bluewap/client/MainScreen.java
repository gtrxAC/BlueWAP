//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class MainScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_SELECT = 1;
    public static final int CMD_MENU = 2;
    public static final int CMD_FORWARD = 3;
    public static final int CMD_REFRESH = 4;
    public static final int CMD_WARNINGS = 5;

    public static String warningsWml;
    public static final MainScreen instance = new MainScreen();

    public MainScreen() {
        super(2, 2);
        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Menu", Command.SCREEN, CMD_MENU));
        addCommand(new Command("Forward", Command.SCREEN, CMD_FORWARD));
        addCommand(new Command("Refresh", Command.SCREEN, CMD_REFRESH));
        addCommand(new Command("Warnings", Command.SCREEN, CMD_WARNINGS));
    }

    public void displayWml(String wml, String cardId) {
        synchronized (History.getCurrent()) {
            removeAllItems();
            try {
                parseWml(wml, cardId);
            }
            catch (Exception e) {
                e.printStackTrace();
                addItem(new StringItem("Failed to load page:"));
                addItem(new StringItem(e.toString()));
            }
        }
    }

    public void parseWml(String wml, String cardId) throws Exception {
        wml = wml.trim();
        ByteArrayInputStream is = new ByteArrayInputStream(Util.stringToBytes(wml));

        WmlParser p = new WmlParser();

        boolean haveShownCard = false;

        p.setInput(is, null);
        p.defineEntityReplacementText("nbsp", " ");
        p.nextTag();

        try {
            require(p, XmlPullParser.START_TAG, "wml");
            p.nextTag();
        }
        catch (XmlPullParserException e) {
            p.addWarning("expected <wml>");
        }

        try {
            require(p, XmlPullParser.START_TAG, "head");
            p.skipSubTree();
            p.nextTag();
        }
        catch (XmlPullParserException e) {}

        while (p.getEventType() != XmlPullParser.END_DOCUMENT) {
            try {
                require(p, XmlPullParser.START_TAG, "card");

                // determine if this card is to be shown (specified card id or first card)
                String thisCardId = p.getAttributeValue(null, "id");

                if ((cardId == null && haveShownCard) || (cardId != null && !cardId.equals(thisCardId))) {
                    // skip this card
                    p.skipSubTree();
                    p.next();
                    ignoreWhitespace(p);
                    continue;
                }
                haveShownCard = true;
            }
            catch (XmlPullParserException e) {
                try {
                    require(p, XmlPullParser.END_TAG, "wml");
                    break;
                }
                catch (XmlPullParserException ee) {
                    p.addWarning("expected <card> or </wml>");
                }
            }
            
            String currentLinkTarget = null;

            try {
                while (true) {
                    p.next();
                    ignoreWhitespace(p);

                    if (p.getEventType() == XmlPullParser.END_DOCUMENT) {
                        p.addWarning("unexpected end of file");
                        break;
                    }
                    if (p.getEventType() == XmlPullParser.END_TAG && "card".equals(p.getName())) {
                        p.nextTag();
                        break;
                    }
                    if (p.getEventType() == XmlPullParser.START_TAG && "p".equals(p.getName())) {
                        parsePTag(p);
                    }
                    if (p.getEventType() == XmlPullParser.START_TAG && "a".equals(p.getName())) {
                        parseATag(p);
                    }
                    if (p.getEventType() == XmlPullParser.START_TAG && "anchor".equals(p.getName())) {
                        parseAnchorTag(p);
                    }
                    if (p.getEventType() == XmlPullParser.TEXT) {
                        addItem(new StringItem(p.getText().trim()));
                    }
                    if (p.getEventType() == XmlPullParser.START_TAG && "img".equals(p.getName())) {
                        parseImgTag(p);
                    }
                    if (p.getEventType() == XmlPullParser.START_TAG && "input".equals(p.getName())) {
                        // not usable yet
                        addItem(new TextFieldItem("Input text", "not implemented", 2000, 0));
                        p.skipSubTree();
                        p.next();
                    }
                }
            }
            catch (XmlPullParserException e) {
                p.addWarning(e.toString());
            }

            // try {
            //     require(p, XmlPullParser.END_TAG, "card");
            //     p.nextTag();
            // }
            // catch (XmlPullParserException e) {
            //     addWarning(e, warnings, "expected </card>");
            // }
        }

        try {
            require(p, XmlPullParser.END_TAG, "wml");
        }
        catch (XmlPullParserException e) {
            p.addWarning("expected </wml>");
        }

        if (!haveShownCard) {
            p.addWarning((cardId == null) ? "no cards found" : "card '" + cardId + "' not found");
        }

        if (!History.getCurrent().url.protocol.equals("warnings")) {
            createWarningsWml(p);
        }
    }

    private String sanitizeWml(String text) {
        text = Util.replace(text, "&", "&amp;");
        text = Util.replace(text, "'", "&apos;");
        text = Util.replace(text, "\"", "&quot;");
        text = Util.replace(text, "<", "&lt;");
        return Util.replace(text, ">", "&gt;");
    }

    private void createWarningsWml(WmlParser p) {
        StringBuffer warningsBuf = new StringBuffer();
        warningsBuf.append(App.WML_BEGIN)
            .append("<card title=\"Page warnings\">")
            .append("<p>Problems with &quot;")
            .append(sanitizeWml(History.getCurrent().url.toString(false)))
            .append("&quot;:</p>");

        if (p.warnings.size() == 0) {
            warningsBuf.append("<p>No problems found with this page.</p>");
        }
        for (int i = 0; i < p.warnings.size(); i++) {
            String warn = (String) p.warnings.elementAt(i);
            String warnLoc = (String) p.warningLocations.elementAt(i);

            warningsBuf.append("<p>Warning: ")
                .append(sanitizeWml(warn))
                .append("</p>")
                .append("<p>at ")
                .append(sanitizeWml(warnLoc))
                .append("</p>");
        }

        warningsBuf.append(App.WML_END);

        warningsWml = warningsBuf.toString();
    }

    public void parsePTag(WmlParser p) throws Exception {
        String text = "";

        while (true) {
            p.next();
            ignoreWhitespace(p);

            if (p.getEventType() == XmlPullParser.END_DOCUMENT) {
                p.addWarning("unexpected end of file");
                break;
            }
            else if (p.getEventType() == XmlPullParser.END_TAG && "p".equals(p.getName())) {
                break;
            }
            if (p.getEventType() == XmlPullParser.START_TAG && "a".equals(p.getName())) {
                break;
            }
            if (p.getEventType() == XmlPullParser.START_TAG && "anchor".equals(p.getName())) {
                break;
            }
            if (p.getEventType() == XmlPullParser.START_TAG && "input".equals(p.getName())) {
                break;
            }
            
            String addText = parseTextElement(p);
            if (addText != null) {
                text += addText;
            } else {
                p.addWarning("expected text, <img>, <br>, or </a>");
            }
        }
        if (text.length() != 0) {
            addItem(new StringItem(text));
        }
    }

    public void parseATag(WmlParser p) throws Exception {
        String text = "";
        String target = p.getAttributeValue(null, "href");

        while (true) {
            p.next();
            ignoreWhitespace(p);

            if (p.getEventType() == XmlPullParser.END_DOCUMENT) {
                p.addWarning("unexpected end of file");
                break;
            }
            else if (p.getEventType() == XmlPullParser.END_TAG && "a".equals(p.getName())) {
                break;
            }
            
            String addText = parseTextElement(p);
            if (addText != null) {
                text += addText;
            } else {
                p.addWarning("expected text, <img>, <br>, or </a>");
            }
        }
        addItem(new WmlAnchorItem(text, WmlAnchorItem.ACTION_GO, target));
    }

    public void parseAnchorTag(WmlParser p) throws Exception {
        String text = "";
        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;

        while (true) {
            p.next();
            ignoreWhitespace(p);

            if (p.getEventType() == XmlPullParser.END_DOCUMENT) {
                p.addWarning("unexpected end of file");
                break;
            }
            else if (p.getEventType() == XmlPullParser.END_TAG && "anchor".equals(p.getName())) {
                break;
            }
            else if (p.getEventType() == XmlPullParser.START_TAG && "go".equals(p.getName())) {
                action = WmlAnchorItem.ACTION_GO;
                target = p.getAttributeValue(null, "href");
                if (target == null) {
                    p.addWarning("<go> does not have 'href' attribute");
                    target = "#";
                }
                p.skipSubTree();
                continue;
            }
            else if (p.getEventType() == XmlPullParser.START_TAG && "prev".equals(p.getName())) {
                action = WmlAnchorItem.ACTION_PREV;
                p.skipSubTree();
                continue;
            }
            else if (p.getEventType() == XmlPullParser.START_TAG && "refresh".equals(p.getName())) {
                action = WmlAnchorItem.ACTION_REFRESH;
                p.skipSubTree();
                continue;
            }
            
            String addText = parseTextElement(p);
            if (addText != null) {
                text += addText.trim();
            } else {
                p.addWarning("expected text, <img>, <br>, <go>, <prev>, <refresh>, or </anchor>");
            }
        }
        addItem(new WmlAnchorItem(text, action, target));
    }

    public String parseTextElement(WmlParser p) throws Exception {
        if (p.getEventType() == XmlPullParser.TEXT) {
            return p.getText();
        }
        else if (p.getEventType() == XmlPullParser.START_TAG && "img".equals(p.getName())) {
            return getImgAltText(p);
        }
        else if (p.getEventType() == XmlPullParser.START_TAG && "br".equals(p.getName())) {
            return "\n";
        }
        return null;
    }

    public void parseImgTag(WmlParser p) {
        addItem(new StringItem(getImgAltText(p)));
    }

    public String getImgAltText(WmlParser p) {
        String result = p.getAttributeValue(null, "alt");
        if (result != null) return result;
        result = p.getAttributeValue(null, "src");
        if (result != null) return result;

        p.addWarning("<img> tag does not have 'src' or 'alt' attribute");
        return "Image";
    }

    private void ignoreWhitespace(WmlParser p) throws IOException {
        try {
            while (p.getEventType() == KXmlParser.TEXT && p.isWhitespace()) {
                p.next();
            }
        }
        catch (XmlPullParserException e) {}
    }

    private void require(WmlParser p, int type, String text) throws Exception {
        ignoreWhitespace(p);
        p.require(type, null, text);
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                History.back();
                break;
            }
            case CMD_SELECT: {
                selectItem();
                break;
            }
            case CMD_FORWARD: {
                History.forward();
                break;
            }
            case CMD_MENU: {
                App.pushScreen(new MenuScreen());
                break;
            }
            case CMD_REFRESH: {
                History.getCurrent().refresh();
                break;
            }
            case CMD_WARNINGS: {
                History.visit("warnings://", false);
                break;
            }
        }
    }

    protected void itemSelected(Item i) {
        if (i instanceof WmlAnchorItem) {
            WmlAnchorItem anchor = (WmlAnchorItem) i;
            switch (anchor.action) {
                case WmlAnchorItem.ACTION_GO: {
                    History.visit(anchor.target, true);
                    break;
                }
                case WmlAnchorItem.ACTION_PREV: {
                    History.back();
                    break;
                }
                case WmlAnchorItem.ACTION_REFRESH: {
                    History.getCurrent().refresh();
                    break;
                }
            }
        }
    }
}
//#endif