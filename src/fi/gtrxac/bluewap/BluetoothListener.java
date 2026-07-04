package fi.gtrxac.bluewap;

import javax.microedition.io.*;

public interface BluetoothListener {
    void btSearchCompleted(String[] deviceNames, String[] deviceURLs);
    void btError(Exception e);

//#ifdef BLUETOOTH_SERVER
    /**
     * Called from a separate thread
     * @param conn
     */
    void btConnected(StreamConnection conn);
//#endif
}