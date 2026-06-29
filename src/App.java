import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class App extends MIDlet implements Runnable {
	public static Display disp;
    public static App instance;
    
    private boolean started = false;

    static URL currentUrl;
    static String currentWml;
    static String currentCard;

    static final String WML_BEGIN =
        "<?xml version=\"1.0\" encoding='utf-8'?>" +
        "<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">" +
        "<wml>" +
        "<head>" +
        "</head>";

    static final String LOADING_WML =
        WML_BEGIN +
        "<card title=\"Loading\">" +
        "<p>Loading...</p>" +
        "</card>" +
        "</wml>";

    static final String ERROR_WML_PREFIX =
        WML_BEGIN +
        "<card title=\"Error\">" +
        "<p>An error occurred:</p>" +
        "<p>";

    static final String ERROR_WML_SUFFIX =
        "</p>" +
        "</card>" +
        "</wml>";

    public App() {
        instance = this;
    }

    public void startApp() {
        if (started) return;
        started = true;

        pushScreen(new MainScreen());
        visit("jar://bs0dd.wml", false);

        disp = Display.getDisplay(this);
        disp.setCurrent(AppCanvas.instance);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}


    private static Stack screens = new Stack();

    public static void pushScreen(Screen s) {
        screens.push(s);
        AppCanvas.instance.updateCommands();
        repaint();
    }

    public static void popScreen() {
        screens.pop();
        AppCanvas.instance.updateCommands();
        repaint();
    }

    public static void replaceScreen(Screen s) {
        screens.pop();
        pushScreen(s);
    }

    public static Screen getCurrentScreen() {
        if (screens.empty()) return null;
        return (Screen) screens.peek();
    }

    public static void resizeAllScreens() {
        for (int i = 0; i < screens.size(); i++) {
            Screen s = (Screen) screens.elementAt(i);
            s.needsResize = true;
        }
        repaint();
    }

    public static void repaint() {
        AppCanvas.instance.repaint();
    }

    public void run() {
        Screen curr = App.getCurrentScreen();
        if (curr instanceof MainScreen) {
            ((MainScreen) curr).displayWml(LOADING_WML, null);

            try {
                currentCard = currentUrl.card;
                currentWml = fetch(currentUrl);
                ((MainScreen) curr).displayWml(currentWml, currentCard);
            }
            catch (Exception e) {
                e.printStackTrace();
                ((MainScreen) curr).displayWml(ERROR_WML_PREFIX + e.toString() + ERROR_WML_SUFFIX, null);
            }
        }
    }

    public String fetch(URL url) throws Exception {
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

    public String fetchHttp(String url) throws Exception {
        byte[] bytes = HTTP.request("GET", url, null, null, false);
        return Util.bytesToString(bytes);
    }

    public static void visit(String url, boolean relative) {
        try {
            if (relative) {
                currentUrl = new URL(url, currentUrl);
            } else {
                currentUrl = new URL(url);
            }
        }
        catch (Exception e) {
            Screen curr = App.getCurrentScreen();
            if (curr instanceof MainScreen) {
                ((MainScreen) curr).displayWml(ERROR_WML_PREFIX + e.toString() + ERROR_WML_SUFFIX, null);
            }
        }
        currentWml = null;
        new Thread(instance).start();
    }
}
