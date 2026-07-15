package fi.gtrxac.bluewap.http;

import fi.gtrxac.bluewap.ui.*;

public class HTTPConfig {
	public static final String DEFAULT_ACCEPT = "text/vnd.wap.wml, image/vnd.wap.wbmp";
	public static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8";
	public static final String DEFAULT_USER_AGENT =
//#ifdef BLUEWAP_CLIENT
		"BlueWAP/" +
//#endif
//#ifdef BLUEWAP_SERVER
		"BlueWAPServer/" +
//#endif
		AppBase.instance.getAppProperty("MIDlet-Version");
}