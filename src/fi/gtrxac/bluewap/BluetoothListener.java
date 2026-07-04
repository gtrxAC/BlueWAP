package fi.gtrxac.bluewap;

import javax.microedition.io.*;

public interface BluetoothListener {
    public void btSearchCompleted(String[] deviceNames, String[] deviceURLs);
    public void btError(Exception e);

//#ifdef BLUETOOTH_SERVER
    /**
     * Called from a separate thread
     * @param conn
     */
    public void btConnected(StreamConnection conn);
//#endif
}