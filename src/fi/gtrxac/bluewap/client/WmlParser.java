//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.util.Vector;
import java.io.*;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class WmlParser extends KXmlParser {
    private ListScreen output;
    private String wml;
    private String cardId;
    private boolean haveShownCard;
    private boolean lastItemTerminated;

    private Vector warnings;
    private Vector warningLocations;

    public static Vector commands = new Vector(5);

    private WmlParser(ListScreen output, String wml, String cardId) throws Exception {
        this.output = output;
        this.wml = wml.trim();
        this.cardId = cardId;
        this.haveShownCard = false;
        this.lastItemTerminated = false;

        this.warnings = new Vector(5);
        this.warningLocations = new Vector(5);

        commands.setSize(0);

        byte[] wmlBytes = Util.stringToBytes(this.wml);
        ByteArrayInputStream is = new ByteArrayInputStream(wmlBytes);
        setInput(is, null);
        defineEntityReplacementText("nbsp", " ");
    }

    public static void displayWml(ListScreen output, String wml, String cardId) {
        synchronized (History.getCurrent()) {
            output.removeAllItems();
            try {
                WmlParser p = new WmlParser(output, wml, cardId);
                p.parseWml();
                p.createWarningsWml();
            }
            catch (Exception e) {
                e.printStackTrace();
                output.addItem(new StringItem("Failed to load page:"));
                output.addItem(new StringItem(e.toString()));
            }
        }
    }

    private void parseWml() throws Exception {
        final String WML_NESTED_TAGS = "expected <card>, <head>, <template>, or </wml>";

        // go to the first tag, if fails then the page is not xml
        try {
            nextTag();
        }
        catch (XmlPullParserException e) {
            addWarning("page does not begin with a tag, treating it as raw text");
            output.addItem(MainScreen.systemBrowserButton);
            output.addItem(new StringItem(wml));
            return;
        }

        // first tag (ignoring xml header and doctype) is <wml>
        try {
            require(XmlPullParser.START_TAG, "wml");
            nextTag();
        }
        catch (XmlPullParserException e) {
            addWarning("expected <wml>");
        }

        // <wml> nested tags: card (any amount), head (up to 1), template (up to 1)
        boolean haveHead = false;
        boolean haveTemplate = false;

        while (true) {
            if (getEventType() == TEXT) {
                addWarning(WML_NESTED_TAGS);
            }
            else if (getEventType() == START_TAG) {
                if ("card".equals(getName())) {
                    parseCard();
                }
                else if ("head".equals(getName())) {
                    if (haveHead) addWarning("more than one <head>");
                    haveHead = true;
                    skipSubTree();  // not supported
                }
                else if ("template".equals(getName())) {
                    if (haveTemplate) addWarning("more than one <template>");
                    haveTemplate = true;
                    skipSubTree();  // not supported
                }
                else {
                    addWarning(WML_NESTED_TAGS);
                    skipSubTree();
                }
            }
            else if (getEventType() == END_TAG) {
                if ("wml".equals(getName())) {
                    break;
                } else {
                    addWarning(WML_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }

        if (!haveShownCard) {
            addWarning((cardId == null) ? "no cards found" : "card '" + cardId + "' not found");
        }
    }

    private void parseCard() throws Exception {
        final String CARD_NESTED_TAGS = "expected <do>, <onevent>, <p>, <timer>, or </card>";

        // determine if this card is to be shown (specified card id or first card)
        String thisCardId = getAttributeValue(null, "id");

        if ((cardId == null && haveShownCard) || (cardId != null && !cardId.equals(thisCardId))) {
            // skip this card
            skipSubTree();
            return;
        }

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                addWarning(CARD_NESTED_TAGS);
                appendToLastItem(getText().trim());
            }
            if (getEventType() == START_TAG) {
                if ("p".equals(getName())) {
                    parseP();
                }
                else if ("do".equals(getName())) {
                    parseDo();
                }
                else if ("onevent".equals(getName())) {
                    parseOnevent();
                }
                else if ("timer".equals(getName())) {
                    parseTimer();
                }
                else {
                    addWarning(CARD_NESTED_TAGS);
                    skipSubTree();
                }
            }
            else if (getEventType() == END_TAG) {
                if ("card".equals(getName())) {
                    break;
                } else {
                    addWarning(CARD_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                haveShownCard = true;
                return;
            }
            nextItem();
        }

        try {
            require(XmlPullParser.END_TAG, "card");
        }
        catch (XmlPullParserException e) {
            addWarning("expected <do>, <onevent>, <p>, <timer>, or </card>");
        }
        haveShownCard = true;
    }

    private boolean isFormattingTag() {
        return ",b,big,em,i,small,strong,u,".indexOf("," + getName() + ",") != -1;
    }

    private void parseP() throws Exception {
        final String P_NESTED_TAGS = "expected text, <a>, <anchor>, <b>, <big>, <br>, <do>, <em>, <fieldset>, <i>, <input>, <img>, <select>, <small>, <strong>, <table>, <u>, or </p>";

        // em, b, u, strong, small, i, big
        // a, anchor, b, big, br, em, i, img, small, strong, table, u 

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText().trim());
            }
            if (getEventType() == START_TAG) {
                if ("a".equals(getName())) {
                    parseA();
                }
                else if ("anchor".equals(getName())) {
                    parseAnchor();
                }
                else if ("br".equals(getName())) {
                    lastItemTerminated = true;
                }
                else if ("do".equals(getName())) {
                    parseDo();
                }
                else if ("fieldset".equals(getName())) {
                    lastItemTerminated = true;
                }
                else if ("input".equals(getName())) {
                    // not usable yet
                    output.addItem(new TextFieldItem("Input text", "not implemented", 2000, 0));
                    skipSubTree();
                }
                else if ("img".equals(getName())) {
                    parseImg();
                }
                else if ("select".equals(getName())) {
                    parseSelect();
                }
                else if ("table".equals(getName())) {
                    parseTable();
                }
                else if (isFormattingTag()) {
                    parseFormattingTag();
                }
                else {
                    addWarning(P_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("p".equals(getName())) {
                    break;
                }
                else if (isFormattingTag()) {
                    // ignore
                }
                else {
                    addWarning(P_NESTED_TAGS);
                }
            }
            nextItem();
        }
        lastItemTerminated = true;
    }

    private void parseFormattingTag() throws Exception {
        final String FORMATTING_TAG_NESTED_TAGS =
            "expected text, formatting tag, <a>, <anchor>, <br>, <img>, or <table>";

        int depth = 1;
        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText().trim());
            }
            else if (getEventType() == START_TAG) {
                if (isFormattingTag()) {
                    depth++;
                }
                else if ("a".equals(getName())) {
                    parseA();
                }
                else if ("anchor".equals(getName())) {
                    parseAnchor();
                }
                else if ("br".equals(getName())) {
                    lastItemTerminated = true;
                }
                else if ("img".equals(getName())) {
                    parseImg();
                }
                else if ("table".equals(getName())) {
                    parseTable();
                }
                else {
                    addWarning(FORMATTING_TAG_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if (isFormattingTag()) {
                    depth--;
                    if (depth == 0) break;
                } else {
                    addWarning(FORMATTING_TAG_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }
         
            // // ignore script and style so they are not shown as text
            // else if (getEventType() == XmlPullParser.START_TAG && "script".equals(getName())) {
            //     addWarning("<script> is not supported, you are likely viewing an HTML page");
            //     skipSubTree();
            //     next();
            // }
            // else if (getEventType() == XmlPullParser.START_TAG && "style".equals(getName())) {
            //     addWarning("<style> is not supported, you are likely viewing an HTML page");
            //     skipSubTree();
            //     next();
            // }

    public void parseA() throws Exception {
        final String A_NESTED_TAGS = "expected text, <img>, <br>, or </a>";

        String text = "";
        String target = getAttributeValue(null, "href");

        if (target == null) {
            addWarning("<a> does not have 'href' attribute");
            target = "#";
        }

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                text += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("br".equals(getName())) {
                    text += "\n";
                }
                else if ("img".equals(getName())) {
                    parseImg();
                }
                else {
                    addWarning(A_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("a".equals(getName())) {
                    break;
                } else {
                    addWarning(A_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        output.addItem(new WmlAnchorItem(text.trim(), WmlAnchorItem.ACTION_GO, target));
    }

    public void parseAnchor() throws Exception {
        final String ANCHOR_NESTED_TAGS =
            "expected text, <br>, <go>, <img>, <prev>, <refresh>, or </anchor>";

        String text = "";
        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                text += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("br".equals(getName())) {
                    text += "\n";
                }
                else if ("go".equals(getName())) {
                    action = WmlAnchorItem.ACTION_GO;
                    target = getAttributeValue(null, "href");
                    if (target == null) {
                        addWarning("<go> does not have 'href' attribute");
                        target = "#";
                    }
                    skipSubTree();  // variables and postfield not supported
                }
                else if ("img".equals(getName())) {
                    parseImg();
                }
                else if ("prev".equals(getName())) {
                    action = WmlAnchorItem.ACTION_PREV;
                    skipSubTree();  // variables and postfield not supported
                }
                else if ("refresh".equals(getName())) {
                    action = WmlAnchorItem.ACTION_REFRESH;
                    skipSubTree();  // variables and postfield not supported
                }
                else {
                    addWarning(ANCHOR_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("anchor".equals(getName())) {
                    break;
                } else {
                    addWarning(ANCHOR_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        output.addItem(new WmlAnchorItem(text.trim(), action, target));
    }

    public void parseImg() throws Exception {
        output.addItem(new StringItem(getImgAltText()));
        skipSubTree();
    }

    public String getImgAltText() {
        String result = getAttributeValue(null, "alt");
        if (result != null) return result;
        result = getAttributeValue(null, "src");
        if (result != null) return result;

        addWarning("<img> tag does not have 'src' or 'alt' attribute");
        return "Image";
    }

    public void parseDo() throws Exception {
        final String DO_NESTED_TAGS =
            "expected <go>, <noop>, <prev>, <refresh>, or </do>";

        String text = getAttributeValue(null, "label");
        if (text == null) text = "";

        String type = getAttributeValue(null, "type");
        if (type == null) {
            addWarning("<do> does not have 'type' attribute");
            type = "unknown";
        }

        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                addWarning(DO_NESTED_TAGS);
                text += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("go".equals(getName())) {
                    action = WmlAnchorItem.ACTION_GO;
                    target = getAttributeValue(null, "href");
                    if (target == null) {
                        addWarning("<go> does not have 'href' attribute");
                        target = "#";
                    }
                    skipSubTree();  // variables and postfield not supported
                }
                else if ("noop".equals(getName())) {
                    action = WmlAnchorItem.ACTION_NONE;
                    skipSubTree();
                }
                else if ("prev".equals(getName())) {
                    action = WmlAnchorItem.ACTION_PREV;
                    skipSubTree();  // variables and postfield not supported
                }
                else if ("refresh".equals(getName())) {
                    action = WmlAnchorItem.ACTION_REFRESH;
                    skipSubTree();  // variables and postfield not supported
                }
                else {
                    addWarning(DO_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("do".equals(getName())) {
                    break;
                } else {
                    addWarning(DO_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }

        if (text.length() == 0) {
            text = type.substring(0, 1).toUpperCase() + type.substring(1);
        }
        
        int prio = commands.size() + 100;
        WmlCommand cmd = new WmlCommand(text, prio, action, target);
        commands.addElement(cmd);
        output.addCommand(cmd);
    }

    public void parseOnevent() throws Exception {
        addWarning("<onevent> is not supported yet");
        skipSubTree();
    }

    public void parseTimer() throws Exception {
        addWarning("<timer> is not supported yet");
        skipSubTree();
    }

    public void parseSelect() throws Exception {
        addWarning("<timer> is not supported yet");
        appendLine("[SELECT]");
        skipSubTree();
    }

    public void parseTable() throws Exception {
        final String TABLE_NESTED_TAGS = "expected text, <tr>, <td>, or </table>";

        lastItemTerminated = true;
        boolean isLineBegin = true;
        
        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                addWarning(TABLE_NESTED_TAGS);
                if (!isLineBegin) appendToLastItem(", ");
                appendToLastItem(getText().trim());
            }
            else if (getEventType() == START_TAG) {
                if ("tr".equals(getName())) {
                    isLineBegin = true;
                    lastItemTerminated = true;
                }
                else if ("td".equals(getName())) {
                    if (!isLineBegin) appendToLastItem(", ");
                    parseTd();
                    isLineBegin = false;
                }
                else {
                    addWarning(TABLE_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("table".equals(getName())) {
                    break;
                }
                else if ("tr".equals(getName())) {
                    // ignore
                }
                else if ("td".equals(getName())) {
                    // ignore
                }
                else {
                    addWarning(TABLE_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }

    public void parseTd() throws Exception {
        final String TD_NESTED_TAGS =
            "expected text, formatting tag, <a>, <anchor>, <br>, <img>, or </td>";

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText().trim());
            }
            else if (getEventType() == START_TAG) {
                if (isFormattingTag()) {
                    parseFormattingTag();
                }
                else if ("a".equals(getName())) {
                    parseA();
                }
                else if ("anchor".equals(getName())) {
                    parseAnchor();
                }
                else if ("br".equals(getName())) {
                    lastItemTerminated = true;
                }
                else if ("img".equals(getName())) {
                    parseImg();
                }
                else {
                    addWarning(TD_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_TAG) {
                if ("td".equals(getName())) {
                    break;
                } else {
                    addWarning(TD_NESTED_TAGS);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }

    // _________________________________________________________________________
    //
    //  Parsing utilities
    // _________________________________________________________________________
    //

    private void ignoreWhitespace() throws IOException {
        try {
            while (getEventType() == KXmlParser.TEXT && isWhitespace()) {
                next();
            }
        }
        catch (XmlPullParserException e) {}
    }

    private void require(int type, String text) throws Exception {
        ignoreWhitespace();
        require(type, null, text);
    }

    private Item getLastItem() {
        return (Item) output.items.lastElement();
    }

    private void appendToLastItem(String text) {
        if (
            lastItemTerminated || output.items.size() == 0 ||
            !(getLastItem() instanceof StringItem) || getLastItem() instanceof WmlAnchorItem
        ) {
            output.addItem(new StringItem(text));
            lastItemTerminated = false;
        } else {
            ((StringItem) getLastItem()).text += text;
        }
    }

    private void appendLine(String text) {
        output.addItem(new StringItem(text));
        lastItemTerminated = true;
    }

    private void nextItem() throws Exception {
        next();
        ignoreWhitespace();
    }

    // _________________________________________________________________________
    //
    //  Warning reporting
    // _________________________________________________________________________
    //

    public void addWarning(String text) {
        warnings.addElement(text);
        warningLocations.addElement(getPositionDescription());
    }

    private void createWarningsWml() {
        if (History.getCurrent().url.protocol.equals("warnings")) {
            return;
        }

        StringBuffer warningsBuf = new StringBuffer();
        warningsBuf.append(WmlTemplates.BEGIN)
            .append("<card title=\"Page warnings\">")
            .append("<p>Problems with &quot;")
            .append(Util.sanitizeWml(History.getCurrent().url.toString(false)))
            .append("&quot;:</p>");

        if (warnings.size() == 0) {
            warningsBuf.append("<p>No problems found with this page.</p>");
        }
        for (int i = 0; i < warnings.size(); i++) {
            String warn = (String) warnings.elementAt(i);
            String warnLoc = (String) warningLocations.elementAt(i);

            warningsBuf.append("<p>Warning: ")
                .append(Util.sanitizeWml(warn))
                .append("</p>")
                .append("<p>at ")
                .append(Util.sanitizeWml(warnLoc))
                .append("</p>");
        }

        warningsBuf.append(WmlTemplates.END);

        MainScreen.warningsWml = warningsBuf.toString();
    }
}
//#endif