package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.Util;

public class ClockThread extends Thread {
    public ClockThread() {}

    public void run() {
        // Update the custom titlebar clock every time the clock's minute changes
        while (true) {
            int ms = 60001 - (int) (System.currentTimeMillis() % 60000);
            Util.sleep(ms);
            AppCanvas.instance.updateClock();
        }
    }
}