package com.onceforall.pdfgenerator.json;

/**
 * Convert a web browser cookie specification to a JSONObject and back.
 * JSON and Cookies are both notations for name/value pairs.
 * @version 2
 */
public class Cookie {

    /**
     * Produce a copy of a string in which the characters '+', '%', '=', ';'
     * and control characters are replaced with "%hh". This is a gentle form
     * of URL encoding, attempting to cause as little distortion to the
     * string as possible. The characters '=' and ';' are meta characters in
     * cookies. By convention, they are escaped using the URL-encoding. This is
     * only a convention, not a standard. Often, cookies are expected to have
     * encoded values. We encode '=' and ';' because we must. We encode '%' and
     * '+' because they are meta characters in URL encoding.
     * @param string The source string.
     * @return       The escaped result.
     */
    public static String escape(String string) {
        char         c;
        String       s = string.trim();
        StringBuffer sb = new StringBuffer();
        int          len = s.length();
        for (int i = 0; i < len; i += 1) {
            c = s.charAt(i);
            if (c < ' ' || c == '+' || c == '%' || c == '=' || c == ';') {
                sb.append('%');
                sb.append(Character.forDigit((char)((c >>> 4) & 0x0f), 16));
                sb.append(Character.forDigit((char)(c & 0x0f), 16));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * Convert a cookie specification string into a JSONObject. The string
     * will contain a name value pair separated by '='. The name and the value
     * will be unescaped, possibly converting '+' and '%' sequences. The
     * cookie properties may follow, separated by ';', also represented as
     * name=value (except the secure property, which does not have a value).
     * The name will be stored under the key "name", and the value will be
     * stored under the key "value". This method does not do checking or
     * validation of the parameters. It only converts the cookie string into
     * a JSONObject.
     * @param string The cookie specification string.
     * @return A JSONObject containing "name", "value", and possibly other
     *  members.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
        String         n;
        JSONObject     o = new JSONObject();
        Object         v;
        JSONTokener x = new JSONTokener(string);
        o.put("name", x.nextTo('='));
        x.next('=');
        o.put("value", x.nextTo(';'));
        x.next();
        while (x.more()) {
            n = unescape(x.nextTo("=;"));
            if (x.next() != '=') {
                if (n.equals("secure")) {
                    v = Boolean.TRUE;
                } else {
                    throw x.syntaxError("Missing '=' in cookie parameter.");
                }
            } else {
                v = unescape(x.nextTo(';'));
                x.next();
            }
            o.put(n, v);
        }
        return o;
    }


    /**
     * Convert a JSONObject into a cookie specification string. The JSONObject
     * must contain "name" and "value" members.
     * If the JSONObject contains "expires", "domain", "path", or "secure"
     * members, they will be appended to the cookie specification string.
     * All other members are ignored.
     * @param o A JSONObject
     * @return A cookie specification string
     * @throws JSONException
     */
    public static String toString(JSONObject o) throws JSONException {
        StringBuffer sb = new StringBuffer();

        sb.append(escape(o.getString("name")));
        sb.append("=");
        sb.append(escape(o.getString("value")));
        if (o.has("expires")) {
            sb.append(";expires=");
            sb.append(o.getString("expires"));
        }
        if (o.has("domain")) {
            sb.append(";domain=");
            sb.append(escape(o.getString("domain")));
        }
        if (o.has("path")) {
            sb.append(";path=");
            sb.append(escape(o.getString("path")));
        }
        if (o.optBoolean("secure")) {
            sb.append(";secure");
        }
        return sb.toString();
    }

    /**
     * Convert <code>%</code><i>hh</i> sequences to single characters, and
     * convert plus to space.
     * @param s A string that may contain
     *      <code>+</code>&nbsp;<small>(plus)</small> and
     *      <code>%</code><i>hh</i> sequences.
     * @return The unescaped string.
     */
    public static String unescape(String s) {
        int len = s.length();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c == '+') {
                c = ' ';
            } else if (c == '%' && i + 2 < len) {
                int d = JSONTokener.dehexchar(s.charAt(i + 1));
                int e = JSONTokener.dehexchar(s.charAt(i + 2));
                if (d >= 0 && e >= 0) {
                    c = (char)(d * 16 + e);
                    i += 2;
                }
            }
            b.append(c);
        }
        return b.toString();
    }
}
