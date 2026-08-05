//#ifndef NO_BLUETOOTH
package fi.gtrxac.bluewap.bt;

import fi.gtrxac.bluewap.Util;
import javax.microedition.io.*;
import java.io.*;

public class ChunkedOutputStream extends OutputStream {
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_INTERVAL_MS = 15;

    private final OutputStream out;

    public ChunkedOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = Math.min(CHUNK_SIZE, len);
            out.write(b, off, n);
            out.flush();

            Util.sleep(CHUNK_INTERVAL_MS);

            off += n;
            len -= n;
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}
//#endif