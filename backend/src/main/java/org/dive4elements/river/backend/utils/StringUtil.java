/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;

import java.net.URLEncoder;
import java.net.URLDecoder;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;


public final class StringUtil {
    final static String NUMBER_SEPERATOR = ";";
    final static String LINE_SEPERATOR = ":";

    private StringUtil() {
    }

    public static final String double2DArrayToString(double[][] values) {

        if (values == null) {
            throw new IllegalArgumentException("keine double[][]-Werte");
        }

        StringBuilder strbuf = new StringBuilder();

        for (int i=0; i < values.length; i++) {
            if (i>0) {
                strbuf.append(LINE_SEPERATOR);
            }
            for (int j=0; j < values[i].length; j++) {
                if (j > 0) {
                    strbuf.append(NUMBER_SEPERATOR);
                }
                strbuf.append(values[i][j]);
            }
        }

        return strbuf.toString();
    }

    public static final double[][] stringToDouble2DArray(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        String[] lineSplit = str.split(LINE_SEPERATOR);
        double[][] array2D = new double[lineSplit.length][];
        for (int i=0; i < lineSplit.length; i++) {
            String[] numberSplit =  lineSplit[i].split(NUMBER_SEPERATOR);

            double[] numbers = new double[numberSplit.length];
            for (int j=0; j < numberSplit.length; j++) {
                numbers[j] = Double.valueOf(numberSplit[j]).doubleValue();
            }

            array2D[i] = numbers;
        }

        return array2D;
    }

    /**
     * Remove first occurrence of "[" and "]" (if both do occur).
     * @param value String to be stripped of [] (might be null).
     * @return input string but with [ and ] removed, or input string if no
     *         brackets were found.
     */
    public static final String unbracket(String value) {
        // null- guard
        if (value == null) return value;

        int start = value.indexOf("[");
        int end   = value.indexOf("]");

        if (start < 0 || end < 0) {
            return value;
        }

        value = value.substring(start + 1, end);

        return value;
    }


    /**
     * From "Q=1" make "W(Q=1)".
     * @return original string wraped in "W()" if it contains a "Q", original
     *         string otherwise.
     */
    public static String wWrap(String wOrQ) {
        return (wOrQ != null && wOrQ.indexOf("Q") >=0)
               ? "W(" + wOrQ + ")"
               : wOrQ;
        }


