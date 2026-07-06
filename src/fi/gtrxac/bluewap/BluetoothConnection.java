package fi.gtrxac.bluewap;

import javax.microedition.io.*;
import java.io.*;

public class BluetoothConnection {
	public static final byte PROTOCOL_BASE = 1;
	public static final byte PROTOCOL_ADDED_RESULT_URL = 2;
	public static final byte PROTOCOL_CURRENT = 2;

    public StreamConnection conn;
    public DataInputStream input;
    public DataOutputStream output;
    
    public BluetoothConnection(StreamConnection c) {
        conn = c;
    }

    public void open() throws Exception {
        if (input == null) input = conn.openDataInputStream();
        if (output == null) output = conn.openDataOutputStream();
    }

    public void close() {
        try { input.close(); } catch (Exception e) {}
        try { output.close(); } catch (Exception e) {}
        try { conn.close(); } catch (Exception e) {}
    }
}