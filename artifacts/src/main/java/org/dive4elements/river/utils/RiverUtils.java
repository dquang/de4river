/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.model.LocationProvider;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.states.WDifferencesState;
import org.dive4elements.river.artifacts.states.WaterlevelSelectState;
import org.dive4elements.river.backend.SessionFactoryProvider;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.River;
import org.dive4elements.river.backend.utils.EpsilonComparator;
import org.dive4elements.river.backend.utils.StringUtil;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TLongArrayList;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.w3c.dom.Document;


/**
 * Static helper methods to e.g. access D4EArtifacts data.
 *
 * @deprecated Don't use RiverUtils to get data from an
 * {@link Artifact} anymore.
 * Instead use and/or create a {@link Access} class hierarchy.
 **/
@Deprecated
public class RiverUtils {

    /** The log that is used in this utility. */
    private static Logger log = LogManager.getLogger(RiverUtils.class);

    /**
     * Comparator to compare Q values with Q main values.
     */
    private static final EpsilonComparator MAIN_VALUE_Q_COMP =
        new EpsilonComparator(1e-3);

    /**
     * Enum that represents the 5 possible WQ modes in FLYS. The 5 values are
     * <i>QFREE</i> <i>QGAUGE</i> <i>WGAUGE</i> <i>WFREE</i> and <i>NONE</i>.
     */
    public static enum WQ_MODE { QFREE, QGAUGE, WFREE, WGAUGE, NONE };

    /**
     * An enum that represents the 4 possible WQ input modes in FLYS. The 4
     * values are
     * <i>ADAPTED</i> <i>SINGLE</i> <i>RANGE</i> and <i>NONE</i>.
     */
    public static enum WQ_INPUT { ADAPTED, SINGLE, RANGE, NONE };

    public static final String XPATH_FLOODMAP_RIVER_PROJECTION =
        "/artifact-database/floodmap/river[@name=$name]/srid/@value";

    public static final String XPATH_MAPSERVER_URL =
        "/artifact-database/mapserver/server/@path";

    public static final String XPATH_MAPFILES_PATH =
        "/artifact-database/mapserver/mapfiles/@path";

    public static final String CURRENT_KM = "currentKm";

    public static final String XPATH_CHART_CURRENTKM =
        "/art:action/art:attributes/art:currentKm/@art:km";


    private RiverUtils() {
    }


    /**
     * Pulls Artifact with given UUID fromm database.
     * @return D4EArtifact with given UUID or null (in case of errors).
     */
    public static D4EArtifact getArtifact(String uuid, CallContext context) {
        try {
            Artifact artifact = context.getDatabase().getRawArtifact(uuid);

            if (artifact == null) {
                log.error("Artifact '" + uuid + "' does not exist.");
                return null;
            }

            if (!(artifact instanceof D4EArtifact)) {
                log.error("Artifact '" +uuid+ "' is no valid D4EArtifact.");
                return null;
            }

            return (D4EArtifact) artifact;
        }
        // TODO: catch more selective
        catch (Exception e) {
            log.error("Cannot get D4EArtifact " + uuid
                + " from database (" + e.getMessage() + ").");
            return null;
        }
    }


    /**
     * Returns the RiverContext from context object.
     *
     * @param context The CallContext or the RiverContext.
     *
     * @return the RiverContext.
     */
    public static RiverContext getFlysContext(Object context) {
        return context instanceof RiverContext
            ? (RiverContext) context
            : (RiverContext) ((CallContext) context).globalContext();
    }


    /**
     * Convinience function to retrieve an XPath as string with replaced config
     * directory.
     *
     * @param xpath The XPath expression.
     *
     * @return a string with replaced config directory.
     */
    public static String getXPathString(String xpath) {
        String tmp = Config.getStringXPath(xpath);
        tmp        = Config.replaceConfigDir(tmp);

        return tmp;
    }


    public static boolean isUsingOracle() {
        SessionFactory sf = SessionFactoryProvider.getSessionFactory();

        String d = SessionFactoryProvider.getDriver((SessionFactoryImpl) sf);

        return d != null ? d.indexOf("Oracle") >= 0 : false;
    }