    public static final String [] splitLines(String s) {
        if (s == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();

        BufferedReader in = null;

        try {
            in =
                new BufferedReader(
                new StringReader(s));

            String line;

            while ((line = in.readLine()) != null) {
                list.add(line);
            }
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch (IOException ioe) {}
        }

        return list.toArray(new String[list.size()]);
    }

    public static final String concat(String [] s) {
        return concat(s, null);
    }

    public static final String concat(String [] s, String glue) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length; ++i) {
            if (i > 0 && glue != null) {
                sb.append(glue);
            }
            sb.append(s[i]);
        }
        return sb.toString();
    }

    public static final String [] splitAfter(String [] src, int N) {
        if (src == null) {
            return null;
        }

        ArrayList<String> list = new ArrayList<String>(src.length);
        for (int i = 0; i < src.length; ++i) {
            String s = src[i];
            int R;
            if (s == null || (R = s.length()) == 0) {
                list.add(s);
            }
            else {
                while (R > N) {
                    list.add(s.substring(0, N));
                    s = s.substring(N);
                    R = s.length();
                }
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static final String [] splitQuoted(String s) {
        return splitQuoted(s, '"');
    }

    public static final String[] fitArray(String [] src, String [] dst) {
        if (src == null) {
            return dst;
        }
        if (dst == null) {
            return src;
        }

        if (src.length == dst.length) {
            return src;
        }

        System.arraycopy(src, 0, dst, 0, Math.min(dst.length, src.length));

        return dst;
    }

    public static final String [] splitQuoted(String s, char quoteChar) {
        if (s == null) {
            return null;
        }
        ArrayList<String> l = new ArrayList<String>();
        int mode = 0, last_mode = 0;
        StringBuilder sb = new StringBuilder();
        for (int N = s.length(), i = 0; i < N; ++i) {
            char c = s.charAt(i);
            switch (mode) {
                case 0: // unquoted mode
                    if (c == quoteChar) {
                        mode = 1; // to quoted mode
                        if (sb.length() > 0) {
                            l.add(sb.toString());
                            sb.setLength(0);
                        }
                    }
                    else if (c == '\\') {
                        last_mode = 0;
                        mode = 2; // escape mode
                    }
                    else if (!Character.isWhitespace(c)) {
                        sb.append(c);
                    }
                    else if (sb.length() > 0) {
                        l.add(sb.toString());
                        sb.setLength(0);
                    }
                    break;
                case 1: // quote mode
                    if (c == '\\') {
                        last_mode = 1;
                        mode = 2; // escape mode
                    }
                    else if (c == quoteChar) { // leave quote mode
                        l.add(sb.toString());
                        sb.setLength(0);
                        mode = 0; // to unquoted mode
                    }
                    else {
                        sb.append(c);
                    }
                    break;
                case 2: // escape mode
                    sb.append(c);
                    mode = last_mode;
                    break;
            }
        }
        if (sb.length() > 0) {
            l.add(sb.toString());
        }
        return l.toArray(new String[l.size()]);
    }

    public static final String [] splitUnique(String s) {
        return splitUnique(s, "[\\s,]+");
    }

    public static final String [] splitUnique(String s, String sep) {
        return s != null ? unique(s.split(sep)) : null;
    }

    public static final String [] unique(String [] str) {
        if (str == null || str.length == 1) {
            return str;
        }

        Arrays.sort(str);

        for (int i = 1; i < str.length; ++i)
            if (str[i].equals(str[i-1])) {
                ArrayList<String> list = new ArrayList<String>(str.length);

                for (int j = 0; j < i; ++j) {
                    list.add(str[j]);
                }

                String last = str[i];

                for (++i; i < str.length; ++i)
                    if (!last.equals(str[i])) {
                        list.add(last = str[i]);
                    }

                return list.toArray(new String[list.size()]);
            }

        return str;
    }

    public static final String [] ensureEmptyExistence(String [] str) {
        if (str == null) {
            return null;
        }

        for (int i = 0; i < str.length; ++i)
            if (str[i].length() == 0) {
                if (i != 0) { // copy to front
                    String t = str[0];
                    str[0] = str[i];
                    str[i] = t;
                }
                return str;
            }

        String [] n = new String[str.length+1];
        n[0] = "";
        System.arraycopy(str, 0, n, 1, str.length);
        return n;
    }

    public static final String ensureWidthPadLeft(
        String s,
        int width,
        char pad
    ) {
        int N = s.length();
        if (N >= width) {
            return s;
        }
        StringBuilder sb = new StringBuilder(width);
        for (; N < width; ++N) {
            sb.append(pad);
        }
        sb.append(s);
        return sb.toString();
    }

    public static final String [] splitWhiteSpaceWithNAsPad(
        String s,
        int    N,
        String pad
    ) {
        if (s == null) {
            return null;
        }

        boolean copyChars = true;
        int     count     = 0; // number of WS

        int S = s.length();

        ArrayList<String> parts = new ArrayList<String>();

        StringBuilder part = new StringBuilder(S);

        for (int i = 0; i < S; ++i) {
            char c = s.charAt(i);
            if (copyChars) { // char mode
                if (Character.isWhitespace(c)) {
                    if (part.length() > 0) {
                        parts.add(part.toString());
                        part.setLength(0);
                    }
                    count     = 1;
                    copyChars = false; // to WS mode
                }
                else {
                    part.append(c);
                }
            }
            else { // counting WS
                if (Character.isWhitespace(c)) {
                    ++count;
                }
                else {
                    while (count >= N) {// enough to insert pad?
                        parts.add(pad);
                        count -= N;
                    }
                    part.append(c);
                    count     = 0;
                    copyChars = true; // back to char mode
                }
            }
        } // for all chars

        if (copyChars) {
            if (part.length() > 0) {
                parts.add(part.toString());
            }
        }
        else {
            while (count >= N) { // enough to insert pad?
                parts.add(pad);
                count -= N;
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    public static final String encodeURL(String url) {
        try {
            return url != null
                   ? URLEncoder.encode(url, "UTF-8")
                   : "";
        }
        catch (UnsupportedEncodingException usee) {
            throw new RuntimeException(usee.getLocalizedMessage());
        }
    }

    public static final String decodeURL(String url) {
        try {
            return url != null
                   ? URLDecoder.decode(url, "UTF-8")
                   : "";
        }
        catch (UnsupportedEncodingException usee) {
            throw new RuntimeException(usee.getLocalizedMessage());
        }
    }

    public static final boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static final String empty(String s) {
        return s == null ? "" : s;
    }


    public static final String trim(String s) {
        return s != null ? s.trim() : null;
    }

    public static final String uniqueWhitespaces(String s) {
        if (s == null) {
            return null;
        }

        boolean wasWS = false;
        StringBuilder sb = new StringBuilder();

        for (int N = s.length(), i = 0; i < N; ++i) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!wasWS) {
                    sb.append(c);
                    wasWS = true;
                }
            }
            else {
                sb.append(c);
                wasWS = false;
            }
        }

        return sb.toString();
    }

    public static final String replaceNewlines(String s) {
        return s == null
               ? null
               : s.replace('\r', ' ').replace('\n', ' ');
    }

    /*
    public static final String quoteReplacement(String s) {

        if (s == null || (s.indexOf('\\') == -1 && s.indexOf('$') == -1))
            return s;

        StringBuilder sb = new StringBuilder();

        for (int N = s.length(), i = 0; i < N; ++i) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') sb.append('\\');
            sb.append(c);
        }

        return sb.toString();
    }
    */

    public static final String quoteReplacement(String s) {

        if (s == null) {
            return null;
        }

        for (int N = s.length(), i = 0; i < N; ++i) { // plain check loop
            char c = s.charAt(i);
            if (c == '$' || c == '\\') { // first special -> StringBuilder
                StringBuilder sb = new StringBuilder(s.substring(0, i))
                .append('\\')
                .append(c);
                for (++i; i < N; ++i) { // build StringBuilder with rest
                    if ((c = s.charAt(i)) == '$' || c == '\\') {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
                return sb.toString();
            }
        }

        return s;
    }

    public static final String repeat(String what, int times) {
        return repeat(what, times, new StringBuilder()).toString();
    }

    public static final StringBuilder repeat(
        String what,
        int times,
        StringBuilder sb
    ) {
        while (times-- > 0) {
            sb.append(what);
        }
        return sb;
    }

    /**
     * Returns the file name without extension.
     */
    public static final String cutExtension(String s) {
        if (s == null) {
            return null;
        }
        int dot = s.lastIndexOf('.');
        return dot >= 0
               ? s.substring(0, dot)
               : s;
    }

    public static final String extension(String s) {
        if (s == null) {
            return null;
        }
        int dot = s.lastIndexOf('.');
        return dot >= 0
               ? s.substring(dot+1)
               : s;
    }

    public static final String [] splitExtension(String x) {
        if (x == null) {
            return null;
        }
        int i = x.lastIndexOf('.');
        return i < 0
               ? new String[] { x, null }
               : new String[] { x.substring(0, Math.max(0, i)),
                                x.substring(i+1).toLowerCase() };
    }

    public static String entityEncode(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        for (int i=0, N =s.length(); i < N; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String entityDecode(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        boolean amp = false;
        StringBuilder sb = new StringBuilder();
        StringBuilder ampbuf = new StringBuilder();
        for (int i=0, N =s.length(); i < N; i++) {
            char c = s.charAt(i);
            if (amp) {
                if (c == ';') {
                    amp = false;
                    String str = ampbuf.toString();
                    ampbuf.setLength(0);
                    if (str.equals("lt")) {
                        sb.append('<');
                    }
                    else if (str.equals("gt")) {
                        sb.append('>');
                    }
                    else if (str.equals("amp")) {
                        sb.append('&');
                    }
                    else {
                        sb.append('&').append(str).append(';');
                    }
                }
                else {
                    ampbuf.append(c);
                }
            }
            else if (c=='&') {
                amp = true;
            }
            else {
                sb.append(c);
            }

        }
        return sb.toString();
    }

    public static final String quote(String s) {
        return quote(s, '"');
    }

    public static final String quote(String s, char quoteChar) {
        if (s == null) {
            return null;
        }

        int N = s.length();

        if (N == 0)
            return new StringBuilder(2)
                   .append(quoteChar)
                   .append(quoteChar)
                   .toString();

        StringBuilder sb = null;

        int i = 0;

        for (; i < N; ++i) {
            char c = s.charAt(i);

            if (Character.isWhitespace(c)) {
                sb = new StringBuilder()
                .append(quoteChar)
                .append(s.substring(0, i+1));
                break;
            }
            else if (c == quoteChar) {
                sb = new StringBuilder()
                .append(quoteChar)
                .append(s.substring(0, i))
                .append('\\')
                .append(quoteChar);
                break;
            }
        }

        if (sb == null) {
            return s;
        }

        for (++i; i < N; ++i) {
            char c = s.charAt(i);
            if (c == quoteChar || c == '\\') {
                sb.append('\\');
            }

            sb.append(c);
        }

        return sb.append(quoteChar).toString();
    }

    /*
    public static String sprintf(String format, Object... args) {
        return sprintf(null, format, args);
    }
    */

    public static String sprintf(
        Locale locale,
        String format,
        Object ... args
    ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf(locale, format, args);
        pw.flush();
        return sw.toString();
    }


    /** Check for occurence of needle in hay, converting both to lowercase
     * to be ignorant of cases. */
    public static boolean containsIgnoreCase(String hay, String needle) {
        return hay.toLowerCase().contains(needle.toLowerCase());
    }
}
// end of file
