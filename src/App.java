import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class App extends MIDlet {
	public static Display disp;
    public static App instance;
    
    private boolean started = false;

    public App() {
        instance = this;
    }

    public void startApp() {
        if (started) return;
        started = true;

        disp = Display.getDisplay(this);

        Form helloForm = new Form("Hello");
        helloForm.append(new StringItem(null, "Hello world!"));
        disp.setCurrent(helloForm);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}
}
