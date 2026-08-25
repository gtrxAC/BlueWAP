package fi.gtrxac.bluewap;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import com.gtrxac.discord.HTTPQueue;

public class Settings {
    public static int fontSize = Font.SIZE_SMALL;
    public static Vector bookmarks;
    public static boolean getPairedDeviceNames = true;
    public static Hashtable deviceNameCache;

    private static void readData(DataInputStream dis) throws Exception {
        fontSize = dis.readUnsignedByte();
        HTTPQueue.maxSlots = dis.readUnsignedByte();

        int bookmarkCount = dis.readInt();
        bookmarks = new Vector(bookmarkCount);

        for (int i = 0; i < bookmarkCount; i++) {
            bookmarks.addElement(dis.readUTF());
        }

        getPairedDeviceNames = dis.readBoolean();

        int deviceNameCount = dis.readInt();
        deviceNameCache = new Hashtable(deviceNameCount);

        for (int i = 0; i < deviceNameCount; i++) {
            deviceNameCache.put(dis.readUTF(), dis.readUTF());
        }
    }

    private static void writeData(DataOutputStream dos) throws Exception {
        dos.writeByte(fontSize);
        dos.writeByte(HTTPQueue.maxSlots);

        dos.writeInt(bookmarks.size());

        for (int i = 0; i < bookmarks.size(); i++) {
            dos.writeUTF((String) bookmarks.elementAt(i));
        }

        dos.writeBoolean(getPairedDeviceNames);

        dos.writeInt(deviceNameCache.size());

        for (Enumeration e = deviceNameCache.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = (String) deviceNameCache.get(key);
            dos.writeUTF(key);
            dos.writeUTF(value);
        }
    }

    static {
        // Populate default bookmarks in case there are none saved
        bookmarks = new Vector();
        bookmarks.addElement("http://gtrxac.fi");
        bookmarks.addElement("http://wap.15pmm01.com");
        bookmarks.addElement("http://wap.ad");
        bookmarks.addElement("http://wap.hutch3g.eu");

        // same for device names
        deviceNameCache = new Hashtable();

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
        catch (EOFException e) {
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