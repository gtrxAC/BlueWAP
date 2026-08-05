package fi.gtrxac.bluewap.server;

import java.util.*;

public class RequestData {
    public String method;
    public String url;
    public Hashtable headers;
    public byte[] data;
    public byte version;

    public RequestData(String method, String url, Hashtable headers, byte[] data, byte version) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.data = data;
        this.version = version;
    }
}