/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.XY;

import org.dive4elements.artifacts.common.utils.FileTools;


/**
 * Parse files in .prf format and generate a mapping of double
 * (km) to List of Points (XY).
 */
public class PRFParser implements CrossSectionParser
{
    private static Logger log = LogManager.getLogger(PRFParser.class);

    public static final String ENCODING =
        System.getProperty("flys.backend.prf.encoding", "ISO-8859-1");

    public static final Pattern DATA_PATTERN =
        Pattern.compile(
            "\\((\\d+)x\\s*,\\s*(\\d+)\\(" +
            "\\s*f(\\d+)\\.(\\d+)\\s*,\\s*f(\\d+)\\.(\\d+)\\s*\\)?\\)?");

    public static final Pattern KM_PATTERN =
        Pattern.compile("\\((\\d+)x\\s*,\\s*f(\\d+)\\.(\\d+)\\s*\\)?");

    public static final Pattern YEAR_PATTERN =
        Pattern.compile("(\\d{4})");

    public static final int MIN_YEAR = 1800;
    public static final int MAX_YEAR = 2100;

    public static class DataFormat {

        protected int deleteChars;
        protected int maxRepetitions;
        protected int firstIntegerPlaces;
        protected int firstFractionPlaces;
        protected int secondIntegerPlaces;
        protected int secondFractionPlaces;

        protected double firstShift;
        protected double secondShift;

        public DataFormat() {
        }

        public DataFormat(Matcher m) {
            deleteChars          = Integer.parseInt(m.group(1));
            maxRepetitions       = Integer.parseInt(m.group(2));
            firstIntegerPlaces   = Integer.parseInt(m.group(3));
            firstFractionPlaces  = Integer.parseInt(m.group(4));
            secondIntegerPlaces  = Integer.parseInt(m.group(5));
            secondFractionPlaces = Integer.parseInt(m.group(6));

            firstShift  = Math.pow(10, firstFractionPlaces);
            secondShift = Math.pow(10, secondFractionPlaces);
        }

        /**
         * @param kmData where data points will be added to.
         * @param line Line to parse.
         */
        public void extractData(String line, List<XY> kmData) {
            int L = line.length();
            if (L <= deleteChars) {
                return;
            }

            int pos = deleteChars;

            boolean debug = log.isDebugEnabled();

            // Repetitions are values per line ( ... 10.0 12.5 15.3 ... )
            int rep = 0;
            for (;rep < maxRepetitions; ++rep) {
                if (pos >= L || pos + firstIntegerPlaces >= L) {
                    break;
                }
                String first = line.substring(
                    pos, pos + firstIntegerPlaces);

                String second = line.substring(
                    pos + firstIntegerPlaces,
                    Math.min(
                        L, pos + firstIntegerPlaces + secondIntegerPlaces));

                double x, y;
                try {
                    x = Double.parseDouble(first);
                    y = Double.parseDouble(second);
                }
                catch (NumberFormatException nfe) {
                    // "Dummy" line to separate datasets, or missing values.
                    log.debug("PRFParser: Broken line: " + line);
                    return;
                }

                if (first.indexOf('.') < 0) {
                    x /= firstShift;
                }

                if (firstFractionPlaces > 0) {
                    x = (int)(x*firstShift)/firstShift;
                }

                if (second.indexOf('.') < 0) {
                    y /= secondShift;
                }

                if (secondFractionPlaces > 0) {
                    y = (int)(y*secondShift)/secondShift;
                }

                kmData.add(new XY(x, y, kmData.size()));

                pos += firstIntegerPlaces + secondIntegerPlaces;
            }

            return;
        }
    } // class DataFormat

    public static class KMFormat {

        protected int deleteChars;
        protected int integerPlaces;
        protected int fractionPlaces;

        protected double shift;

        public KMFormat() {
        }

        public KMFormat(Matcher m) {
            deleteChars    = Integer.parseInt(m.group(1));
            integerPlaces  = Integer.parseInt(m.group(2));
            fractionPlaces = Integer.parseInt(m.group(3));

            shift = Math.pow(10, fractionPlaces);
        }

        public double extractKm(String line) throws NumberFormatException {

            if (line.length() <= deleteChars) {
                throw new NumberFormatException("line too short");
            }

            String kmS =
                line.substring(deleteChars, deleteChars+integerPlaces);

            double km = Double.parseDouble(kmS.trim());

            if (kmS.indexOf('.') < 0) {
                km /= shift;
            }

            return fractionPlaces > 0
                ? ((int)(km*shift))/shift
                : km;
        }
    } // class KMFormat

    /** Mapping stations (km) to measured points. */
    protected Map<Double, List<XY>> data;

    protected Integer year;

    protected String description;


    public PRFParser() {
        data = new TreeMap<Double, List<XY>>();
    }

