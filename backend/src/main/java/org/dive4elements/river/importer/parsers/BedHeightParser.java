/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;

import java.math.BigDecimal;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.Locale;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportBedHeight;
import org.dive4elements.river.importer.ImportBedHeightValue;
import org.dive4elements.river.importer.ImportBedHeightType;
import org.dive4elements.river.importer.ImportElevationModel;
import org.dive4elements.river.importer.ImportLocationSystem;
import org.dive4elements.river.importer.ImportRange;
import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.importer.ImportUnit;
import org.dive4elements.river.model.BedHeightType;
import org.dive4elements.river.importer.ImporterSession;
import org.dive4elements.river.backend.utils.EpsilonComparator;
import org.dive4elements.river.backend.utils.DateUtil;

public class BedHeightParser {

    private static final Logger log =
        LogManager.getLogger(BedHeightParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    public static final String START_META_CHAR = "#";
    public static final String SEPERATOR_CHAR  = ";";

    public static final Pattern META_YEAR =
        Pattern.compile("^Jahr: [^0-9]*(\\d*).*");

    public static final Pattern META_TIMEINTERVAL =
        Pattern.compile("^Zeitraum: Epoche (\\d*)-(\\d*).*");

    public static final Pattern META_TYPE =
        Pattern.compile("^Aufnahmeart: (.*).*");

    public static final Pattern META_LOCATION_SYSTEM =
        Pattern.compile("^Lagesystem: (.*).*");

    public static final Pattern META_CUR_ELEVATION_SYSTEM =
        Pattern.compile("^H.hensystem:\\s(.*)?? \\[(.*)\\].*");

    public static final Pattern META_OLD_ELEVATION_SYSTEM =
        Pattern.compile("^urspr.ngliches H.hensystem:\\s(.*)?? \\[(.*)\\].*");

    public static final Pattern META_RANGE =
        Pattern.compile("^Strecke:\\D*(\\d++.?\\d*) ?- ?(\\d++.?\\d*).*");

    public static final Pattern META_EVALUATION_BY =
        Pattern.compile("^Auswerter: (.*).*");

    public static final Pattern META_COMMENTS =
        Pattern.compile("^Weitere Bemerkungen: (.*).*");


    protected static NumberFormat nf = NumberFormat.getInstance(
        DEFAULT_LOCALE);


    protected List<ImportBedHeight> bedHeights;


    protected ImportBedHeight newImportBedHeight(String description) {
        return new ImportBedHeight(description);
    }


    protected TreeSet<Double> kmExists;

    public BedHeightParser() {
        bedHeights = new ArrayList<ImportBedHeight>();
        kmExists = new TreeSet<Double>(EpsilonComparator.CMP);
    }


    public List<ImportBedHeight> getBedHeights() {
        return bedHeights;
    }


    public void parse(File file) throws IOException {
        log.info("Parsing bed height single file '" + file + "'");

        ImportBedHeight obj = newImportBedHeight(
            file.getName().replaceAll("\\.csv", ""));

        kmExists.clear();

        LineNumberReader in = null;
        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = null;
            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() == 0) {
                    continue;
                }

                if (line.startsWith(START_META_CHAR)) {
                    handleMetaLine(obj, line);
                }
                else {
                    handleDataLine(obj, line);
                }
            }

