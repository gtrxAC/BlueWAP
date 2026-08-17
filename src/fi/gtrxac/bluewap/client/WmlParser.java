//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.util.*;
import java.io.*;
import org.kxml2.io.*;
import org.xmlpull.v1.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

public class WmlParser extends KXmlParser {
    public Vector output;
    private ListScreen outputScreen;
    private byte[] wml;
    private String encoding;
    private String cardId;
    private String contentType;
    private boolean keepInputs;

    private boolean haveShownCard;
    private boolean lastItemTerminated;
    private boolean isHtml;
    private int currentAlign;

    private Vector warnings;
    private Vector warningLocations;

    public static Vector commands = new Vector(5);

    private WmlParser(ListScreen outputScreen, byte[] wml, String cardId, String contentType, boolean keepInputs) throws Exception {
        // If card name is empty, treat it as null -> always show the first card
        if ("".equals(cardId)) cardId = null;

        this.output = new Vector();
        this.outputScreen = outputScreen;
        this.wml = wml;
        this.cardId = cardId;
        this.contentType = contentType;
        this.keepInputs = keepInputs;

        this.haveShownCard = false;
        this.lastItemTerminated = false;
        this.isHtml = false;
        this.currentAlign = Graphics.TOP | Graphics.LEFT;

        this.warnings = new Vector(5);
        this.warningLocations = new Vector(5);

        commands.setSize(0);

        encoding = null;
        if (contentType != null) {
            encoding = Util.getCharsetFromContentType(contentType);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(wml);
        setInput(is, encoding);
        encoding = getDetectedEncoding();
        defineEntityReplacementText("nbsp", " ");
        defineEntityReplacementText("copy", "©");
    }

    public static void displayWml(ListScreen outputScreen, byte[] wml, String cardId, String contentType, boolean keepInputs) {
        synchronized (History.getCurrent()) {
            if (!keepInputs) {
                outputScreen.removeAllItems();
                outputScreen.addItem("Parsing...");
            }
            WmlParser p = null;

            try {
                p = new WmlParser(outputScreen, wml, cardId, contentType, keepInputs);
                p.setFeature("http://xmlpull.org/v1/doc/relaxedrelaxedrelaxed", true);
                p.parseWml();
            }
            catch (Exception e) {
                e.printStackTrace();
                outputScreen.removeAllItems();
                outputScreen.addItem("Failed to load page:");
                outputScreen.addItem(e.toString());
            }

            if (p != null) {
                // trailing whitespace in items may cause blank lines to appear, so trim them
                for (int i = 0; i < p.output.size(); i++) {
                    Object item = p.output.elementAt(i);
                    if (!(item instanceof WmlStringItem)) continue;

                    WmlStringItem strItem = (WmlStringItem) item;
                    strItem.setRawText(Util.trimRight(strItem.getRawText()));
                }

                outputScreen.removeAllItems();
                outputScreen.addItems(p.output);
            }

            try {
                p.createWarningsWml();
            }
            catch (Exception e) {}
        }
    }

    private boolean isContentType(String type) {
        return contentType != null && contentType.startsWith(type);
    }

    private void parseWml() throws Exception {
        // go to the first tag, if fails then the page is not xml
        try {
            nextTag();
        }
        catch (XmlPullParserException e) {
            // if it's html/wml but doesn't have the xml declaration crap,
            // then skip the first tag (e.g. doctype html) and hope for the best
            boolean isWmlHtmlContentType = isContentType("text/vnd.wap.wml") || isContentType("text/html");
            String wmlStr = Util.bytesToString(wml, encoding);

            if (isWmlHtmlContentType && wmlStr.startsWith("<")) {
                addWarning("unrecognized header");
                nextTag();
            }
            // else show the file as image or text
            else {
                if (!isWmlHtmlContentType && !isContentType("text/plain")) {
                    output.addElement(MainScreen.systemBrowserButton);
                }

                if (isContentType("image/")) {
                    output.addElement(new WmlImageItem(History.getCurrent().url.toString(false), null, "", currentAlign));
                } else {
                    addWarning("page does not begin with a tag, treating it as raw text");
                    output.addElement(wmlStr);
                }
                return;
            }
        }

        // first tag (ignoring xml header and doctype) is <wml>
        try {
            require(START_TAG, "wml");
            nextTag();
        }
        catch (XmlPullParserException e) {
            try {
                require(START_TAG, "html");
                isHtml = true;
                nextTag();
            }
            catch (XmlPullParserException ee) {
                addWarning("expected <wml> or <html>");
            }
        }

        // <wml> nested tags: card (any amount), head (up to 1), template (up to 1)
        boolean haveHead = false;
        boolean haveTemplate = false;

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed("wml");
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
                else if (isHtml && "body".equals(getName())) {
                    parseP("body");
                }
                else {
                    warnNotAllowed("wml");
                    skipSubTree();
                }
            }
            else if (getEventType() == END_TAG) {
                if ((isHtml ? "html" : "wml").equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("wml");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }

        if (!isHtml && !haveShownCard) {
            addWarning((cardId == null) ? "no cards found" : "card '" + cardId + "' not found");
        }
    }

    private void parseCard() throws Exception {
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
                warnNotAllowed("card");
                appendToLastItem(getText(), Fonts.plain);
            }
            if (getEventType() == START_TAG) {
                if ("p".equals(getName())) {
                    parseP("p");
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
                else if (parseTagInP()) {
                    // already handled, ignore but warn
                    addWarning("<" + getName() + "> should be inside <p>");
                }
                else {
                    warnNotAllowed("card");
                    skipSubTree();
                }
            }
            else if (getEventType() == END_TAG) {
                if ("card".equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("card");
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

    private void parseP(String tagName) throws Exception {
        String align = getAttributeValue(null, "align");

        if (align == null || "left".equals(align)) {
            currentAlign = Graphics.TOP | Graphics.LEFT;
        }
        else if ("center".equals(align)) {
            currentAlign = Graphics.TOP | Graphics.HCENTER;
        }
        else if ("right".equals(align)) {
            currentAlign = Graphics.TOP | Graphics.RIGHT;
        }
        else {
            addWarning("'align' should be 'left', 'center', or 'right'");
            currentAlign = Graphics.TOP | Graphics.LEFT;
        }

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText(), Fonts.plain);
            }
            if (getEventType() == START_TAG) {
                if (parseTagInP()) {
                    // already handled, ignore
                }
                else {
                    warnNotAllowed(tagName);
                }
            }
            else if (getEventType() == END_TAG) {
                if (tagName.equals(getName())) {
                    break;
                }
                else if (isFormattingTag()) {
                    // ignore
                }
                else {
                    warnNotAllowed(tagName);
                }
            }
            nextItem();
        }
        lastItemTerminated = true;
    }

    private boolean parseTagInP() throws Exception {
        if ("a".equals(getName())) {
            parseA();
            return true;
        }
        if ("anchor".equals(getName())) {
            parseAnchor();
            return true;
        }
        if ("br".equals(getName())) {
            lastItemTerminated = true;
            return true;
        }
        if ("do".equals(getName())) {
            parseDo();
            return true;
        }
        if ("fieldset".equals(getName())) {
            lastItemTerminated = true;
            return true;
        }
        if ("input".equals(getName())) {
            parseInput();
            return true;
        }
        if ("img".equals(getName())) {
            parseImg();
            return true;
        }
        if ("select".equals(getName())) {
            parseSelect();
            return true;
        }
        if ("table".equals(getName())) {
            parseTable();
            return true;
        }
        if (isFormattingTag()) {
            parseFormattingTag();
            return true;
        }
        if (isHtml && ("script".equals(getName()) || "style".equals(getName()))) {
            skipSubTree();
            return true;
        }
        if (isHtml && ",p,div,h1,h2,h3,h4,h5,h6,".indexOf("," + getName() + ",") != -1) {
            parseP(getName());
            return true;
        }
        return false;
    }

    private Font formattingTagToFont(String tagName) {
        if (tagName.equals("b")) return Fonts.bold;
        if (tagName.equals("big")) return Fonts.bold;
        // if (tagName.equals("i")) return Fonts.italic;
        if (tagName.equals("strong")) return Fonts.bold;
        if (tagName.equals("u")) return Fonts.underlined;
        return Fonts.plain;
    }

    private void parseFormattingTag() throws Exception {
        String tagName = getName();
        Font font = formattingTagToFont(tagName);
        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText(), font);
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
                else if ("table".equals(getName())) {
                    parseTable();
                }
                else {
                    warnNotAllowed(tagName);
                }
            }
            else if (getEventType() == END_TAG) {
                if (tagName.equals(getName())) {
                    break;
                }
                else {
                    warnNotAllowed(tagName);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }

    public void parseA() throws Exception {
        String text = "";
        String target = getAttributeRequired("href");
        if (target == null) target = "#";

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
                    text += parseImgInAnchor();
                }
                else {
                    warnNotAllowed("a");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("a".equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("a");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        addAnchorItem(text, WmlAnchorItem.ACTION_GO, target, null, null, false);
    }

    public void parseAnchor() throws Exception {
        String text = "";
        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;
        Hashtable postfields = new Hashtable(3);
        Hashtable setvars = new Hashtable(3);
        boolean isPost = false;

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
                    target = getGoTarget();
                    isPost = getGoMethod();
                    text += parseGo(postfields, setvars);
                }
                else if ("img".equals(getName())) {
                    text += parseImgInAnchor();
                }
                else if ("prev".equals(getName())) {
                    action = WmlAnchorItem.ACTION_PREV;
                    skipSubTree();  // setvar not supported
                }
                else if ("refresh".equals(getName())) {
                    action = WmlAnchorItem.ACTION_REFRESH;
                    skipSubTree();  // setvar not supported
                }
                else {
                    warnNotAllowed("anchor");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("anchor".equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("anchor");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        addAnchorItem(text, action, target, postfields, setvars, isPost);
    }

    public String parseGo(Hashtable postfieldsOutput, Hashtable setvarsOutput) throws Exception {
        String addText = "";

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed("go");
                addText += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("postfield".equals(getName())) {
                    parsePostfieldOrSetvar(postfieldsOutput);
                }
                else if ("setvar".equals(getName())) {
                    parsePostfieldOrSetvar(setvarsOutput);
                }
                else {
                    warnNotAllowed("go");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("go".equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("go");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        return addText;
    }

    public void parsePostfieldOrSetvar(Hashtable output) throws Exception {
        String name = getAttributeRequired("name");
        String value = getAttributeRequired("value");

        if (name != null && value != null) {
            output.put(name, value);
        }
        skipSubTree();
    }

    public void addAnchorItem(String text, int action, String target, Hashtable postfields, Hashtable setvars, boolean isPost) {
        if (text == null || text.trim().length() == 0) {
            text = "Link";
        }
        output.addElement(new WmlAnchorItem(text.trim(), currentAlign, action, target, postfields, setvars, isPost));
    }

    public String parseImgInAnchor() throws Exception {
        String result = getImgAltText();
        skipSubTree();
        return result;
    }

    public void parseImg() throws Exception {
        String src = getAttributeRequired("src");
        String localsrc = getAttributeValue(null, "localsrc");

        if (src != null) {
            output.addElement(new WmlImageItem(src, localsrc, getImgAltText(), currentAlign));
        } else {
            output.addElement(new WmlStringItem(getImgAltText(), currentAlign));
        }
        skipSubTree();
    }

    public String getImgAltText() {
        String result = getAttributeValue(null, "alt");
        if (result != null) return result;
        result = getAttributeValue(null, "src");
        if (result != null) return result;

        addWarning("<img> does not have 'src' or 'alt' attribute");
        return "Image";
    }

    public void parseInput() throws Exception {
        String name = getAttributeRequired("name");
        String maxlengthStr = getAttributeValue(null, "maxlength");
        String value = getInputOrSelectValue(name);

        int maxlength = 2000;

        if (maxlengthStr != null) {
            try {
                maxlength = Integer.parseInt(maxlengthStr);
                if (maxlength < 1) throw new Exception();
            }
            catch (Exception e) {
                addWarning("'maxlength' should be a positive integer");
                maxlength = 2000;
            }
        }
        
        output.addElement(new WmlInputItem(name, value, maxlength));
        skipSubTree();

        if (name != null) {
            WmlVariables.set(name, value);
        }
    }

    private String getInputOrSelectValue(String name) {
        if (keepInputs && name != null && WmlVariables.has(name)) {
            return WmlVariables.get(name);
        }
        String result = getAttributeValue(null, "value");
        if (result == null) return "";
        return result;
    }

    public void parseDo() throws Exception {
        String text = getAttributeValue(null, "label");
        if (text == null) text = "";

        String type = getAttributeRequired("type");
        if (type == null) type = "unknown";

        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;
        Hashtable postfields = new Hashtable(3);
        Hashtable setvars = new Hashtable(3);
        boolean isPost = false;

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed("do");
                text += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("go".equals(getName())) {
                    action = WmlAnchorItem.ACTION_GO;
                    target = getGoTarget();
                    isPost = getGoMethod();
                    text += parseGo(postfields, setvars);
                }
                else if ("noop".equals(getName())) {
                    action = WmlAnchorItem.ACTION_NONE;
                    skipSubTree();
                }
                else if ("prev".equals(getName())) {
                    action = WmlAnchorItem.ACTION_PREV;
                    text += parsePrevOrRefresh(setvars);
                }
                else if ("refresh".equals(getName())) {
                    action = WmlAnchorItem.ACTION_REFRESH;
                    text += parsePrevOrRefresh(setvars);
                }
                else {
                    warnNotAllowed("do");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("do".equals(getName())) {
                    break;
                } else {
                    warnNotAllowed("do");
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
        WmlCommand cmd = new WmlCommand(text, prio, action, target, postfields, setvars, isPost);
        commands.addElement(cmd);
        outputScreen.addCommand(cmd);
    }

    public String getGoTarget() {
        String target = getAttributeRequired("href");
        if (target != null) return target;
        return "#";
    }

    public boolean getGoMethod() {
        String method = getAttributeValue(null, "method");
        if (method == null) return false;
        if (method.equals("post")) return true;
        if (method.equals("get")) return false;

        addWarning("<go> method should be 'get' or 'post'");
        return false;
    }

    public String parsePrevOrRefresh(Hashtable setvarsOutput) throws Exception {
        String addText = "";
        String tagName = getName();

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed(tagName);
                addText += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("setvar".equals(getName())) {
                    parsePostfieldOrSetvar(setvarsOutput);
                } else {
                    warnNotAllowed(tagName);
                }
            }
            else if (getEventType() == END_TAG) {
                if (tagName.equals(getName())) {
                    break;
                } else {
                    warnNotAllowed(tagName);
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
        return addText;
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
        String name = getAttributeRequired("name");
        String iname = getAttributeValue(null, "iname");
        String value = getInputOrSelectValue(name);

        WmlOptionGroup group = new WmlOptionGroup(name, iname);
        int optgroups = 0;

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed("select");
            }
            else if (getEventType() == START_TAG) {
                if ("option".equals(getName())) {
                    parseOption(group);
                }
                else if ("optgroup".equals(getName())) {
                    optgroups++;
                }
                else {
                    warnNotAllowed("select");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("select".equals(getName())) {
                    break;
                }
                else if ("optgroup".equals(getName())) {
                    if (optgroups <= 0) {
                        addWarning("malformed <optgroup> tags");
                    }
                    optgroups--;
                }
                else {
                    warnNotAllowed("select");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }

        if (optgroups != 0) {
            addWarning("unbalanced <optgroup> tags");
        }

        if (name != null && WmlVariables.has(name)) {
            group.setTickedValue(value);
        }
        else {
            group.setTickedIndex(0);
        }
    }

    public void parseOption(WmlOptionGroup group) throws Exception {
        String text = "";

        String value = getAttributeValue(null, "value");
        if (value == null) value = "";

        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                text += getText().trim();
            }
            else if (getEventType() == START_TAG) {
                if ("onevent".equals(getName())) {
                    parseOnevent();
                }
                else {
                    warnNotAllowed("option");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("option".equals(getName())) {
                    output.addElement(new WmlOptionItem(group, text, value));
                    break;
                }
                else {
                    warnNotAllowed("option");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }

    public void parseTable() throws Exception {
        lastItemTerminated = true;
        
        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                warnNotAllowed("table");
                appendLine(getText());
            }
            else if (getEventType() == START_TAG) {
                if ("tr".equals(getName())) {
                    output.addElement(new SpacerItem());
                }
                else if ("td".equals(getName()) || (isHtml && "th".equals(getName()))) {
                    lastItemTerminated = true;
                    parseTd(getName());
                    lastItemTerminated = true;
                }
                else {
                    warnNotAllowed("table");
                }
            }
            else if (getEventType() == END_TAG) {
                if ("table".equals(getName())) {
                    output.addElement(new SpacerItem());
                    break;
                }
                else if ("tr".equals(getName())) {
                    // ignore
                }
                else if ("td".equals(getName()) || (isHtml && "th".equals(getName()))) {
                    // ignore
                }
                else {
                    warnNotAllowed("table");
                }
            }
            else if (getEventType() == END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            nextItem();
        }
    }

    public void parseTd(String tagName) throws Exception {
        nextItem();

        while (true) {
            if (getEventType() == TEXT) {
                appendToLastItem(getText(), Fonts.plain);
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
                    warnNotAllowed("td");
                }
            }
            else if (getEventType() == END_TAG) {
                if (tagName.equals(getName())) {
                    break;
                }
                else {
                    warnNotAllowed("td");
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

    public String getName() {
        String result = super.getName();
        if (result == null) return null;
        
        String resultLower = result.toLowerCase();
        if (!isHtml && !result.equals(resultLower)) {
            addWarning("<" + result + "> should be in lowercase");
        }
        return resultLower;
    }

    private String getAttributeRequired(String attributeName) {
        String result = getAttributeValue(null, attributeName);
        if (result == null) {
            addWarning("<" + getName() + "> does not have '" + attributeName + "' attribute");
        }
        return result;
    }

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

        try {
            require(type, null, text);
        }
        catch (XmlPullParserException e) {
            require(type, null, null);
            // allow case-insensitive match via getName
            if (!getName().equals(text)) throw e;
        }
    }

    private Item getLastItem() {
        return (Item) output.lastElement();
    }

    private void appendToLastItem(String text, Font font) {
        text = Util.removeDuplicateWhitespace(text);
        RichTextItem item;

        if (lastItemTerminated || output.size() == 0 || !(getLastItem() instanceof RichTextItem)) {
            item = new RichTextItem();
            output.addElement(item);
            lastItemTerminated = false;
        } else {
            item = (RichTextItem) getLastItem();
        }
        item.addStringPart(Util.trimLeft(text), font);
    }

    private void appendLine(String text) {
        RichTextItem item = new RichTextItem();
        item.addStringPart(text, Fonts.plain);
        output.addElement(item);
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

    public String tagToString() throws Exception {
        if (getEventType() == START_TAG) return '<' + getName() + '>';
        if (getEventType() == END_TAG) return "</" + getName() + '>';
        return TYPES[getEventType()].toLowerCase();
    }

    public void warnNotAllowed(String containingTag) throws Exception {
        // ignore <br> which might get parsed as multiple tags
        // depending on the way it's written and may cause false errors
        if ("br".equals(getName())) return;

        addWarning(tagToString() + " not allowed inside <" + containingTag + '>');
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
        } else {
            warningsBuf.append("<table>");
        }

        for (int i = 0; i < warnings.size(); i++) {
            String warn = (String) warnings.elementAt(i);
            String warnLoc = (String) warningLocations.elementAt(i);

            warningsBuf.append("<tr><td>")
                .append(Util.sanitizeWml(warn))
                .append("<br/>at ")
                .append(Util.sanitizeWml(warnLoc))
                .append("</td></tr>");
        }

        if (warnings.size() != 0) {
            warningsBuf.append("</table>");
        }

        warningsBuf.append(WmlTemplates.END);

        MainScreen.warningsWml = Util.stringToBytes(warningsBuf.toString());
    }
}
//#endif