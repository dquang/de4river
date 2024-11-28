/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.XY;

import org.dive4elements.river.backend.utils.EpsilonComparator;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * To create cross-sections, generate: Map<double,list<xy>> from files
 * in da66 format.
 */
public class DA66Parser extends LineParser implements CrossSectionParser
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(DA66Parser.class);

    private static final String HEAD_HEAD = "00";
    private static final String HEAD_GEOM = "66"; // "Values"
    private static final String HEAD_ENDG = "88"; // Probably never used.

    /** Regex to match lines of files in da66 format. */
    private static final Pattern LINE_PATTERN =
        Pattern.compile("^([0-9 -]{2})" + // Type (00|66|88)
                        "([0-9 -]{5})" + // unset
                        "([0-9 -]{2})" + // id
                        "([0-9 -]{9})" + // station
                        "([0-9 -]{2})" + // running number
                        "([0-9 -]{1})?" + // point id
                        /*
                        Would be great if we could express the pattern as this:
                        ([0-9 -]{1})([0-9 -JKMLMNOPQR]{7})([0-9 -]{7})+
                        */
                        "([0-9 -JKMLMNOPQR]{7})?" + // y
                        "([0-9 -]{7})?" + // z
                        "([0-9 -]{1})?" + // point id
                        "([0-9 -JKMLMNOPQR]{7})?" + // y
                        "([0-9 -]{7})?" + // z
                        "([0-9 -]{1})?" + // point id
                        "([0-9 -JKMLMNOPQR]{7})?" + // y
                        "([0-9 -]{7})?" + // z
                        "([0-9 -]{1})?" + // point id
                        "([0-9 -JKMLMNOPQR]{7})?" + // y
                        "([0-9 -]{7})?" // z
                        );


    /** Indices to match group of main regex. */
    private static enum FIELD {
        HEAD      ( 1),
        UNSET     ( 2),
        ID        ( 3),
        STATION   ( 4),
        RUNNR     ( 5),
        POINT_1_ID( 6),
        POINT_1_Y ( 7),
        POINT_1_Z ( 8),
        POINT_2_ID( 9),
        POINT_2_Y (10),
        POINT_2_Z (11),
        POINT_3_ID(12),
        POINT_3_Y (13),
        POINT_3_Z (14),
        POINT_4_ID(15),
        POINT_4_Y (16),
        POINT_4_Z (17);

        private int idx;
        FIELD(int idx) {
            this.idx = idx;
        }
        int getIdx() {
            return idx;
        }
    }


    /** Header lines of da66 can define a type. */
    private static enum Type {
        DATE                     ( 0),
        HEKTOSTONE_LEFT          ( 1), //grm. "Standlinie"
        HEKTOSTONE_RIGHT         ( 2),
        CHANNEL_LEFT             ( 3), //grm. "Fahrrinne"
        CHANNEL_RIGHT            ( 4),
        CHANNEL_2_LEFT           ( 5),
        CHANNEL_2_RIGHT          ( 6),
        GIW_1972                 ( 7),
        GROIN_DIST_LEFT          ( 8), //grm. "Buhnenkopfabstand links"
        GROIN_HEIGHT_LEFT        ( 9),
        GROIN_SLOPE_LEFT         (10),
        GROIN_DIST_RIGHT         (11),
        GROIN_HEIGHT_RIGHT       (12),
        GROIN_SLOPE_RIGHT        (13),
        STRIKE_LEFT              (14), //grm. "Streichlinie links"
        AXIS                     (15),
        STRIKE_RIGHT             (16),
        GROIN_BACK_SLOPE_LEFT    (17), //grm. "Buhnenrueckenneigung"
        GROIN_BACK_SLOPE_RIGHT   (18),
        GIW_1932                 (19),
        GIW_1982                 (20),
        STAND_ISLAND_1           (21),
        STAND_ISLAND_2           (22),
        STAND_ISLAND_3           (23),
        STAND_ISLAND_4           (24),
        UNSPECIFIED_1            (25),
        UNSPECIFIED_2            (26),
        HHW                      (27),
        OLD_PROFILE_NULL         (28),
        AW_1978                  (29),
        SIGN_LEFT                (30),
        SIGN_RIGHT               (31),
        DIST_SIGNAL_CHANNEL_LEFT (32),
        DIST_SIGNAL_CHANNEL_RIGHT(33),
        UNSPECIFIED_3            (34),
        UNSPECIFIED_4            (35),
        UNSPECIFIED_5            (36),
        UNSPECIFIED_6            (37),
        SHORE_LEFT               (38),
        SHORE_RIGHT              (39),
        UNSPECIFIED_7            (40);

        private final int id;
        Type(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
    }


    /** Available types. */
    private static HashMap<Integer, Type> typeMap;


    /** Types we can deal with. */
    private static List<Type> implementedTypes;


    static {
        typeMap = new HashMap<Integer, Type>();
        for (Type t: Type.values()) {
            typeMap.put(new Integer(t.getId()), t);
        }
        // TODO populate and respect header type.
        implementedTypes = new ArrayList<Type>();
        //implementedTypes.add(..);
    }


    /** The current line to which add points. */
    private List<XY> currentLine;


    /** Data collected so far, last element will be currentLine. */
    protected Map<Double, List<XY>> data;


    /** Trivial constructor. */
    public DA66Parser() {
        data = new TreeMap<Double, List<XY>>(EpsilonComparator.CMP);
    }


    /** Get the description of the cross section parsed. */
    @Override
    public String getDescription() {
        return FileTools.removeExtension(getFileName());
    }


    /** Get the year of this cross sections measurement. */
    @Override
    public Integer getYear() {
        return null;
    }


    /**
     * Return the data parsed.
     * @return map of stations (km) to list of points.
     */
    @Override
    public Map<Double, List<XY>> getData() {
        return data;
    }


    /**
     * Walk a directory tree, parse its *.da66 files and store the
     * data found.
     */
    public void parseDA66s(File root, final Callback callback) {

        FileTools.walkTree(root, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".d66")
                && (callback == null || callback.accept(file))) {
                    reset();
                    try {
                        parse(file);
                        log.info("parsing done");
                        if (callback != null) {
                            callback.parsed(DA66Parser.this);
                        }
                    }
                    catch (IOException ioe) {
                        log.error("IOException while parsing file");
                        return false;
                    }
                }
                return true;
            }
        });
    }


    /**
     * Get the Index of the last cross-section lines point.
     * @return last points index, -1 if not available.
     */
    private int lastPointIdx() {
        if (currentLine == null || currentLine.isEmpty()) {
            return -1;
        }
        XY lastPoint = this.currentLine.get(currentLine.size()-1);
        return lastPoint.getIndex();
    }


    /** Returns station, deciding if it could in cm, in which case convert. */
    private double stationInKm(double station) {
        if (station > 10000) {
            return station/100000d;
        }
        else {
            return station;
        }
    }


    /** Apply the convention how to deal with numbers < -99.999 .*/
    private String applyLetterConvention(String orig) {
        if (orig.endsWith("-")) {
            return "-" + orig.replace("-","");
        }
        else if (orig.endsWith("J")) {
            return "-" + orig.replace("J","1");
        }
        else if (orig.endsWith("K")) {
            return "-" + orig.replace("K","2");
        }
        else if (orig.endsWith("L")) {
            return "-" + orig.replace("L","3");
        }
        else if (orig.endsWith("M")) {
            return "-" + orig.replace("M","4");
        }
        else if (orig.endsWith("N")) {
            return "-" + orig.replace("N","5");
        }
        else if (orig.endsWith("O")) {
            return "-" + orig.replace("O","6");
        }
        else if (orig.endsWith("P")) {
            return "-" + orig.replace("P","7");
        }
        else if (orig.endsWith("Q")) {
            return "-" + orig.replace("Q","8");
        }
        else if (orig.endsWith("R")) {
            return "-" + orig.replace("R","9");
        }
        else {
            return orig;
        }
    }

    /**
     * Add a Point (YZ,Index) to the current cross section line.
     * @param y The y coordinate of new point.
     * @param z The z coordinate of new point.
     * @param idx Ignored, the parameter of new point.
     * @return true if point could been added, false otherwise (e.g. not
     *         parsable y or z values.
     */
    private boolean addPoint(String y, String z, String idx) {
        if (z == null || y == null || idx == null) {
            log.error("Incomplete point definition");
            return false;
        }

        double iy;
        double iz;
        // Handle letter convention.
        y = applyLetterConvention(y);
        try {
            iy = Double.parseDouble(y) / 1000d;
            iz = Double.parseDouble(z) / 1000d;
        }
        catch(java.lang.NumberFormatException nfe) {
            log.error("Could not parse Number: " + nfe.getMessage());
            return false;
        }

        // We ignore idx, and increment instead.
        int index;
        int lastPointIdx = lastPointIdx();
        if (lastPointIdx <= 0) {
            index = 1;
        } else {
            index = lastPointIdx + 1;
        }

        currentLine.add(new XY(iy, iz, index));
        return true;
    }


    /** Called before consuming first line of file. */
    public void reset() {
        data.clear();
        currentLine = new ArrayList<XY>();
    }


    /**
     * Called for each line. Try to extract info from a da66 line.
     */
    @Override
    protected void handleLine(int lineNum, String line) {
        String head = line.substring(0,2);
        if (HEAD_HEAD.equals(head)) {
                //log.debug("New station");
                Matcher m = LINE_PATTERN.matcher(line);
                if (m.find()) {
                    // Actually matches!
                    // TODO 'move' last line to match river axis
                    // TODO find river axis intersection
                    currentLine = new ArrayList<XY>();
                    double station = stationInKm(
                        Double.parseDouble(m.group(FIELD.STATION.getIdx())));
                    data.put(station, currentLine);
                }
                else {
                    log.error("HEAD line bad.");
                }
        }
        else if (HEAD_GEOM.equals(head)) {
            Matcher m = LINE_PATTERN.matcher(line);
            if (m.find()) {
                //log.info("Station: " + m.group(FIELD.STATION.getIdx()));
                // TODO if last station differs, error and abort
                if (m.group(FIELD.POINT_1_ID.getIdx()) != null) {
                    // Point 1
                    if(addPoint(
                        m.group(FIELD.POINT_1_Y.getIdx()),
                        m.group(FIELD.POINT_1_Z.getIdx()),
                        m.group(FIELD.POINT_1_ID.getIdx()))) {
                        // Point added.
                    }
                    else {
                        // Problematic point.
                        log.error("A point could not be added");
                    }
                }
                if (m.group(FIELD.POINT_2_ID.getIdx()) != null) {
                    // Point 2
                    if(addPoint(
                        m.group(FIELD.POINT_2_Y.getIdx()),
                        m.group(FIELD.POINT_2_Z.getIdx()),
                        m.group(FIELD.POINT_2_ID.getIdx()))) {
                        // Point added.
                    }
                    else {
                        // Problematic point.
                        log.error("A point could not be added");
                    }
                }
                if (m.group(FIELD.POINT_3_ID.getIdx()) != null) {
                    // Point 3
                    if(addPoint(
                        m.group(FIELD.POINT_3_Y.getIdx()),
                        m.group(FIELD.POINT_3_Z.getIdx()),
                        m.group(FIELD.POINT_3_ID.getIdx()))) {
                        // Point added.
                    }
                    else {
                        // Problematic point.
                        log.error("A point could not be added");
                    }
                }
                if (m.group(FIELD.POINT_4_ID.getIdx()) != null) {
                    // Point 4
                    if(addPoint(
                        m.group(FIELD.POINT_4_Y.getIdx()),
                        m.group(FIELD.POINT_4_Z.getIdx()),
                        m.group(FIELD.POINT_4_ID.getIdx()))) {
                        // Point added.
                    }
                    else {
                        // Problematic point.
                        log.error("A point could not be added");
                    }
                }
            }
            else {
                log.warn("Line could not be parsed: ");
                log.warn(line);
            }
        }
        else if (HEAD_GEOM.equals(head)) {
            log.debug("Hit a 88");
        }
        else {
            log.error("Do not know how to treat da66 line:");
            log.error(line);
        }
    }


    /** Called when file is fully consumed. */
    @Override
    protected void finish() {
        // TODO 'move' last line to match river axis
        log.info("Parsed " + data.size() + " lines");
    }


    /** Parses files given as arguments. */
    public static void main(String [] args) {

        DA66Parser parser = new DA66Parser();

        log.warn("Start parsing files.");
        for (String arg: args) {
            parser.parseDA66s(new File(arg), null);
            log.warn("Parsing a file.");
        }
        log.error("Finished parsing files.");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
