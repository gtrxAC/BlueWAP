package fi.gtrxac.bluewap.bt;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;

public interface BluetoothServerListener {
    public void bluetoothConnected(StreamConnection sc);
    public void bluetoothError(Exception e);
}