//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.bt;

import java.io.*;
import javax.microedition.io.*;

public class BluetoothServerConnection extends Thread {
    private BluetoothServerListener listener;
    private StreamConnection sc;

    public BluetoothServerConnection(BluetoothServerListener listener, StreamConnection sc) {
        this.listener = listener;
        this.sc = sc;
    }

    public void run() {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = sc.openDataInputStream();
            dos = sc.openDataOutputStream();
            listener.bluetoothConnected(sc, dis, dos);
        }
        catch (Exception e) {
            listener.bluetoothError(e);
        }
        try { dis.close(); } catch (Exception e) {}
        try { dos.close(); } catch (Exception e) {}
        try { sc.close(); } catch (Exception e) {}
    }
}
//#endif