package fi.gtrxac.bluewap;

import javax.microedition.io.*;
import javax.microedition.lcdui.Font;
import java.io.*;
import java.util.*;

public class Util {
	public static byte[] stringToBytes(String str) {
		try {
			return str.getBytes("UTF-8");
		}
		catch (Exception e) {
			return str.getBytes();
		}
	}

	public static String bytesToString(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		}
		catch (Exception e) {
			return new String(bytes);
		}
	}

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static String[] split(String str, String delimiter) {
		if (str == null || str.length() == 0) {
			return new String[0];
		}
		Vector parts = _split(str, delimiter);
		String[] result = new String[parts.size()];
		parts.copyInto(result);
		return result;
	}

	public static Vector splitVec(String str, String delimiter) {
		if (str == null || str.length() == 0) {
			return new Vector();
		}
		return _split(str, delimiter);
	}

	private static Vector _split(String str, String delimiter) {
		Vector parts = new Vector();
		int start = 0;
		int index;
		
		while ((index = str.indexOf(delimiter, start)) != -1) {
			parts.addElement(str.substring(start, index));
			start = index + delimiter.length();
		}
		
		// Add the last part
		parts.addElement(str.substring(start));

		return parts;
	}

    public static String sanitizeWml(String text) {
        text = Util.replace(text, "&", "&amp;");
        text = Util.replace(text, "'", "&apos;");
        text = Util.replace(text, "\"", "&quot;");
        text = Util.replace(text, "<", "&lt;");
        return Util.replace(text, ">", "&gt;");
    }

	/**
	 * Get array of text lines to draw (word wrap)
	 * https://github.com/shinovon/JTube/blob/2.6.1/src/jtube/Util.java
	 */
	public static String[] wordWrap(String text, int maxWidth, Font font) {
		if (text == null || text.length() == 0 || text.equals(" ") || maxWidth < font.charWidth('W') + 2) {
			return new String[0];
		}
		text = replace(text, "\r", "");
		Vector v = new Vector(3);
		char[] chars = text.toCharArray();
		if (text.indexOf('\n') > -1) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				if (chars[i] == '\n') {
					v.addElement(text.substring(j, i));
					j = i + 1;
				}
			}
			v.addElement(text.substring(j, text.length()));
		} else {
			v.addElement(text);
		}
		for (int i = 0; i < v.size(); i++) {
			String s = (String) v.elementAt(i);
			if(font.stringWidth(s) >= maxWidth) {
				int i1 = 0;
				for (int i2 = 0; i2 < s.length(); i2++) {
					if (font.stringWidth(s.substring(i1, i2+1)) >= maxWidth) {
						boolean space = false;
						for (int j = i2; j > i1; j--) {
							char c = s.charAt(j);
							if (c == ' ' || (c >= ',' && c <= '/')) {
								space = true;
								v.setElementAt(s.substring(i1, j + 1), i);
								v.insertElementAt(s.substring(j + 1), i + 1);
								i += 1;
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!space) {
							i2 = i2 - 2;
							v.setElementAt(s.substring(i1, i2), i);
							v.insertElementAt(s.substring(i2), i + 1);
							i2 = i1 = i2 + 1;
							i += 1;
						}
					}
				}
			}
		}
		String[] arr = new String[v.size()];
		v.copyInto(arr);
		return arr;
	}

	// https://github.com/gtrxAC/discord-j2me/pull/5/commits/193c63f6a00b8e24da7a3582e9d1a92522f9940e
	public static byte[] readBytes(InputStream inputStream, int initialSize, int bufferSize, int expandSize) throws IOException {
		if (initialSize <= 0) initialSize = bufferSize;
		byte[] buf = new byte[initialSize];
		int count = 0;
		byte[] readBuf = new byte[bufferSize];
		int readLen;
		while ((readLen = inputStream.read(readBuf)) != -1) {
			if(count + readLen > buf.length) {
				byte[] newbuf = new byte[count + expandSize];
				System.arraycopy(buf, 0, newbuf, 0, count);
				buf = newbuf;
			}
			System.arraycopy(readBuf, 0, buf, count, readLen);
			count += readLen;
		}
		if(buf.length == count) {
			return buf;
		}
		byte[] res = new byte[count];
		System.arraycopy(buf, 0, res, 0, count);
		return res;
	}

	/**
	 * Reads a file's contents from the JAR into a string.
	 * @param name File name
	 * @return String representation of the file's entire contents (UTF-8)
	 * @throws Exception Failed to open file, e.g. it doesn't exist
	 */
	public static String readFile(String name) throws Exception {
		InputStream is = new Object().getClass().getResourceAsStream(name);
		if (is == null) throw new Exception("File not found: '" + name + "'");

		DataInputStream dis = new DataInputStream(is);
		StringBuffer buf = new StringBuffer();

		int ch;
		while ((ch = dis.read()) != -1) {
			buf.append((char) ch);
		}

		String result = buf.toString();
		try {
			return new String(result.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return result;
		}
	}

	/**
	 * Split RGB color to its components.
	 * @param color RGB color value
	 * @return An array of three integers where the first is the red value of the given color, second green, and third blue.
	 */
	public static int[] splitRGB(int color) {
		return new int[] {
			(color & 0x00FF0000) >> 16,
			(color & 0x0000FF00) >> 8,
			(color & 0x000000FF)
		};
	}
	
	/**
	 * Blend colors A and B. Alpha is disregarded.
	 * @param a First RGB color value to be blended
	 * @param b Second RGB color value to be blended
	 * @param aRatio The ratio of A to B in increments of 10%, for example, with aRatio = 7, the resulting color will be a blend of 70% A and 30% B.
	 * @return The blended RGB color value
	 */
	public static int blend(int a, int b, int aRatio) {
		int[] as = splitRGB(a);
		int[] bs = splitRGB(b);

		int bRatio = 10 - aRatio;
		int cR = (as[0]*aRatio/10 + bs[0]*bRatio/10) & 0xFF;
		int cG = (as[1]*aRatio/10 + bs[1]*bRatio/10) & 0xFF;
		int cB = (as[2]*aRatio/10 + bs[2]*bRatio/10) & 0xFF;

		return (cR << 16) | (cG << 8) | cB;
	}

	public static int contrast(int color, int compare) {
		int[] colorSplit = splitRGB(color);
		int[] compareSplit = splitRGB(compare);

		return
			Math.abs(colorSplit[0] - compareSplit[0]) +
			Math.abs(colorSplit[1] - compareSplit[1]) +
			Math.abs(colorSplit[2] - compareSplit[2]);
	}

	/**
	 * Get which of the colors (A or B) has a higher contrast against the 'compare' color. Alpha is disregarded.
	 */
	public static int higherContrast(int a, int b, int compare) {
		if (contrast(b, compare) > contrast(a, compare)) return b;
		return a;
	}

	// https://github.com/phd051199/MIDPlay/blob/main/src/Utils.java#L125

  	private static final String HEX_DIGITS = "0123456789ABCDEF";

	public static String urlEncode(String text) {
		if (text == null) {
		return "";
		}

		try {
		byte[] bytes = text.getBytes("UTF-8");
		StringBuffer result = new StringBuffer(bytes.length + (bytes.length >> 1));

		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xFF;
			char c = (char) b;

			if (c == ' ') {
			result.append('+');
			} else if (isUrlSafeCharacter(c)) {
			result.append(c);
			} else {
			result
				.append('%')
				.append(HEX_DIGITS.charAt((b >> 4) & 0xF))
				.append(HEX_DIGITS.charAt(b & 0xF));
			}
		}
		return result.toString();
		} catch (UnsupportedEncodingException e) {
		return "";
		}
	}

	private static boolean isUrlSafeCharacter(char c) {
		return (c >= 'A' && c <= 'Z')
			|| (c >= 'a' && c <= 'z')
			|| (c >= '0' && c <= '9')
			|| c == '-'
			|| c == '_'
			|| c == '.'
			|| c == '~';
	}
	
	public static boolean checkClass(String s) {
		try {
			Class.forName(s);
			return true;
		}
		catch (Throwable e) {}

		return false;
	}
}