    /**
     * Returns an WQ_MODE enum which is based on the parameters
     * stored in <i>flys</i> Artifact. If there is no <i>wq_isq</i> parameter
     * existing, WQ_MODE.NONE is returned.
     *
     * @param flys The D4EArtifact that stores wq mode relevant parameters.
     *
     * @return an enum WQ_MODE.
     */
    public static WQ_MODE getWQMode(D4EArtifact flys) {
        if (flys == null) {
            return WQ_MODE.NONE;
        }

        String values = flys.getDataAsString("wq_values");
        Boolean isQ   = flys.getDataAsBoolean("wq_isq");

        if (values != null) {
            return isQ ? WQ_MODE.QGAUGE : WQ_MODE.WGAUGE;
        }

        Boolean isFree = flys.getDataAsBoolean("wq_isfree");

        if (isQ != null && isQ) {
            return isFree ? WQ_MODE.QFREE : WQ_MODE.QGAUGE;
        }
        else if (isQ != null && !isQ) {
            return isFree ? WQ_MODE.WFREE : WQ_MODE.WGAUGE;
        }
        else {
            return WQ_MODE.NONE;
        }
    }


    public static WQ_INPUT getWQInputMode(D4EArtifact flys) {
        if (flys == null) {
            return WQ_INPUT.NONE;
        }

        Boolean selection = flys.getDataAsBoolean("wq_isrange");
        String adapted = flys.getDataAsString("wq_values");

        if(adapted != null && adapted.length() > 0) {
            return WQ_INPUT.ADAPTED;
        }

        if (selection != null && selection) {
            return WQ_INPUT.RANGE;
        }
        else {
            return WQ_INPUT.SINGLE;
        }
    }


    public static double[] getKmFromTo(D4EArtifact flys) {
        String strFrom = flys.getDataAsString("ld_from");
        String strTo   = flys.getDataAsString("ld_to");

        if (strFrom == null) {
            strFrom = flys.getDataAsString("from");
        }

        if (strTo == null) {
            strTo = flys.getDataAsString("to");
        }

        if (strFrom == null || strTo == null) {
            return null;
        }

        try {
            return new double[] {
                Double.parseDouble(strFrom),
                Double.parseDouble(strTo) };
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }


    /**
     * Return sorted array of locations at which stuff was calculated
     * (from ld_locations data), null if not parameterized this way.
     */
    // TODO moved to RangeAccess. Resolve remaining calls.
    private static double[] getLocations(D4EArtifact flys) {
        String locationStr = flys.getDataAsString("ld_locations");

        if (locationStr == null || locationStr.length() == 0) {
            if (flys instanceof WINFOArtifact) {
                WINFOArtifact winfo = (WINFOArtifact) flys;
                if (winfo.getReferenceStartKm() != null
                    && winfo.getReferenceEndKms() != null
                ) {
                    return new double[]
                        {
                            winfo.getReferenceStartKm().doubleValue(),
                            winfo.getReferenceEndKms()[0]
                        };
                }
            }
            return null;
        }

        String[] tmp               = locationStr.split(" ");
        TDoubleArrayList locations = new TDoubleArrayList();

        for (String l: tmp) {
            try {
                locations.add(Double.parseDouble(l));
            }
            catch (NumberFormatException nfe) {
                log.debug(nfe.getLocalizedMessage(), nfe);
            }
        }

        locations.sort();

        return locations.toNativeArray();
    }


    /**
     * Returns the Qs for a given D4EArtifact. This method currently accepts
     * only instances of WINFOArtifact.
     *
     * @param flys A D4EArtifact.
     *
     * @return the Qs.
     */
    public static double[] getQs(D4EArtifact flys) {
        // XXX this is not nice!
        if (flys instanceof WINFOArtifact) {
            return ((WINFOArtifact) flys).getQs();
        }

        log.warn("This method (getQs) currently supports WINFOArtifact only!");

        return null;
    }


    /**
     * Returns the Ws for a given D4EArtifact. This method currently accepts
     * only instances of WINFOArtifact.
     *
     * @param flys A D4EArtifact.
     *
     * @return the Ws.
     */
    public static double[] getWs(D4EArtifact flys) {
        // XXX this is not nice!
        if (flys instanceof WINFOArtifact) {
            return ((WINFOArtifact) flys).getWs();
        }

        log.warn("This method (getWs) currently supports WINFOArtifact only!");

        return null;
    }


    /**
     * Returns the selected River object based on the 'river' data that might
     * have been inserted by the user.
     *
     * @deprecated - use RiverAccess instead
     *
     * @return the selected River or null if no river has been chosen yet.
     */
    public static River getRiver(D4EArtifact flys) {
        String sRiver = getRivername(flys);

        return (sRiver != null)
            ? RiverFactory.getRiver(sRiver)
            : null;
    }


    /**
     * Returns the name of the river specified in the given <i>flys</i>
     * Artifact.
     *
     * @param flys The D4EArtifact that stores a river relevant information.
     *
     * @return the name of the specified river or null.
     */
    public static String getRivername(D4EArtifact flys) {
        return flys != null ? flys.getDataAsString("river") : null;
    }


    /**
     * Extracts the SRID defined in the global configuration for the river
     * specified in <i>artifact</i>.
     *
     * @param artifact The D4EArtifact that stores the name of the river.
     *
     * @return the SRID as string (e.g. "31466").
     */
    public static String getRiverSrid(D4EArtifact artifact) {
        String river = artifact.getDataAsString("river");

        if (river == null || river.length() == 0) {
            return null;
        }

        return getRiverSrid(river);
    }


    public static String getRiverSrid(String rivername) {
        Map<String, String> variables = new HashMap<String, String>(1);
        variables.put("name", rivername);

        Document cfg = Config.getConfig();

        return (String) XMLUtils.xpath(
            cfg,
            XPATH_FLOODMAP_RIVER_PROJECTION,
            XPathConstants.STRING,
            null,
            variables);
    }

    /**
     * Return the (first) Gauge corresponding to the given location(s) of
     * the artifact.
     *
     * This method is left for compatibility. Use river.determineRefGauge()
     * directly in new code.
     *
     * @param flys the artifact in question.
     * @return Reference / first gauge of locations of river of artifact.
     */
    private static Gauge getGauge(D4EArtifact flys) {
        River river = getRiver(flys);
        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] dist = rangeAccess.getKmRange();

        return river.determineRefGauge(dist, rangeAccess.isRange());
    }


