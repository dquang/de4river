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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.importer.ImportUnit;

import org.dive4elements.river.importer.ImportRange;
import org.dive4elements.river.importer.ImportWst;
import org.dive4elements.river.importer.ImportWstColumn;
import org.dive4elements.river.importer.ImportWstColumnValue;
import org.dive4elements.river.importer.ImportWstQRange;
import org.dive4elements.river.backend.utils.DateUtil;


/**
 * Parse CSV Waterlevel files.
 * As these waterlevels are probably used in fixation analysis
 * only, functionality to export them to "fixation"-wsts
 * has been added (the ImportWaterlevel*- stuff is actually
 * not needed to do so.)
 */
public class WaterlevelParser extends LineParser {

    private static final Logger log = LogManager.getLogger(WaterlevelParser.class);

    private static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    private static final Pattern META_Q_RANGE =
        Pattern.compile("Abfluss\\s\\[(.*)\\];(.*)");

    public static final Pattern META_UNIT =
        Pattern.compile("^Einheit: \\[(.*)\\].*");

    public static final double INTERVAL_GAP = 0.00001d;

    private List<ImportWst> waterlevels;

    private ImportWst current;

    /** The Waterlevel-Wst s will always have but one column. */
    private ImportWstColumn column;

    /** The current (incomplete) Q Range. */
    private ImportWstQRange currentQRange;

    /** The current (incomplete) km range for Q Range. */
    private ImportRange currentRange;

    private String currentDescription;


    public WaterlevelParser() {
        waterlevels = new ArrayList<ImportWst>();
    }


    public List<ImportWst> getWaterlevels() {
        return waterlevels;
    }


    @Override
    public void parse(File file) throws IOException {
        currentDescription = file.getName();

        super.parse(file);
    }


    @Override
    protected void reset() {
        currentQRange = null;
        current       = new ImportWst(currentDescription);
        current.setNumberColumns(1);
        column        = current.getColumn(0);
        column.setName(currentDescription);
        column.setDescription(currentDescription);

        // Try to extract and set the TimeInterval.
        Matcher m = WaterlevelDifferencesParser.YEARS_IN_COLUMN.matcher(
            currentDescription);

        if (m.matches()) {
            int startYear = Integer.parseInt(m.group(1));
            int endYear   = Integer.parseInt(m.group(2));
            ImportTimeInterval time = new ImportTimeInterval(
                DateUtil.getStartDateFromYear(startYear),
                DateUtil.getEndDateFromYear(endYear)
            );
            column.setTimeInterval(time);
        } else {
            log.debug("No time interval in column header found: "
                + currentDescription);
        }

        current.setKind(7);
    }


    @Override
    protected void finish() {
        if (current != null) {
            if (currentQRange != null) {
                List<ImportWstColumnValue> cValues = column.getColumnValues();
                // Set end of range to last station
                // or expand range to minimal length in case it would be 0
                // TODO: should otherwise be extended to
                // (first station of next range - INTERVAL_GAP),
                // assuming always ascending stations
                BigDecimal lastStation = cValues.get(cValues.size() -1)
                    .getPosition();
                if (lastStation.compareTo(currentRange.getA()) == 0) {
                    currentRange.setB(new BigDecimal(lastStation.doubleValue()
                        + INTERVAL_GAP));
                }
                else {
                    currentRange.setB(lastStation);
                }

                currentQRange.setRange(currentRange);
                column.addColumnQRange(currentQRange);
            }

            waterlevels.add(current);
        }
    }

    @Override
    protected void handleLine(int lineNum, String line) {
        if (line.startsWith(START_META_CHAR)) {
            handleMetaLine(stripMetaLine(line));
            return;
        }
        else if (handleQRange(line)) {
            return;
        }
        else {
            handleDataLine(line);
            return;
        }
    }


    private void handleMetaLine(String meta) {
        Matcher m = META_UNIT.matcher(meta);

        if (m.matches()) {
            String unit = m.group(1);
            log.debug("Found unit: '" + unit + "'");

            current.setUnit(new ImportUnit(unit));
        }
    }


    private boolean handleQRange(String line) {
        Matcher m = META_Q_RANGE.matcher(line);

        if (m.matches()) {
            String unitStr  = m.group(1);
            String valueStr = m.group(2);
            try {
                if (currentQRange != null) {
                    // Finish off the last one.
                    List<ImportWstColumnValue> cValues = column
                        .getColumnValues();
                    // Set end of range to last station.
                    currentRange.setB(cValues.get(cValues.size() -1)
                        .getPosition());
                    currentQRange.setRange(currentRange);
                    column.addColumnQRange(currentQRange);
                }
                currentQRange = new ImportWstQRange(null,
                    new BigDecimal(nf.parse(valueStr).doubleValue()));
                currentRange = new ImportRange();

                log.debug("Found new Q range: Q=" + valueStr);

                return true;
            }
            catch (ParseException pe) {
                log.warn("Unparseable Q range: '" + line + "'");
            }
        }

        return false;
    }


    private void handleDataLine(String line) {
        String[] cols = line.split(SEPERATOR_CHAR);

        if (cols == null || cols.length < 2) {
            log.warn("skip invalid waterlevel line: '" + line + "'");
            return;
        }

        try {
            // Store the value and remember the position for QRange, if needed.
            Double station = nf.parse(cols[0]).doubleValue();
            Double value   = nf.parse(cols[1]).doubleValue();

            BigDecimal stationBD = new BigDecimal(station);

            column.addColumnValue(stationBD, new BigDecimal(value));

            if (currentRange.getA() == null) {
                currentRange.setA(stationBD);
            }
        }
        catch (ParseException pe) {
            log.warn("Unparseable number in data row: " + line);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
