//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.*;
import java.util.*;

public class History implements Runnable {
    public URL url;
    public String wml;
    public String card;
    public boolean loaded;
    public String contentType;
    public String postData;

    private static Vector list = new Vector();
    public static Vector menuUrls = new Vector();
    private static int currentIndex = -1;
    
    private History(String url, boolean relative, String postData) {
        this.postData = postData;

        if (url.equals("warnings://")) {
            try {
                this.url = new URL(url);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            this.wml = MainScreen.warningsWml;
            this.card = null;
            this.loaded = true;
            return;
        }

        try {
            History curr = getCurrent();
            if (relative) {
                this.url = new URL(url, curr.url);
            } else {
                this.url = new URL(url);
            }
            if (curr != null && this.url.isSamePage(curr.url)) {
                this.wml = curr.wml;
                this.card = this.url.card;
                this.loaded = true;
            } else {
                this.card = this.url.card;
                refresh();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                this.url = new URL("jar://home.wml");
            }
            catch (Exception ee) {}

            this.wml = WmlTemplates.ERROR_BEGIN + e.toString() + WmlTemplates.ERROR_END;
            this.card = null;
            this.loaded = false;
        }
    }

    private static void addMenuUrlsItem(String url) {
        while (menuUrls.size() > 8) {
            menuUrls.removeElementAt(0);
        }
        if (menuUrls.indexOf(url) != -1) return;
        menuUrls.addElement(url);
    }

    public static synchronized void visit(String url, boolean relative, Hashtable postfields, boolean isPost) {
        String postData = null;

        if (postfields != null && (isPost || postfields.size() >= 1)) {
            StringBuffer queryBuf = new StringBuffer();
            
            if (!isPost) {
                queryBuf.append(url);
                queryBuf.append((url.indexOf("?") != -1) ? "&" : "?");
            }

            boolean isFirst = true;

            for (Enumeration e = postfields.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String value = (String) postfields.get(key);
                
                if (!isFirst) queryBuf.append("&");
                
                queryBuf.append(Util.urlEncode(WmlVariables.parse(key)))
                    .append("=")
                    .append(Util.urlEncode(WmlVariables.parse(value)));

                isFirst = false;
            }

            if (isPost) postData = queryBuf.toString();
            else url = queryBuf.toString();
        }

        // if we've gone back in the history, remove all entries after the current one
        if (currentIndex < list.size() - 1) {
            list.setSize(currentIndex + 1);
        }
        History hist = new History(url, relative, postData);
        list.addElement(hist);
        addMenuUrlsItem(hist.url.toString(false));
        forward();
    }

    public static void back() {
        if (currentIndex <= 0) return;
        currentIndex--;
        screenChanged();
    }

    public static void forward() {
        if (currentIndex >= list.size() - 1) return;
        currentIndex++;
        screenChanged();
    }

    public void refresh() {
        this.wml = WmlTemplates.LOADING;
        this.loaded = false;
        screenChanged();
        new Thread(this).start();
    }

    public static synchronized History getCurrent() {
        if (list.size() == 0) return null;
        return (History) list.elementAt(currentIndex);
    }

    private static void screenChanged() {
        History curr = getCurrent();
        if (curr == null) return;
        
        String card = (curr.loaded) ? curr.card : null;
        MainScreen.instance.displayWml(curr.wml, card, curr.contentType);
    }

    public void run() {
        try {
            this.wml = fetch(getCurrent().url);
            this.loaded = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            this.wml = WmlTemplates.ERROR_BEGIN + e.toString() + WmlTemplates.ERROR_END;
            this.loaded = false;
        }
        screenChanged();
    }

    private String fetch(URL url) throws Exception {
        String urlStr = url.toString(false);

        if (url.protocol.equals("http") || url.protocol.equals("https")) {
            return fetchHttp(urlStr);
        }
        else if (url.protocol.equals("jar") || url.protocol.equals("file")) {
            return Util.readFile("/" + url.getPath());
        }
        else {
            throw new Exception("Unsupported protocol '" + url.protocol + "'");
        }
    }

    private String fetchHttp(String url) throws Exception {
        HTTP http = HTTP.createRequest((postData != null) ? "POST" : "GET", url);
        if (postData != null) http.setData(postData);
        contentType = http.getResponseHeader("Content-Type");
        String result = http.getResponseString();
        this.url = new URL(http.getUrl());
        return result;
    }
}
//#endif