    public static String getGaugename(D4EArtifact flys) {
        Gauge gauge = getGauge(flys);

        return gauge != null ? gauge.getName() : null;
    }


    public static Gauge getReferenceGauge(D4EArtifact flys) {
        Long officialNumber = flys.getDataAsLong("reference_gauge");
        String river = getRivername(flys);

        if (officialNumber != null && river != null) {
            return Gauge.getGaugeByOfficialNumber(officialNumber, river);
        } else if (officialNumber != null) {
            return Gauge.getGaugeByOfficialNumber(officialNumber);
        }
        return null;
    }


    public static String getReferenceGaugeName(D4EArtifact flys) {
        Gauge refGauge = getReferenceGauge(flys);

        return refGauge != null
            ? refGauge.getName()
            : "-- not found --";
    }


    /** Creates human-readable name for a wsp (waterlevel/longitudinal section).
     * @param name will be split at '='s.
     */
    public static String createWspWTitle(
        WINFOArtifact winfo,
        CallContext   cc,
        String        name
    ) {
        String[] parts = name.split("=");

        NumberFormat nf = Formatter.getWaterlevelW(cc);

        String namedMainValue = null;

        boolean isQ    = winfo.isQ();
        boolean isFree = winfo.isFreeQ();

        double v;

        try {
            v = Double.valueOf(parts[1]);

            namedMainValue = getNamedMainValue(getGauge(winfo), v);
        }
        catch (NumberFormatException nfe) {
            log.warn("Cannot parse Double of: '" + parts[1] + "'");
            return name;
        }

        String prefix = null;

        if (isQ && !isFree && namedMainValue != null) {
            return "W (" + namedMainValue + ")";
        }

        if (isQ) {
            prefix = "Q=";
        }

        return prefix == null
            ? "W(" + nf.format(v) + ")"
            : "W(" + prefix + nf.format(v) + ")";
    }


