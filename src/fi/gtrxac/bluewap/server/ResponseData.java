package fi.gtrxac.bluewap.server;

import java.util.*;

public class ResponseData {
    public String resultUrl;
    public Hashtable headers;
    public int code;
    public byte[] body;

    public ResponseData(String resultUrl, Hashtable headers, int code, byte[] body) {
        this.resultUrl = resultUrl;
        this.headers = headers;
        this.code = code;
        this.body = body;
    }
}