//#ifdef BLUEWAP_SERVER
package fi.gtrxac.bluewap.server;

import javax.microedition.io.*;
import java.io.*;

public class BluetoothConnection {
    StreamConnection conn;
    DataInputStream input;
    DataOutputStream output;
    
    public BluetoothConnection(StreamConnection c) {
        conn = c;
    }

    public void open() throws Exception {
        input = conn.openDataInputStream();
        output = conn.openDataOutputStream();
    }

    public void close() {
        try { input.close(); } catch (Exception e) {}
        try { output.close(); } catch (Exception e) {}
        try { conn.close(); } catch (Exception e) {}
    }
}
//#endif