    public static String createWspQTitle(
        WINFOArtifact winfo,
        CallContext   cc,
        String        name
    ) {
        String[] parts = name.split("=");

        NumberFormat nf = Formatter.getWaterlevelQ(cc);

        String namedMainValue = null;

        boolean isQ    = winfo.isQ();
        boolean isFree = winfo.isFreeQ();

        double v;

        try {
            v = Double.valueOf(parts[1]);

            namedMainValue = getNamedMainValue(getGauge(winfo), v);
        }
        catch (NumberFormatException nfe) {
            log.warn("Cannot parse Double of: '" + parts[1] + "'");
            return name;
        }

        String prefix = null;

        if (isQ && !isFree && namedMainValue != null) {
            return namedMainValue;
        }

        if (!isQ) {
            prefix = "W=";
        }

        return prefix == null
            ? "Q(" + nf.format(v) + ")"
            : "Q(" + prefix + nf.format(v) + ")";
    }


    /**
     * Returns the named main value if a Q was selected and if this Q fits to a
     * named main value. Otherwise, this function returns null.
     *
     * @param winfo The WINFO Artifact.
     * @param value The Q (or W) value.
     *
     * @return a named main value or null.
     */
    public static String getNamedMainValue(WINFOArtifact winfo, double value) {
        WQ_MODE wqmode = getWQMode(winfo);

        if (wqmode != WQ_MODE.QGAUGE) {
            return null;
        }
        else {
            return getNamedMainValue(getGauge(winfo), value);
        }
    }


    public static String getNamedMainValue(Gauge gauge, double value) {
        List<MainValue> mainValues = gauge.getMainValues();

        for (MainValue mv: mainValues) {
            if (MAIN_VALUE_Q_COMP.compare(
                    mv.getValue().doubleValue(), value) == 0
            ) {
                return mv.getMainValue().getName();
            }
        }

        return null;
    }


    /**
     *
     * @param nmv A string that represents a named main value.
     *
     * @throws NullPointerException if nmv is null.
     */
    public static String stripNamedMainValue(String nmv) {
        int startIndex = nmv.indexOf("(");
        int endIndex   = nmv.indexOf(")");

        if (startIndex > 0 && endIndex > 0 && startIndex < endIndex) {
            return nmv.substring(0, startIndex);
        }

        return nmv;
    }


    /**
     * Returns the URL of user mapfile for the owner of Artifact
     * <i>artifactId</i>.
     *
     * @return the URL of the user wms.
     */
    public static String getUserWMSUrl() {
        String url = getXPathString(XPATH_MAPSERVER_URL);
        url = url.endsWith("/") ? url + "user-wms" : url + "/" + "user-wms";

        return url;
    }


    public static String getRiverWMSUrl() {
        String url = getXPathString(XPATH_MAPSERVER_URL);
        url = url.endsWith("/") ? url + "river-wms" : url + "/" + "river-wms";

        return url;
    }


    /**
     * Returns the description for a given <i>km</i> for a specific
     * river. The river is provided by the D4EArtifact <i>flys</i>.
     *
     * @param flys The D4EArtifact that provides a river.
     * @param km The kilometer.
     *
     * @return the description for <i>km</i> or an empty string if no
     * description was found.
     */
    public static String getLocationDescription(D4EArtifact flys, double km) {
        String river = getRivername(flys);

        if (river == null) {
            return "";
        }

        return LocationProvider.getLocation(river, km);
    }


    /**
     * Returns the differences for a w-differences calculation.
     *
     * @param winfo The WINFOArtifact.
     * @param context The context.
     *
     * @return The differences as string separated by semicolon and linebreak.
     */
    public static String getWDifferences(
        WINFOArtifact winfo,
        CallContext context)
    {
        State state = winfo.getCurrentState(context);
        if(state instanceof WDifferencesState) {
            WDifferencesState wState = (WDifferencesState) state;

            String diffids = winfo.getDataAsString("diffids");
            String datas[] = diffids.split("#");

            // Validate the Data-Strings.
            for (String s: datas) {
                if (!WaterlevelSelectState.isValueValid(s)) {
                    return "";
                }
            }

            if (datas.length < 2) {
                return "";
            }

            String diffs = "";
            for(int i = 0; i < datas.length; i+=2) {
                // e.g.:
                //42537f1e-3522-42ef-8968-635b03d8e9c6;longitudinal_section.w;1
                WKms minuendWKms = wState.getWKms(
                    StringUtil.unbracket(datas[i + 0]),
                    context, 0, 0);
                WKms subtrahendWKms = wState.getWKms(
                    StringUtil.unbracket(datas[i + 1]),
                    context, 0, 0);
                if (minuendWKms != null && subtrahendWKms != null) {
                    diffs += StringUtil.wWrap(minuendWKms.getName())
                        + " - " + StringUtil.wWrap(subtrahendWKms.getName());
                }
                diffs += ";\n";
            }
            return diffs;
        }
        else {
            log.warn("Not a valid state for differences.");
            return "";
        }
    }

