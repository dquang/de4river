/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.model.MapserverStyle;
import org.dive4elements.river.artifacts.model.MapserverStyle.Clazz;
import org.dive4elements.river.artifacts.model.MapserverStyle.Expression;
import org.dive4elements.river.artifacts.model.MapserverStyle.Label;
import org.dive4elements.river.artifacts.model.MapserverStyle.Style;
import org.dive4elements.river.artifacts.resources.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ThemeDocument
{
    private static Logger log = LogManager.getLogger(ThemeDocument.class);

    private static final String MSG_ISOBATH_CLASS = "floodmap.isobath.class";

    private static final String MSG_ISOBATH_LASTCLASS =
        "floodmap.isobath.lastclass";

    public final static String FILL_COLOR = "fillcolor";

    public final static String LINE_COLOR = "linecolor";

    public final static String AREA_LINE_COLOR = "areabordercolor";

    public static final String LINE_SIZE = "linesize";

    public static final String LINE_STYLE = "linetype";

    public static final String POINT_SIZE = "pointsize";

    public static final String POINT_COLOR = "pointcolor";

    public final static String SHOW_BORDER = "showborder";

    public final static String AREA_SHOW_BORDER = "showborder";

    public final static String SHOW_POINTS = "showpoints";

    public final static String SHOW_LINE = "showlines";

    public final static String SHOW_VERTICAL_LINE = "showverticalline";

    public final static String SHOW_HORIZONTAL_LINE = "showhorizontalline";

    public final static String SHOW_LINE_LABEL = "showlinelabel";

    public final static String SHOW_POINT_LABEL = "showpointlabel";

    public final static String SHOW_WIDTH = "showwidth";

    public final static String SHOW_LEVEL = "showlevel";

    public final static String TRANSPARENCY = "transparency";

    public final static String AREA_TRANSPARENCY = "areatransparency";

    public final static String SHOW_AREA = "showarea";

    public final static String SHOW_AREA_LABEL = "showarealabel";

    public final static String SHOW_MIDDLE_HEIGHT = "showmiddleheight";

    public final static String LABEL_FONT_COLOR = "labelfontcolor";

    public final static String LABEL_FONT_SIZE = "labelfontsize";

    public final static String LABEL_FONT_FACE = "labelfontface";

    public final static String LABEL_FONT_STYLE = "labelfontstyle";

    public final static String FONT = "font";

    public final static String TEXT_SIZE = "textsize";

    public final static String TEXT_STYLE = "textstyle";

    public final static String TEXT_ORIENTATION = "textorientation";

    public final static String LABEL_BGCOLOR = "labelbgcolor";

    public final static String LABEL_SHOW_BACKGROUND = "labelshowbg";

    public final static String BACKGROUND_COLOR = "backgroundcolor";

    public final static String AREA_BACKGROUND_COLOR = "areabgcolor";

    public final static String SYMBOL = "symbol";

    public final static String SHOW_MINIMUM = "showminimum";

    public final static String SHOW_MAXIMUM = "showmaximum";

    public final static String WSPLGEN_STARTCOLOR = "startcolor";

    public final static String WSPLGEN_ENDCOLOR = "endcolor";

    public final static String WSPLGEN_NUMCLASSES = "numclasses";

    public final static String BANDWIDTH = "bandwidth";

    public final static String SHOWEXTRAMARK = "showextramark";

    public final static String USE_FILL_PAINT = "usefillpaint";

    private Map<String, String> values;

    public ThemeDocument() {
    }

    public ThemeDocument(Document document) {
        values = extractValues(document);
    }

    public ThemeDocument(ThemeDocument other) {
        values = new HashMap<String, String>(other.values);
    }


    public String getValue(String key) {
        return values.get(key);
    }

    public void setValue(String key, String value) {
        values.put(key, value);
    }

    private static Map<String, String> extractValues(Document document) {
        Map<String, String> values = new HashMap<String, String>();
        if (document == null) {
            log.error("Invalid null document given.");
            return values;
        }

        NodeList fields = document.getElementsByTagName("field");
        for (int i = 0, N = fields.getLength(); i < N; ++i) {
            Element field = (Element)fields.item(i);
            String name   = field.getAttribute("name");
            String value  = field.getAttribute("default");
            if (!name.isEmpty() && !value.isEmpty()) {
                values.put(name, value);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Theme values: " + values);
        }
        return values;
    }

    /** Parse string to be boolean with default if empty or unrecognized. */
    private static boolean parseBoolean(String value, boolean defaultsTo) {
        if (value == null) {
            return defaultsTo;
        }
        if (value.equals("false")) {
            return false;
        }
        if (value.equals("true")) {
            return true;
        }
        return defaultsTo;
    }


    /**
     * Attempt converting \param value to an integer, in failing cases,
     * return \param defaultsTo.
     * @param value String to be converted to integer.
     * @param defaultsTo Default to return if conversion failed.
     * @return \param value as integer or defaultsto if conversion failed.
     */
    private static int parseInteger(String value, int defaultsTo) {
        if (value == null) {
            return defaultsTo;
        }

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        return defaultsTo;
    }


    /**
     * Attempt converting \param value to a double, in failing cases,
     * return \param defaultsTo.
     * @param value String to be converted to double.
     * @param defaultsTo Default to return if conversion failed.
     * @return \param value as integer or defaultsto if conversion failed.
     */
    private static double parseDouble(String value, double defaultsTo) {
        if (value == null) {
            return defaultsTo;
        }

        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        return defaultsTo;
    }

    public boolean parseShowLineLabel() {
        String show = getValue(SHOW_LINE_LABEL);
        return parseBoolean(show, false);
    }

    public boolean parseShowWidth() {
        String show = getValue(SHOW_WIDTH);
        return parseBoolean(show, false);
    }

    public boolean parseShowLevel() {
        String show = getValue(SHOW_LEVEL);
        return parseBoolean(show, false);
    }

    public String parseTextOrientation() {
        String o = getValue(TEXT_ORIENTATION);

        return o != null && "true".equals(o)
            ? "horizontal"
            : "vertical";
    }

    public boolean parseShowMiddleHeight() {
        String show = getValue(SHOW_MIDDLE_HEIGHT);
        return parseBoolean(show, false);
    }

    public boolean parseLabelShowBackground() {
        String show = getValue(LABEL_SHOW_BACKGROUND);
        return parseBoolean(show, false);
    }

    public Font parseFont() {
        String font = getValue(FONT);
        log.debug(" font is " + font);
        if (font == null) {
            return null;
        }

        int size = parseFontSize();
        int style = parseFontStyle();
        Font f = new Font(font, style, size);
        return f;
    }

    public Font parseTextFont() {
        String font = getValue(LABEL_FONT_FACE);
        if (font == null) {
            return null;
        }

        int size = parseTextSize();
        int style = parseTextStyle();
        Font f = new Font(font, style, size);
        return f;
    }

    public Color parseTextColor() {
        return parseRGB(getTextColorString());
    }

    private String getTextColorString() {
        return getValue(LABEL_FONT_COLOR);
    }

    public Color parseTextBackground() {
        String color = getLabelBackgroundColorString();
        return color != null
            ? parseRGB(color)
            : Color.WHITE;
    }

    private String getLabelBackgroundColorString() {
        return getValue(LABEL_BGCOLOR);
    }

    public int parseLineWidth() {
        String size = getValue(LINE_SIZE);
        if (size == null) {
            return 0;
        }

        try {
            return Integer.parseInt(size);
        }
        catch (NumberFormatException nfe) {
            log.warn("Unable to set line size from string: '" + size + "'");
        }
        return 0;
    }

    public float [] parseLineStyle() {
        String dash = getValue(LINE_STYLE);

        float[] def = {10};
        if (dash == null) {
            return def;
        }

        String[] pattern = dash.split(",");
        if(pattern.length == 1) {
            return def;
        }

        try {
            float[] dashes = new float[pattern.length];
            for (int i = 0; i < pattern.length; i++) {
                dashes[i] = Float.parseFloat(pattern[i]);
            }
            return dashes;
        }
        catch (NumberFormatException nfe) {
            log.warn("Unable to set dash from string: '" + dash + "'");
            return def;
        }
    }

    public int parsePointWidth() {
        String width = getValue(POINT_SIZE);
        return parseInteger(width, 3);
    }

    public Color parsePointColor() {
        String color = getValue(POINT_COLOR);
        return parseColor(color);
    }

    public boolean parseShowPoints() {
        String show = getValue(SHOW_POINTS);
        return parseBoolean(show, false);
    }

    public boolean parseShowLine() {
        String show = getValue(SHOW_LINE);
        return parseBoolean(show, false);
    }

    public int parseFontStyle() {
        String style = getValue(TEXT_STYLE);
        if (style == null) {
            return Font.PLAIN;
        }

        if (style.equals("italic")) {
            return Font.ITALIC;
        }
        if (style.equals("bold")) {
            return Font.BOLD;
        }
        return Font.PLAIN;
    }

    public int parseTextStyle() {
        String style = getValue(LABEL_FONT_STYLE);
        if (style == null) {
            return Font.PLAIN;
        }

        if (style.equals("italic")) {
            return Font.ITALIC;
        }
        if (style.equals("bold")) {
            return Font.BOLD;
        }
        return Font.PLAIN;
    }

    /** Handle info for label or annotation text. */
    public TextStyle parseComplexTextStyle() {
        Font font = parseTextFont();
        // Try the annotation text styles.
        if (font == null) {
            font = parseFont();
        }
        return new TextStyle(
            parseTextColor(),
            font,
            parseTextBackground(),
            parseLabelShowBackground(),
            !parseTextOrientation().equals("horizontal"));
    }

    public LineStyle parseComplexLineStyle() {
        return new LineStyle(
            parseLineColorField(),
            Integer.valueOf(parseLineWidth()));
    }

    public boolean parseShowVerticalLine() {
        String show = getValue(SHOW_VERTICAL_LINE);
        return parseBoolean(show, true);
    }

    public boolean parseShowHorizontalLine() {
        String show = getValue(SHOW_HORIZONTAL_LINE);
        return parseBoolean(show, true);
    }

    public double parseBandWidth() {
        String bandWidth = getValue(BANDWIDTH);
        return parseDouble(bandWidth, 0);
    }

    private static Color parseColor(String colorString) {
        if (colorString == null) {
            return null;
        }
        if (colorString.indexOf("#") == 0) {
            return parseHexColor(colorString);
        }
        if (colorString.indexOf(",") >= 0) {
            return parseRGB(colorString);
        }

        return null;
    }


    /**
     * Parse a string like "#00CC22" and return the corresponding color.
     *
     * @param hex The hex color value.
     *
     * @return a Color or null, if <i>hex</i> is empty.
     */
    private static Color parseHexColor(String hex) {
        return hex != null
            ? Color.decode(hex)
            : null;
    }


    public boolean parseShowArea() {
        String show = getValue(SHOW_AREA);
        return parseBoolean(show, false);
    }

    public boolean parseShowAreaLabel() {
        String show = getValue(SHOW_AREA_LABEL);
        return parseBoolean(show, false);
    }

    public boolean parseShowPointLabel() {
        String show = getValue(SHOW_POINT_LABEL);
        return parseBoolean(show, false);
    }

    public boolean parseShowExtraMark() {
        String show = getValue(SHOWEXTRAMARK);
        return parseBoolean(show, false);
    }

    public Boolean parseUseFillPaint() {
        String use = getValue(USE_FILL_PAINT);
        return use != null
            ? parseBoolean(use, false)
            : null;
    }

    public int parseFontSize() {
        String size = getValue(TEXT_SIZE);
        if (size == null) {
            return 10;
        }

        try {
            return Integer.parseInt(size);
        }
        catch (NumberFormatException nfe) {
            // Do nothing
        }
        return 10;
    }

    public int parseTextSize() {
        String size = getValue(LABEL_FONT_SIZE);
        if (size == null) {
            return 10;
        }

        try {
            return Integer.parseInt(size);
        }
        catch (NumberFormatException nfe) {
            // Do nothing
        }
        return 10;
    }

    /**
     * Parse a string like "103, 100, 0" and return a corresping color.
     * @param rgbtext Color as string representation, e.g. "255,0,20".
     * @return Color, null in case of issues.
     */
    public static Color parseRGB(String rgbtext) {
        if (rgbtext == null) {
            return null;
        }
        String rgb[] = rgbtext.split(",");
        try {
            return new Color(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim()));
        }
        catch (NumberFormatException nfe) {
            // Do nothing
        }
        return null;
    }

    private String getLineColorString() {
        return getValue(LINE_COLOR);
    }


    /** Get show border as string. */
    private String getShowBorderString() {
        return getValue(SHOW_BORDER);
    }


    /** Get fill color as string. */
    private String getFillColorString() {
        return getValue(FILL_COLOR);
    }

    private String getSymbol() {
        return getValue(SYMBOL);
    }

    private String getTransparencyString() {
        return getValue(TRANSPARENCY);
    }


    private String getAreaTransparencyString() {
        return getValue(AREA_TRANSPARENCY);
    }


    private String getShowMinimum() {
        return getValue(SHOW_MINIMUM);
    }


    private String getShowMaximum() {
        return getValue(SHOW_MAXIMUM);
    }

    /**
     * Gets color from color field.
     * @param theme    the theme document.
     * @return color.
     */
    public Color parseFillColorField() {
        return parseRGB(getFillColorString());
    }

    public boolean parseShowBorder() {
        return parseBoolean(getShowBorderString(), false);
    }

    public int parseTransparency() {
        return parseInteger(getTransparencyString(), 50);
    }


    /**
     * Gets color from color field.
     * @return color.
     */
    public Color parseLineColorField() {
        String lineColorStr = getLineColorString();
        if (log.isDebugEnabled()) {
            log.debug("parseLineColorField: lineColorStr = " +
                (lineColorStr == null
                     ? "null"
                     : lineColorStr));
        }
        return parseColor(lineColorStr);
    }


    public Color parseAreaLineColorField() {
        String lineColorStr = getAreaLineColorString();
        if (log.isDebugEnabled()) {
            log.debug("parseLineColorField: lineColorStr = " +
                (lineColorStr == null
                    ? "null"
                    : lineColorStr));
        }
        return parseColor(lineColorStr);
    }


    private String getAreaLineColorString() {
        return getValue(AREA_LINE_COLOR);
    }


    public boolean parseShowMinimum() {
        return parseBoolean(getShowMinimum(), false);
    }


    public boolean parseShowMaximum() {
        return parseBoolean(getShowMaximum(), false);
    }


    /**
     * Creates a MapserverStyle from the given XML theme.
     * This method uses a start- and endcolor to interpolate a
     * given number of color classes for the MapserverStyle.
     * @param theme
     * @return String representation of the MapserverStyle
     */
    public String createDynamicMapserverStyle(
        float    from,
        float    to,
        float    step,
        CallMeta meta
    ) {
        MapserverStyle ms = new MapserverStyle();

        String strStartColor = getValue(WSPLGEN_STARTCOLOR);
        Color startColor = strStartColor != null
            ? parseColor(strStartColor)
            : new Color(178, 201, 215);
        String strEndColor = getValue(WSPLGEN_ENDCOLOR);
        Color endColor = strEndColor != null
            ? parseColor(strEndColor)
            : new Color(2, 27, 42);

        to = to >= 0 ? to : 9999;
        step = to != from ? step : 1;

        int numClasses = (int)((to - from) / step + 1);

        float rd = (endColor.getRed()   - startColor.getRed())
            / (float)numClasses;
        float gd = (endColor.getGreen() - startColor.getGreen())
            / (float)numClasses;
        float bd = (endColor.getBlue()  - startColor.getBlue())
            / (float)numClasses;

        for (int n = 0; n < numClasses; n++) {
            StringBuilder newColor = new StringBuilder();
            newColor.append(startColor.getRed()   + Math.round(n * rd));
            newColor.append(' ');
            newColor.append(startColor.getGreen() + Math.round(n * gd));
            newColor.append(' ');
            newColor.append(startColor.getBlue()  + Math.round(n * bd));

            String expr = createWSPLGENClassExpression(
                from + n * step, step, n + 1, numClasses);
            String name = createWSPLGENClassName(
                from + n * step, step, n + 1, numClasses, meta);

            Clazz c = new Clazz(name);
            Style s = new Style();
            s.setColor(newColor.toString());
            s.setSize(5);

            c.addItem(new Expression("(" + expr + ")"));
            c.addItem(s);

            ms.addClazz(c);
        }

        return ms.toString();
    }


    protected static String createWSPLGENClassExpression(
        float val,
        float step,
        int idx,
        int maxIdx
    ) {
        if (idx < maxIdx) {
            return "[DIFF] >= " + val + " AND  [DIFF] < " + (val + step);
        }
        else {
            return "[DIFF] >= " + val;
        }
    }

    /**
     * Creates a class name for the mapfile style that visualizes a floodmap.
     * The class names are used in the map's legend.
     *
     * @param val       Current isobath value.
     * @param step      Difference between to class values.
     * @param idx       Current class index that is being processed.
     * @param maxIdx    Highest class index.
     * @param meta      Caller meta object used to determine locale.
     * @return
     */
    protected static String createWSPLGENClassName(
        float    val,
        float    step,
        int      idx,
        int      maxIdx,
        CallMeta meta
    ) {
        assert meta != null : "CallMeta instance is null";

        if (idx < maxIdx) {
            return Resources.getMsg(meta, MSG_ISOBATH_CLASS,
                    new Object[] {val, val + step});
        }
        return Resources.getMsg(meta, MSG_ISOBATH_LASTCLASS,
                new Object[] {val});
    }


    public String createMapserverStyle() {
        String symbol    = getSymbol();
        String backcolor = getLabelBackgroundColorString();
        String linecolor = getLineColorString();
        if (linecolor == null) {
            log.warn("createMapserverStyle: linecolor String is empty");
            linecolor = "0,128,255";
        }

        int linewidth = parseLineWidth();

        MapserverStyle ms = new MapserverStyle();

        Clazz c = new Clazz(" ");

        Style s = new Style();
        s.setOutlineColor(linecolor.replace(",", " "));

        if (backcolor != null) {
            s.setColor(backcolor.replace(",", " "));
        }

        s.setSize(linewidth);
        s.setSymbol(symbol);
        c.addItem(s);

        String textcolor = getTextColorString();
        int    textsize  = parseTextSize();

        if (textcolor != null && textsize > 0) {
            Label l = new Label();
            l.setColor(textcolor.replace(",", " "));
            l.setSize(textsize);
            c.addItem(l);
        }

        ms.addClazz(c);

        return ms.toString();
    }


    private String getAreaBackgroundColorString() {
        return getValue(AREA_BACKGROUND_COLOR);
    }


    public Color parseAreaBackgroundColor() {
        return parseColor(getAreaBackgroundColorString());
    }


    public int parseAreaTransparency() {
        return parseAreaTransparency(50);
    }

    public int parseAreaTransparency(int alpha) {
        return parseInteger(getAreaTransparencyString(), alpha);
    }


    public boolean parseAreaShowBorder() {
        return parseBoolean(getAreaShowBorderString(), false);
    }


    private String getAreaShowBorderString() {
        return getValue(AREA_SHOW_BORDER);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
