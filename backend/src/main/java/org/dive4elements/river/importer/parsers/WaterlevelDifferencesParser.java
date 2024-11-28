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

import org.dive4elements.river.importer.ImportWst;
import org.dive4elements.river.importer.ImportWstQRange;
import org.dive4elements.river.importer.ImportWstColumn;
import org.dive4elements.river.importer.ImportWstColumnValue;
import org.dive4elements.river.backend.utils.DateUtil;


/**
 * Parse WaterlevelDifferences CSV file.
 */
public class WaterlevelDifferencesParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(WaterlevelDifferencesParser.class);

    private static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    public static final Pattern META_UNIT =
        Pattern.compile("^Einheit: \\[(.*)\\].*");

    public static final Pattern YEARS_IN_COLUMN =
        Pattern.compile(".*(\\d{4})-(\\d{4})$");

    public static final double INTERVAL_GAP = 0.00001d;

    /** List of parsed differences as ImportWst s. */
    private List<ImportWst> differences;

    private ImportWstColumn[] columns;

    /** The currently processed dataset. */
    private ImportWst current;


    public WaterlevelDifferencesParser() {
        differences = new ArrayList<ImportWst>();
    }


    /** Get the differences as wst parsed so far. */
    public List<ImportWst> getDifferences() {
        return differences;
    }


    /**
     * Parse a csv waterleveldifferenceparser and create a ImportWst object
     * from it.
     */
    @Override
    public void parse(File file) throws IOException {
        current = new ImportWst(file.getName());
        current.setKind(6);

        super.parse(file);
    }


    /** No rewind implemented. */
    @Override
    protected void reset() {
    }


    @Override
    protected void finish() {
        if (columns != null && current != null) {
            // TODO figure out if its needed, as the columns
            //      are registered at their construction time.
            for (ImportWstColumn col: columns) {
                // TODO place a current.addColumn(col); here?
            }

            differences.add(current);
        }

        // For all differences columns, add a single Q-Range with
        // -1.
        // Expand range to minimal length in case it would be 0
        // TODO: should otherwise be extended to
        // (first station of next range - INTERVAL_GAP),
        // assuming always ascending stations
        for (ImportWstColumn column: columns) {
            List<ImportWstColumnValue> cValues = column.getColumnValues();
            BigDecimal a = cValues.get(0).getPosition();
            BigDecimal b = cValues.get(cValues.size() - 1).getPosition();
            if (a.compareTo(b) == 0) {
                b = new BigDecimal(b.doubleValue() + INTERVAL_GAP);
            }
            column.addColumnQRange(
                new ImportWstQRange(
                    a,
                    b,
                    new BigDecimal(-1d))
                );
        }
        current = null;
        columns = null;
    }


    @Override
    protected void handleLine(int lineNum, String line) {
        if (line.startsWith(START_META_CHAR)) {
            handleMetaLine(stripMetaLine(line));
        }
        else {
            handleDataLine(line);
        }
    }


    private void handleMetaLine(String meta) {
        if (handleMetaUnit(meta)) {
            return;
        }
        else {
            handleMetaColumnNames(meta);
        }
    }


    private boolean handleMetaUnit(String meta) {
        Matcher m = META_UNIT.matcher(meta);

        if (m.matches()) {
            String unit = m.group(1);
            log.debug("Found unit: '" + unit + "'");

            current.setUnit(new ImportUnit(unit));

            return true;
        }

        return false;
    }


    private boolean handleMetaColumnNames(String meta) {
        Pattern META_COLUMN_NAMES = Pattern.compile("Fluss-km;(.*)");
        Matcher m = META_COLUMN_NAMES.matcher(meta);

        if (m.matches()) {
            String colStr = m.group(1);
            String[] cols = colStr.split(SEPERATOR_CHAR);

            log.debug("Found " + cols.length + " columns.");

            initColumns(cols);

            return true;
        }

        return false;
    }


    /** Setup column structures with name, description and time interval. */
    private void initColumns(String[] cols) {
        current.setNumberColumns(cols.length);
        columns = current.getColumns().toArray(
            new ImportWstColumn[cols.length]);

        for (int i = 0; i < cols.length; i++) {
            String name = cols[i].replace("\"", "");

            log.debug("Create new column '" + name + "'");
            ImportWstColumn column = current.getColumn(i);
            column.setName(name);
            column.setDescription(name);

            Matcher m = YEARS_IN_COLUMN.matcher(name);

            if (m.matches()) {
                int startYear = Integer.parseInt(m.group(1));
                int endYear   = Integer.parseInt(m.group(2));
                ImportTimeInterval time = new ImportTimeInterval(
                    DateUtil.getStartDateFromYear(startYear),
                    DateUtil.getEndDateFromYear(endYear)
                );
                column.setTimeInterval(time);
            } else {
                log.debug("No time interval in column header found: " + name);
            }
        }
    }


    /** Handle one line of data, add one value for all columns.
     * @param line the line to parse
     */
    private void handleDataLine(String line) {
        // Split by separator, do not exclude trailing empty string.
        String[] cols = line.split(SEPERATOR_CHAR, -1);

        if (cols == null || cols.length < 2) {
            log.warn("skip invalid waterlevel-diff line: '" + line + "'");
            return;
        }

        try {
            // The first value in a line like 12,9;4,3;4,5 is the station,
            // later real values.
            Double station = nf.parse(cols[0]).doubleValue();

            for (int i = 0; i < columns.length; i++) {
                int idx = i+1;

                if (idx >= cols.length) {
                    log.warn("Insufficient column numbers: " + line);
                    continue;
                }

                String value = cols[idx];

                if (value != null && !value.equals("")) {
                    try {
                        columns[i].addColumnValue(
                            new BigDecimal(station),
                            new BigDecimal(nf.parse(value).doubleValue()));
                    }
                    catch (ParseException pe) {
                        log.warn("Could not parse value: '" + value + "'");
                    }
                }
            }
        }
        catch (ParseException pe) {
            log.warn("Could not parse station: '" + line + "'");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
