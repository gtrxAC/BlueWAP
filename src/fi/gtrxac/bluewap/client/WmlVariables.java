package fi.gtrxac.bluewap.client;

import java.util.*;
import fi.gtrxac.bluewap.Util;

public class WmlVariables {
    private static Hashtable variables = new Hashtable();

    public static void set(String key, String value) {
        variables.put(key, value);
    }

    public static String get(String key) {
        return (String) variables.get(key);
    }

    public static String parse(String text) {
		for (Enumeration e = variables.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String value = get(key);
			
            text = Util.replace(text, "$" + key + " ", value);
            text = Util.replace(text, "$" + key + "\t", value);
            text = Util.replace(text, "$" + key + "\r", value);
            text = Util.replace(text, "$" + key + "\n", value);
            text = Util.replace(text, "$(" + key + ")", value);
		}
        return text;
    }
}