    @Override
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<Double, List<XY>> getData() {
        return data;
    }

    public void setData(Map<Double, List<XY>> data) {
        this.data = data;
    }

    protected void sortLists() {
        for (List<XY> xy: data.values()) {
            Collections.sort(xy);
        }
    }

    public static final Integer findYear(String s) {
        Matcher m = YEAR_PATTERN.matcher(s);
        while (m.find()) {
            int year = Integer.parseInt(m.group(1));
            if (year >= MIN_YEAR && year <= MAX_YEAR) {
                return Integer.valueOf(year);
            }
        }
        return null;
    }

    public boolean parse(File file) {

        if (!(file.isFile() && file.canRead())) {
            log.warn("PRF: cannot open file '" + file + "'");
            return false;
        }

        log.info("parsing PRF file: '" + file + "'");

        description = FileTools.removeExtension(file.getName());

        year = findYear(file.getName());

        if (year == null) {
            File parent = file.getParentFile();
            if (parent != null) {
                description = parent.getName() + "/" + description;
                year = findYear(parent.getName());
            }
        }

        if (year != null) {
            log.info("year of sounding: " + year);
        }

        LineNumberReader in = null;

        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = in.readLine();

            if (line == null || (line = line.trim()).length() == 0) {
                log.warn("PRF: file is empty.");
                return false;
            }

            Matcher m = DATA_PATTERN.matcher(line);

            if (!m.matches()) {
                log.warn(
                    "PRF: First line does not look like a PRF data pattern.");
                return false;
            }

            DataFormat dataFormat = new DataFormat(m);

            if ((line = in.readLine()) == null
            || (line = line.trim()).length() == 0) {
                log.warn("PRF: premature EOF. Expected integer in line 2");
                return false;
            }

            try {
                if (Integer.parseInt(line) != dataFormat.maxRepetitions) {
                    log.warn("PRF: Expected " +
                        dataFormat.maxRepetitions + " in line 2");
                    return false;
                }
            }
            catch (NumberFormatException nfe) {
                log.warn("PRF: invalid integer in line 2", nfe);
                return false;
            }

            if ((line = in.readLine()) == null) {
                log.warn(
                    "PRF: premature EOF. Expected pattern for km extraction");
                return false;
            }

            m = KM_PATTERN.matcher(line);

            if (!m.matches()) {
                log.warn(
                    "PRF: line 4 does not look like "
                    + "a PRF km extraction pattern.");
                return false;
            }

            KMFormat kmFormat = new KMFormat(m);

            if ((line = in.readLine()) == null
            || (line = line.trim()).length() == 0) {
                log.warn("PRF: premature EOF. Expected skip row count.");
                return false;
            }

            int lineSkipCount;
            try {
                if ((lineSkipCount = Integer.parseInt(line)) < 0) {
                    throw new IllegalArgumentException(lineSkipCount + " < 0");
                }
            }
            catch (NumberFormatException nfe) {
                log.warn(
                    "PRF: line 5 is not an positive integer.");
                return false;
            }

            int skip = 0;

            while ((line = in.readLine()) != null) {
                // Expecting dummy lines.
                if (skip > 0) {
                    skip--;
                    continue;
                }

                double km;
                try {
                    km = kmFormat.extractKm(line);
                }
                catch (NumberFormatException iae) {
                    log.warn("PRF: cannot extract km in line "
                        + in.getLineNumber());
                    return false;
                }

                Double station = Double.valueOf(km);

                List<XY> kmData = data.get(station);

                // When the station changed (no data yet in line) we expect
                // skip/dummy lines to follow
                if (kmData == null) {
                    kmData = new ArrayList<XY>();
                    data.put(station, kmData);
                    // When a station change occurs,
                    // dummy lines will occur, too.
                    skip = lineSkipCount -1;
                    continue;
                }

                dataFormat.extractData(line, kmData);
            }

            // sort all the lists by x and index
            sortLists();
        }
        catch (IOException ioe) {
            log.error("Error reading PRF file.", ioe);
            return false;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    log.error("Error closing PRF file.", ioe);
                }
            }
        }

        return true;
    }

    public void reset() {
        data.clear();
        year        = null;
        description = null;
    }

    public void parsePRFs(
        File root,
        final CrossSectionParser.Callback callback
    ) {
        FileTools.walkTree(root, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".prf")
                && (callback == null || callback.accept(file))) {
                    reset();
                    boolean success = parse(file);
                    log.info("parsing " + (success ? "succeeded" : "failed"));
                    if (success && callback != null) {
                        callback.parsed(PRFParser.this);
                    }
                }
                return true;
            }
        });
    }

    public static void main(String [] args) {

        PRFParser parser = new PRFParser();

        for (String arg: args) {
            parser.parsePRFs(new File(arg), null);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
