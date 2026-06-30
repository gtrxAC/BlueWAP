import java.util.*;

public class History implements Runnable {
    URL url;
    String wml;
    String card;
    boolean loaded;

    private static Vector list = new Vector();
    private static int currentIndex = -1;
    
    private History(String url, boolean relative) {
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
                this.url = new URL("jar://bs0dd.wml");
            }
            catch (Exception ee) {}

            this.wml = App.ERROR_WML_PREFIX + e.toString() + App.ERROR_WML_SUFFIX;
            this.card = null;
            this.loaded = false;
        }
    }

    public static void visit(String url, boolean relative) {
        // if we've gone back in the history, remove all entries after the current one
        if (currentIndex < list.size() - 1) {
            list.setSize(currentIndex + 1);
        }
        list.addElement(new History(url, relative));
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
        this.wml = App.LOADING_WML;
        this.loaded = false;
        new Thread(this).start();
    }

    public static History getCurrent() {
        if (list.size() == 0) return null;
        return (History) list.elementAt(currentIndex);
    }

    private static void screenChanged() {
        History curr = getCurrent();
        String card = (curr.loaded) ? curr.card : null;
        MainScreen.instance.displayWml(curr.wml, card);
    }

    public void run() {
        try {
            this.wml = fetch(getCurrent().url);
            this.loaded = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            this.wml = App.ERROR_WML_PREFIX + e.toString() + App.ERROR_WML_SUFFIX;
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
        byte[] bytes = HTTP.request("GET", url, null, null, false);
        return Util.bytesToString(bytes);
    }
}