            log.info("File contained " + obj.getValueCount() + " values.");
            bedHeights.add(obj);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }


    protected static String stripMetaLine(String line) {
        String tmp = line.substring(1, line.length());

        if (tmp.startsWith(" ")) {
            return tmp.substring(1, tmp.length());
        }
        else {
            return tmp;
        }
    }


    protected void handleMetaLine(ImportBedHeight obj, String line) {
        String meta = stripMetaLine(line);

        if (handleMetaYear(obj, meta)) {
            return;
        }
        else if (handleMetaTimeInterval(obj, meta)) {
            return;
        }
        else if (handleMetaComment(obj, meta)) {
            return;
        }
        else if (handleMetaEvaluationBy(obj, meta)) {
            return;
        }
        else if (handleMetaRange(obj, meta)) {
            return;
        }
        else if (handleMetaType(obj, meta)) {
            return;
        }
        else if (handleMetaLocationSystem(obj, meta)) {
            return;
        }
        else if (handleMetaCurElevationModel(obj, meta)) {
            return;
        }
        else if (handleMetaOldElevationModel(obj, meta)) {
            return;
        }
        else {
            log.warn("BHP: Meta line did not match any known type: " + line);
        }
    }


    protected boolean handleMetaYear(ImportBedHeight obj, String line) {
        Matcher m = META_YEAR.matcher(line);

        if (m.matches()) {
            String tmp = m.group(1);
            if (tmp.length() > 0) {
                obj.setYear(Integer.parseInt(tmp));
            }
            else {
                log.warn("BHP: No year given.");
            }
            return true;
        }

        return false;
    }


    protected boolean handleMetaTimeInterval(
        ImportBedHeight obj,
        String line
    ) {
        Matcher m = META_TIMEINTERVAL.matcher(line);

        if (m.matches()) {
            String lo = m.group(1);
            String up = m.group(2);

            log.debug("Found time interval: " + lo + " - " + up);

            try {
                int lower = Integer.valueOf(lo);
                int upper = Integer.valueOf(up);

                Date fromYear = DateUtil.getStartDateFromYear(lower);
                Date toYear   = DateUtil.getEndDateFromYear(upper);

                obj.setTimeInterval(new ImportTimeInterval(fromYear, toYear));
            }
            catch (NumberFormatException e) {
                log.warn("BHP: could not parse timeinterval", e);
            }

            return true;
        }

        return false;
    }


    protected boolean handleMetaComment(ImportBedHeight obj, String line) {
        Matcher m = META_COMMENTS.matcher(line);

        if (m.matches()) {
            String tmp = m.group(1);

            obj.setDescription(tmp);

            return true;
        }

        return false;
    }


    protected boolean handleMetaEvaluationBy(
        ImportBedHeight obj,
        String                line
    ) {
        Matcher m = META_EVALUATION_BY.matcher(line);

        if (m.matches()) {
            String tmp = m.group(1);
            tmp = tmp.replace(";", "");

            obj.setEvaluationBy(tmp);

            return true;
        }

        return false;
    }


    protected boolean handleMetaRange(ImportBedHeight obj, String line) {
        Matcher m = META_RANGE.matcher(line);

        if (m.matches() && m.groupCount() >= 2) {
            String a = m.group(1).replace(";", "");
            String b = m.group(2).replace(";", "");

            try {
                BigDecimal lower = new BigDecimal(nf.parse(a).doubleValue());
                BigDecimal upper = new BigDecimal(nf.parse(b).doubleValue());

                obj.setRange(new ImportRange(lower, upper));

                return true;
            }
            catch (ParseException e) {
                log.warn("BHP: could not parse range", e);
            }
        }

        return false;
    }


    protected boolean handleMetaType(ImportBedHeight obj, String line) {
        Matcher m = META_TYPE.matcher(line);

        if (m.matches()) {
            String tmp = m.group(1).replace(";", "").trim();

            BedHeightType bht = BedHeightType.fetchBedHeightTypeForType(
                tmp,
                ImporterSession.getInstance().getDatabaseSession());

            if (bht != null) {
                obj.setType(new ImportBedHeightType(bht));
                return true;
            }

            log.error("Unknown bed height type: '" + tmp + "'. File ignored.");
        }

        return false;
    }


    protected boolean handleMetaLocationSystem(
        ImportBedHeight obj,
        String          line
    ) {
        Matcher m = META_LOCATION_SYSTEM.matcher(line);

        if (m.matches()) {
            String tmp = m.group(1).replace(";", "");

            obj.setLocationSystem(new ImportLocationSystem(tmp, tmp));

            return true;
        }

        return false;
    }


    protected boolean handleMetaCurElevationModel(
        ImportBedHeight obj,
        String          line
    ) {
        Matcher m = META_CUR_ELEVATION_SYSTEM.matcher(line);

        if (m.matches()) {
            String name = m.group(1);
            String unit = m.group(2);

            obj.setCurElevationModel(new ImportElevationModel(
                name,
                new ImportUnit(unit)
            ));

            return true;
        }

        return false;
    }


    protected boolean handleMetaOldElevationModel(
        ImportBedHeight obj,
        String          line
    ) {
        Matcher m = META_OLD_ELEVATION_SYSTEM.matcher(line);

        if (m.matches()) {
            String name = m.group(1);
            String unit = m.group(2);

            obj.setOldElevationModel(new ImportElevationModel(
                name,
                new ImportUnit(unit)
            ));

            return true;
        }

        return false;
    }

    private Double parse(String []values, int idx, String msg)  {

        if (idx >= 0 && idx < values.length && !values[idx].isEmpty()) {
            try {
                return nf.parse(values[idx]).doubleValue();
            }
            catch (ParseException e) {
                log.warn("BSP: unparseable " + msg + " '" + values[idx] + "'");
            }
        }

        return null;
    }

    protected void handleDataLine(ImportBedHeight obj, String line) {
        String[] values = line.split(SEPERATOR_CHAR, 0);

        if (values.length < 2) {
            // Do not import line without data or only km
            return;
        }

        Double km;
        try {
            km = new Double(nf.parse(values[0]).doubleValue());

            if (kmExists.contains(km)) {
                log.warn("duplicate station '" + km + "': -> ignored");
                return;
            }

            kmExists.add(km);
        }
        catch (ParseException e) {
            log.error("Error parsing km '" + values[0] + "': " +
                e.getMessage());
            return;
        }

        ImportBedHeightValue value = new ImportBedHeightValue(
            (ImportBedHeight) obj,
            km,
            parse(values, 1, "height"),
            parse(values, 2, "uncertainty"),
            parse(values, 3, "data gap"),
            parse(values, 4, "sounding width"));

        obj.addValue(value);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
