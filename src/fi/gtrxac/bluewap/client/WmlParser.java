//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import fi.gtrxac.bluewap.ui.*;
import java.util.Vector;
import java.io.*;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

public class WmlParser extends KXmlParser {
    private String wml;
    private ListScreen output;
    private Vector warnings;
    private Vector warningLocations;

    private WmlParser(ListScreen output, String wml) throws Exception {
        this.output = output;
        this.wml = wml.trim();
        this.warnings = new Vector(5);
        this.warningLocations = new Vector(5);

        byte[] wmlBytes = Util.stringToBytes(this.wml);
        ByteArrayInputStream is = new ByteArrayInputStream(wmlBytes);
        setInput(is, null);
        defineEntityReplacementText("nbsp", " ");
    }

    public static void displayWml(ListScreen output, String wml, String cardId) {
        synchronized (History.getCurrent()) {
            output.removeAllItems();
            try {
                WmlParser p = new WmlParser(output, wml);
                p.parseWml(cardId);
                p.createWarningsWml();
            }
            catch (Exception e) {
                e.printStackTrace();
                output.addItem(new StringItem("Failed to load page:"));
                output.addItem(new StringItem(e.toString()));
            }
        }
    }

    private void parseWml(String cardId) throws Exception {
        boolean haveShownCard = false;

        try {
            nextTag();
        }
        catch (XmlPullParserException e) {
            addWarning("page does not begin with a WML tag, treating it as raw text");
            output.addItem(MainScreen.systemBrowserButton);
            output.addItem(new StringItem(wml));
            return;
        }

        try {
            require(XmlPullParser.START_TAG, "wml");
            nextTag();
        }
        catch (XmlPullParserException e) {
            addWarning("expected <wml>");
        }

        try {
            require(XmlPullParser.START_TAG, "head");
            skipSubTree();
            nextTag();
        }
        catch (XmlPullParserException e) {}

        while (getEventType() != XmlPullParser.END_DOCUMENT) {
            try {
                require(XmlPullParser.START_TAG, "card");

                // determine if this card is to be shown (specified card id or first card)
                String thisCardId = getAttributeValue(null, "id");

                if ((cardId == null && haveShownCard) || (cardId != null && !cardId.equals(thisCardId))) {
                    // skip this card
                    skipSubTree();
                    next();
                    ignoreWhitespace();
                    continue;
                }
                haveShownCard = true;
            }
            catch (XmlPullParserException e) {
                try {
                    require(XmlPullParser.END_TAG, "wml");
                    break;
                }
                catch (XmlPullParserException ee) {
                    addWarning("expected <card> or </wml>");
                }
            }
            
            String currentLinkTarget = null;

            try {
                while (true) {
                    next();
                    ignoreWhitespace();

                    if (getEventType() == XmlPullParser.END_DOCUMENT) {
                        addWarning("unexpected end of file");
                        break;
                    }
                    else if (getEventType() == XmlPullParser.END_TAG && "card".equals(getName())) {
                        nextTag();
                        break;
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "p".equals(getName())) {
                        parsePTag();
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "a".equals(getName())) {
                        parseATag();
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "anchor".equals(getName())) {
                        parseAnchorTag();
                    }
                    else if (getEventType() == XmlPullParser.TEXT) {
                        output.addItem(new StringItem(getText().trim()));
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "img".equals(getName())) {
                        parseImgTag();
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "input".equals(getName())) {
                        // not usable yet
                        output.addItem(new TextFieldItem("Input text", "not implemented", 2000, 0));
                        skipSubTree();
                        next();
                    }
                    // ignore script and style so they are not shown as text
                    else if (getEventType() == XmlPullParser.START_TAG && "script".equals(getName())) {
                        addWarning("<script> is not supported, you are likely viewing an HTML page");
                        skipSubTree();
                        next();
                    }
                    else if (getEventType() == XmlPullParser.START_TAG && "style".equals(getName())) {
                        addWarning("<style> is not supported, you are likely viewing an HTML page");
                        skipSubTree();
                        next();
                    }
                }
            }
            catch (XmlPullParserException e) {
                addWarning(e.toString());
            }

            // try {
            //     require(XmlPullParser.END_TAG, "card");
            //     nextTag();
            // }
            // catch (XmlPullParserException e) {
            //     addWarning(e, warnings, "expected </card>");
            // }
        }

        try {
            require(XmlPullParser.END_TAG, "wml");
        }
        catch (XmlPullParserException e) {
            addWarning("expected </wml>");
        }

        if (!haveShownCard) {
            addWarning((cardId == null) ? "no cards found" : "card '" + cardId + "' not found");
        }
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

    public void parsePTag() throws Exception {
        String text = "";

        while (true) {
            next();
            ignoreWhitespace();

            if (getEventType() == XmlPullParser.END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            else if (getEventType() == XmlPullParser.END_TAG && "p".equals(getName())) {
                break;
            }
            if (getEventType() == XmlPullParser.START_TAG && "a".equals(getName())) {
                break;
            }
            if (getEventType() == XmlPullParser.START_TAG && "anchor".equals(getName())) {
                break;
            }
            if (getEventType() == XmlPullParser.START_TAG && "input".equals(getName())) {
                break;
            }
            
            String addText = parseTextElement();
            if (addText != null) {
                text += addText;
            } else {
                addWarning("expected text, <img>, <br>, or </a>");
            }
        }
        if (text.length() != 0) {
            output.addItem(new StringItem(text));
        }
    }

    public void parseATag() throws Exception {
        String text = "";
        String target = getAttributeValue(null, "href");

        while (true) {
            next();
            ignoreWhitespace();

            if (getEventType() == XmlPullParser.END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            else if (getEventType() == XmlPullParser.END_TAG && "a".equals(getName())) {
                break;
            }
            
            String addText = parseTextElement();
            if (addText != null) {
                text += addText;
            } else {
                addWarning("expected text, <img>, <br>, or </a>");
            }
        }
        output.addItem(new WmlAnchorItem(text, WmlAnchorItem.ACTION_GO, target));
    }

    public void parseAnchorTag() throws Exception {
        String text = "";
        int action = WmlAnchorItem.ACTION_NONE;
        String target = null;

        while (true) {
            next();
            ignoreWhitespace();

            if (getEventType() == XmlPullParser.END_DOCUMENT) {
                addWarning("unexpected end of file");
                break;
            }
            else if (getEventType() == XmlPullParser.END_TAG && "anchor".equals(getName())) {
                break;
            }
            else if (getEventType() == XmlPullParser.START_TAG && "go".equals(getName())) {
                action = WmlAnchorItem.ACTION_GO;
                target = getAttributeValue(null, "href");
                if (target == null) {
                    addWarning("<go> does not have 'href' attribute");
                    target = "#";
                }
                skipSubTree();
                continue;
            }
            else if (getEventType() == XmlPullParser.START_TAG && "prev".equals(getName())) {
                action = WmlAnchorItem.ACTION_PREV;
                skipSubTree();
                continue;
            }
            else if (getEventType() == XmlPullParser.START_TAG && "refresh".equals(getName())) {
                action = WmlAnchorItem.ACTION_REFRESH;
                skipSubTree();
                continue;
            }
            
            String addText = parseTextElement();
            if (addText != null) {
                text += addText.trim();
            } else {
                addWarning("expected text, <img>, <br>, <go>, <prev>, <refresh>, or </anchor>");
            }
        }
        output.addItem(new WmlAnchorItem(text, action, target));
    }

    public String parseTextElement() throws Exception {
        if (getEventType() == XmlPullParser.TEXT) {
            return getText();
        }
        else if (getEventType() == XmlPullParser.START_TAG && "img".equals(getName())) {
            return getImgAltText();
        }
        else if (getEventType() == XmlPullParser.START_TAG && "br".equals(getName())) {
            return "\n";
        }
        return null;
    }

    public void parseImgTag() {
        output.addItem(new StringItem(getImgAltText()));
    }

    public String getImgAltText() {
        String result = getAttributeValue(null, "alt");
        if (result != null) return result;
        result = getAttributeValue(null, "src");
        if (result != null) return result;

        addWarning("<img> tag does not have 'src' or 'alt' attribute");
        return "Image";
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
        require(type, null, text);
    }

    public void addWarning(String text) {
        warnings.addElement(text);
        warningLocations.addElement(getPositionDescription());
    }
}
//#endif