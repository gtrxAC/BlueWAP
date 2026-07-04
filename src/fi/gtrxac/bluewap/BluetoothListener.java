package fi.gtrxac.bluewap;

import javax.microedition.io.*;

public interface BluetoothListener {
    void btSearchCompleted(String[] deviceNames, String[] deviceURLs);
    void btError(Exception e);
}