    /**
     * Transform a string into an int array. Therefore, the string
     * <i>raw</i> must consist of int values separated by a <i>';'</i>.
     *
     * @param raw The raw integer array as string separated by a ';'.
     *
     * @return an array of int values.
     */
    public static int[] intArrayFromString(String raw) {
        String[] splitted = raw != null ? raw.split(";") : null;

        if (splitted == null || splitted.length == 0) {
            log.warn("No integer values found in '" + raw + "'");
            return new int[0];
        }

        TIntArrayList integers = new TIntArrayList(splitted.length);

        for (String value: splitted) {
            try {
                integers.add(Integer.parseInt(value));
            }
            catch (NumberFormatException nfe) {
                log.warn("Parsing integer failed: " + nfe);
            }
        }

        return integers.toNativeArray();
    }


    /**
     * Transform a string into a long array. Therefore, the string
     * <i>raw</i> must consist of int values separated by a <i>';'</i>.
     *
     * @param raw The raw long array as string separated by a ';'.
     *
     * @return an array of int values.
     */
    public static long[] longArrayFromString(String raw) {
        String[] splitted = raw != null ? raw.split(";") : null;

        if (splitted == null || splitted.length == 0) {
            log.warn("No long values found in '" + raw + "'");
            return new long[0];
        }

        TLongArrayList longs = new TLongArrayList(splitted.length);

        for (String value: splitted) {
            try {
                longs.add(Long.valueOf(value));
            }
            catch (NumberFormatException nfe) {
                log.warn("Parsing long failed: " + nfe);
            }
        }

        return longs.toNativeArray();
    }


    /**
     * Transform a string into an double array. Therefore, the
     * string <i>raw</i> must consist of double values separated by a
     * <i>';'</i>.
     *
     * @param raw The raw double array as string separated by a ';'.
     *
     * @return an array of double values.
     */
    public static double[] doubleArrayFromString(String raw) {
        String[] splitted = raw != null ? raw.split(";") : null;

        if (splitted == null || splitted.length == 0) {
            log.warn("No double values found in '" + raw + "'");
            return new double[0];
        }

        TDoubleArrayList doubles = new TDoubleArrayList(splitted.length);

        for (String value: splitted) {
            try {
                doubles.add(Double.valueOf(value));
            }
            catch (NumberFormatException nfe) {
                log.warn("Parsing double failed: " + nfe);
            }
        }

        return doubles.toNativeArray();
    }


    /**
     * Returns the gauges that match the selected kilometer range.
     *
     * @param flys the flys artifact.
     *
     * @return the gauges based on the selected kilometer range (null if
     *         none/no range set).
     */
    public static List<Gauge> getGauges(D4EArtifact flys) {

        River river = getRiver(flys);
        if (river == null) {
            log.debug("getGauges: no river!");
            return null;
        }

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] dist = rangeAccess.getKmRange();
        if (dist == null) {
            log.debug("getGauges: no dist!");
            return null;
        }
        log.debug("getGauges: " + dist[0] + " - " + dist[1]);

        return river.determineGauges(dist[0], dist[1]);
    }

    /** Round a Q in the AT format style **/
    public static double roundQ(double q) {
        if (q < 10d) q = Math.rint((q*1000d)) / 1000d;
        else if (q < 100d) q = Math.rint((q*100d)) / 100d;
        else if (q < 1000d) q = Math.rint((q*10d)) / 10d;
        else if (q >= 1000d) q = Math.rint(q);
        return q;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
