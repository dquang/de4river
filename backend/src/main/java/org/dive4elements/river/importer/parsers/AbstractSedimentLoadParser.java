/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;
import java.io.IOException;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImporterSession;
import org.dive4elements.river.importer.ImportGrainFraction;
import org.dive4elements.river.importer.ImportTimeInterval;

import org.dive4elements.river.model.GrainFraction;

import org.dive4elements.river.backend.utils.DateUtil;
import org.dive4elements.river.backend.utils.EpsilonComparator;

/** Parses sediment load files. */
public abstract class AbstractSedimentLoadParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(AbstractSedimentLoadParser.class);


    public static final NumberFormat nf = NumberFormat.getInstance(
        DEFAULT_LOCALE);


    public static final Pattern TIMEINTERVAL_SINGLE =
        Pattern.compile("\\D*([0-9]+?)\\D*");

    public static final Pattern TIMEINTERVAL_EPOCH =
        Pattern.compile("\\D*([0-9]+?)\\s*-\\s*([0-9]+?)\\D*");

    public static final Pattern META_FRACTION =
        Pattern.compile("^Fraktion: (.*)");

    public static final Pattern META_FRACTION_NAME =
        Pattern.compile("^Fraktionsname: (.*)");

    public static final Pattern META_SQ_TIMEINTERVAL =
        Pattern.compile("^S-Q-Beziehung: (.*)");

    public static final Pattern META_COLUMN_NAMES =
        Pattern.compile("^Fluss-km.*");

    public static final Pattern META_GRAIN_SIZE =
        Pattern.compile("([0-9]*,*[0-9]+)-([0-9]*,*[0-9]+) *mm");


    protected abstract void handleDataLine(String line);

    /** Initialize SedimentLoadLSs from columns, set the kind
     * with respect to file location (offical epoch or not?) */
    protected abstract void initializeSedimentLoads();

    protected abstract void handleMetaLine(String line)
        throws LineParserException;


    protected ImportGrainFraction grainFraction;

    protected ImportTimeInterval sqTimeInterval;

    protected String description;

    protected String[] columnNames;

    private String upper;

    private String lower;


    @Override
    public void parse(File file) throws IOException {
        description = file.getName();

        super.parse(file);
    }


    @Override
    protected void handleLine(int lineNum, String line)
        throws LineParserException {
        if (line.startsWith(START_META_CHAR)) {
            handleMetaLine(stripMetaLine(line));
        }
        else {
            handleDataLine(line);
        }
    }


    public boolean handleMetaFraction(String line) {
        Matcher m = META_FRACTION.matcher(line);

        if (m.matches()) {
            String interval = m.group(1);

            Matcher sizes = META_GRAIN_SIZE.matcher(interval);
            if (sizes.matches()) {
                lower = sizes.group(1);
                upper = sizes.group(2);

                return true;
            }

            log.warn("ASLP: Unrecognized grain-size interval. Ignored.");
            return true;

        }

        return false;
    }


    public boolean handleMetaFractionName(String line)
        throws LineParserException {
        Matcher m = META_FRACTION_NAME.matcher(line);

        if (m.matches()) {
            String name = m.group(1);


            GrainFraction gf = ImporterSession.getInstance()
                .getGrainFraction(name);

            if (gf != null) {

                if (lower != null && upper != null) {
                    // Validate grain size interval
                    try {
                        Double lowval = nf.parse(lower).doubleValue();
                        Double upval = nf.parse(upper).doubleValue();

                        if (EpsilonComparator.CMP.compare(lowval,
                                gf.getLower()) != 0 ||
                            EpsilonComparator.CMP.compare(upval,
                                gf.getUpper()) != 0) {
                            log.warn(
                                "ASLP: Invalid grain size for grain fraction '"
                                + name + "'. Ignored.");
                        }
                    }
                    catch (ParseException pe) {
                        log.warn("ASLP: Could not parse grain-size interval. "
                            + "Ignored.");
                    }
                }

                grainFraction = new ImportGrainFraction(gf);
                return true;
            }

            throw new LineParserException("ASLP: Unknown grain fraction: '" +
                name + "'");
        }

        return false;
    }


    public boolean handleMetaSQTimeInterval(String line) {
        Matcher m = META_SQ_TIMEINTERVAL.matcher(line);

        if (m.matches()) {
            String interval = m.group(1);

            try {
                Matcher a = TIMEINTERVAL_EPOCH.matcher(interval);
                if (a.matches()) {
                    int yearA = nf.parse(a.group(1)).intValue();
                    int yearB = nf.parse(a.group(2)).intValue();

                    sqTimeInterval = new ImportTimeInterval(
                        DateUtil.getStartDateFromYear(yearA),
                        DateUtil.getEndDateFromYear(yearB)
                    );
                }
                else {
                    log.warn("ASLP: Unknown SQ-time string: '" + interval +
                        "'. Ignored.");
                }
            }
            catch (ParseException pe) {
                log.error("ASLP: Could not parse SQ-time string: '" +
                    interval + "'. Ignored.", pe);
            }

            return true;

        }

        return false;
    }


    public boolean handleColumnNames(String line) throws LineParserException {
        Matcher m = META_COLUMN_NAMES.matcher(line);

        if (m.matches()) {
            columnNames = line.split(SEPERATOR_CHAR);

            // 'Fluss-km', 'Hinweise' and at least one data column required
            if (columnNames.length < 3) {
                throw new LineParserException("ASLP: missing columns in '" +
                    line + "'");
            }

            initializeSedimentLoads();

            return true;
        }

        return false;
    }


    protected ImportTimeInterval getTimeInterval(String column) {
        try {
            Matcher a = TIMEINTERVAL_EPOCH.matcher(column);
            if (a.matches()) {
                int yearA = nf.parse(a.group(1)).intValue();
                int yearB = nf.parse(a.group(2)).intValue();

                return new ImportTimeInterval(
                    DateUtil.getStartDateFromYear(yearA),
                    DateUtil.getEndDateFromYear(yearB)
                );
            }

            Matcher b = TIMEINTERVAL_SINGLE.matcher(column);
            if (b.matches()) {
                int year = nf.parse(b.group(1)).intValue();

                return new ImportTimeInterval(
                    DateUtil.getStartDateFromYear(year));
            }

            log.warn("ASLP: Unknown time interval string: '" + column + "'");
        }
        catch (ParseException pe) {
            log.warn("ASLP: Could not parse years: " + column, pe);
        }

        return null;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
