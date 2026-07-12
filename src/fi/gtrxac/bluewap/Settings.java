package fi.gtrxac.bluewap;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;

import javax.microedition.lcdui.*;

public class Settings {
    public static int fontSize = Font.SIZE_SMALL;

    private static void readData(DataInputStream dis) throws Exception {
        fontSize = dis.readUnsignedByte();
    }

    private static void writeData(DataOutputStream dos) throws Exception {
        dos.writeByte(fontSize);
    }

    static {
        load();
    }

    private static void load() {
        RecordStore rms = null;
        ByteArrayInputStream is = null;
        DataInputStream dis = null;

        try {
            rms = RecordStore.openRecordStore("a", false);
            byte[] record = rms.getRecord(1);
            is = new ByteArrayInputStream(record);
            dis = new DataInputStream(is);

            readData(dis);
        }
        catch (RecordStoreNotFoundException e) {
            // ignore
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Util.closeRecordStore(rms);
            try { is.close(); } catch (Exception e) {}
            try { dis.close(); } catch (Exception e) {}
        }
    }

    public static void save() {
        RecordStore rms = null;
        ByteArrayOutputStream os = null;
        DataOutputStream dos = null;

        try {
            rms = RecordStore.openRecordStore("a", true);
            os = new ByteArrayOutputStream();
            dos = new DataOutputStream(os);

            writeData(dos);
            Util.setOrAddRecord(rms, 1, os.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Util.closeRecordStore(rms);
            try { os.close(); } catch (Exception e) {}
            try { dos.close(); } catch (Exception e) {}
        }
    }
}