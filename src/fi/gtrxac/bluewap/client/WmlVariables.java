package fi.gtrxac.bluewap.client;

import java.util.*;
import fi.gtrxac.bluewap.Util;

public class WmlVariables {
    private static Hashtable variables = new Hashtable();

    public static void set(String key, String value) {
        variables.put(key, value);
    }

    public static String get(String key) {
        String result = (String) variables.get(key);
        if (result != null) return result;
        return "";
    }

    public static String get(String key, char escapeMode) {
        String result = get(key);

        if (escapeMode == 'e') return Util.urlEncode(result);
        if (escapeMode == 'u') return Util.urlDecode(result);
        return result;
    }
    
    public static void clear() {
        variables = new Hashtable();
    }

    private static boolean isValidVariableNameChar(char c, boolean allowDigits) {
        return Character.isLowerCase(c) || Character.isUpperCase(c) ||
            c == '_' || (allowDigits && Character.isDigit(c));
    }

    public static String parse(String text, boolean urlEscapeByDefault) {
        if (text == null) return "";

        // no vars -> return text on its own
        if (text.indexOf("$") == -1) return text;

        StringBuffer result = new StringBuffer();

        int i = 0;
        while (i < text.length()) {
            // find next var
            int varBeginIndex = text.indexOf("$", i);

            // no more vars -> append rest of the string
            if (varBeginIndex == -1) {
                result.append(text.substring(i));
                break;
            }

            // append everything until the var
            result.append(text.substring(i, varBeginIndex));

            // the '$' was the last char -> malformed, but we treat it as just a literal dollar sign
            if (varBeginIndex == text.length() - 1) {
                result.append('$');
                break;
            }
            int varNameBeginIndex = varBeginIndex + 1;

            // next char is also '$' -> escaped dollar sign
            if (text.charAt(varNameBeginIndex) == '$') {
                result.append('$');
                i += 2;
                continue;
            }
            
            // next char is '(' -> var name starts after it; allow defining escape modes later on
            boolean isParentheses = false;
            if (text.charAt(varNameBeginIndex) == '(') {
                isParentheses = true;
                varNameBeginIndex++;
            }

            // first char of variable name is invalid -> malformed, show '$' or '$(' literally
            if (!isValidVariableNameChar(text.charAt(varNameBeginIndex), false)) {
                if (isParentheses) {
                    result.append("$(");
                    i += 2;
                } else {
                    result.append('$');
                    i++;
                }
                continue;
            }

            int varNameEndIndex = varNameBeginIndex + 1;

            // find the end of the var name
            while (varNameEndIndex < text.length() && isValidVariableNameChar(text.charAt(varNameEndIndex), true)) {
                varNameEndIndex++;
            }

            char escapeMode = urlEscapeByDefault ? 'e' : 'n';

            // text ended there -> add variable
            if (varNameEndIndex == text.length()) {
                result.append(get(text.substring(varNameBeginIndex, varNameEndIndex), escapeMode));
                break;
            }

            int varEndIndex;

            // next char is ':' and this var is surrounded by parentheses
            // -> look for conversion method (e for escape, n for noesc, u for unesc)
            if (isParentheses && text.charAt(varNameEndIndex) == ':') {
                // text ended there -> malformed, add variable
                if (varNameEndIndex == text.length() - 1) {
                    result.append(get(text.substring(varNameBeginIndex, varNameEndIndex), escapeMode));
                    break;
                }

                char convChar = text.charAt(varNameEndIndex + 1);

                if (convChar == 'e' || convChar == 'E') escapeMode = 'e';
                else if (convChar == 'n' || convChar == 'N') escapeMode = 'n';
                else if (convChar == 'u' || convChar == 'U') escapeMode = 'u';
                // else malformed -> ignore and keep the default esc mode

                // we can skip until the ')' which ends off this var
                varEndIndex = text.indexOf(")", varNameEndIndex) + 1;
                
                // there is no ')' -> malformed, instead find the next char that is invalid for a var name
                if (varEndIndex == -1) {
                    varEndIndex = varNameEndIndex + 1;
                    while (varEndIndex < text.length() && isValidVariableNameChar(text.charAt(varEndIndex), false)) {
                        varEndIndex++;
                    }
                }
            }

            // there was a '(' -> the next char should be ')' so we'll assume it is, var ends after it
            else if (isParentheses) {
                varEndIndex = varNameEndIndex + 1;
            }

            // no parentheses -> variable ends where variable name ends
            else {
                varEndIndex = varNameEndIndex;
            }

            result.append(get(text.substring(varNameBeginIndex, varNameEndIndex), escapeMode));

            i = varEndIndex;
        }

        return result.toString();
    }

    /*
    static {
        test();
    }

    public static void testCase(String in, String expectedOut, boolean urlEscape) {
        String out = parse(in, urlEscape);
        boolean pass = out.equals(expectedOut);

        System.out.println("'" + in + "' -> '" + expectedOut + "' - " + (pass ? "OK" : ("FAIL  (actual: '" + out + "')")));
    }

    public static void test() {
        set("example", "variable test");

        testCase("simple text", "simple text", false);
        testCase("simple text", "simple text", true);
        testCase("simple $example text", "simple variable test text", false);
        testCase("simple $exampletext", "simple ", false);
        testCase("simple $(example)text", "simple variable testtext", false);
        testCase("simple $(example)text", "simple variable+testtext", true);
        testCase("simple $(example:n)text", "simple variable testtext", false);
        testCase("simple $(example:noesc)text", "simple variable testtext", false);
        testCase("simple $(example:NOEsc)text", "simple variable testtext", false);
        testCase("simple $(example:e)text", "simple variable+testtext", false);
        testCase("simple $(example:escape)text", "simple variable+testtext", false);
        testCase("simple $(example:EscAPE)text", "simple variable+testtext", false);

        set("a", "variable+test%20two");
        testCase("decode$a", "decodevariable+test%20two", false);
        testCase("decode$a", "decodevariable%2Btest%2520two", true);
        testCase("decode  $(a:escape) ", "decode  variable%2Btest%2520two ", false);
        testCase("decode$(a:unesc)", "decodevariable test two", true);
        testCase("decode$(a:UnEsC)", "decodevariable test two", true);
        testCase("decode$(a:U)", "decodevariable test two", true);

        clear();
    }
    */
}