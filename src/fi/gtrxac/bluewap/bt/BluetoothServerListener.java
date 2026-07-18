//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.bt;

import java.io.*;
import javax.microedition.io.*;

public interface BluetoothServerListener {
    /**
     * Called from a separate thread when a client device connects to this server.
     * Connection and stream closing is handled automatically.
     */
    public void bluetoothConnected(StreamConnection sc, DataInputStream dis, DataOutputStream dos);

    /**
     * Called when any error related to the Bluetooth server or connection occurs.
     */
    public void bluetoothError(Exception e);
}
//#endif