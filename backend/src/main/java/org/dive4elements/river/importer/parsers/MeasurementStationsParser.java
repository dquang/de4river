/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.MeasurementStation;

import org.dive4elements.river.importer.ImportMeasurementStation;
import org.dive4elements.river.importer.ImportRange;
import org.dive4elements.river.importer.ImportTimeInterval;


public class MeasurementStationsParser extends LineParser {

    public static class MeasurementStationParserException extends Exception {

        private static final long serialVersionUID = 1L;

        public MeasurementStationParserException(String msg) {
            super(msg);
        }
    }

    public static final int MIN_COLUMNS = 9;

    public static final int MAX_COMMENT_LENGTH = 512;

    private static final Logger log = LogManager
        .getLogger(MeasurementStationsParser.class);

    private List<ImportMeasurementStation> measurementStations;
    private ImportMeasurementStation current;

    @Override
    protected void reset() {
        this.measurementStations = new ArrayList<ImportMeasurementStation>();
    }

    @Override
    protected void finish() {
    }

    @Override
    protected void handleLine(int lineNum, String line) {
        if (line == null || line.startsWith(START_META_CHAR)) {
            log.info("skip meta information at line " + lineNum);
            return;
        }

        try {
            current = new ImportMeasurementStation();
            handleDataLine(lineNum, line);
            measurementStations.add(current);
        }
        catch (MeasurementStationParserException e) {
            log.warn("Problem in line " + lineNum + ": " + e.getMessage());
        }
    }

    public List<ImportMeasurementStation> getMeasurementStations() {
        return measurementStations;
    }

    protected void handleDataLine(int lineNum, String line)
        throws MeasurementStationParserException {
        String[] cols = line.split(SEPERATOR_CHAR);

        if (cols == null || cols.length < MIN_COLUMNS) {
            int num = cols != null ? cols.length : 0;
            throw new MeasurementStationParserException("Not enough columns: "
                + num);
        }

        current.name = getName(cols, lineNum);
        current.range = getRange(cols, lineNum);
        current.measurementType = getMeasurementType(cols, lineNum);
        current.riverside = getRiverside(cols, lineNum);
        current.gauge = getGauge(cols, lineNum);
        current.observationTimerange = getObservationTimerange(cols, lineNum);
        current.operator = getOperator(cols, lineNum);
        current.comment = getComment(cols, lineNum);
    }

    protected String getName(String[] cols, int lineNum)
        throws MeasurementStationParserException {
        if (cols[0] == null || cols[0].length() == 0) {
            throw new MeasurementStationParserException("invalid name in line "
                + lineNum);
        }

        return cols[0];
    }

    protected ImportRange getRange(String[] cols, int lineNum) {
        String from = cols[1];
        String to   = cols[4];
        if (from == null || from.length() == 0) {
            log.error("No station found in line" + lineNum);
            return null;
        }

        try {
            double lower = getDouble(from);

            if (to == null || to.length() == 0) {
                log.warn("No end km found in line " + lineNum);
                return new ImportRange(new BigDecimal(lower));
            }

            try {
                double upper = getDouble(to);

                return new ImportRange(new BigDecimal(lower),
                    new BigDecimal(upper));
            }
            catch (ParseException e) {
                log.warn("Unparseable end km in line " + lineNum +
                    ". Error: " + e.getMessage());
                return new ImportRange(new BigDecimal(lower));
            }

        }
        catch (ParseException e) {
            log.error("Unparseable station in line " + lineNum +
                    ". Error: " + e.getMessage());
            return null;
        }
    }

    protected String getMeasurementType(String[] cols, int lineNum)
        throws MeasurementStationParserException {
        String mtype = cols[2].trim();
        if (!(MeasurementStation.MEASUREMENT_TYPE_BEDLOAD.equals(mtype) ||
                MeasurementStation.MEASUREMENT_TYPE_SUSP.equals(mtype))) {
            throw new MeasurementStationParserException(
                "invalid measurement type in line " + lineNum);
        }

        return mtype;
    }

    protected String getRiverside(String[] cols, int lineNum) {
        String col = cols[3];
        if (col == null || col.length() == 0) {
            log.warn("No river side given in line " + lineNum);
        }
        return col;
    }

    protected String getGauge(String[] cols, int lineNum) {
        String col = cols[5];
        if (col == null || col.length() == 0) {
            log.warn("Invalid gauge found in line " + lineNum);
        }
        return col;
    }

    protected ImportTimeInterval getObservationTimerange(
        String[] cols,
        int lineNum
    ) {
        String col = cols[7];
        if (col == null || col.length() == 0) {
            log.warn("Observation time invalid in line " + lineNum);
            return null;
        }

        try {
            Date date = getDate(col);

            if (date != null) {
                return new ImportTimeInterval(date);
            }
            log.warn("Observation time invalid in line " + lineNum);
        }
        catch (ParseException pe) {
            log.warn("Unparseable observation time '" + col +
                "' in line " + lineNum);
        }
        return null;
    }

    protected String getOperator(String[] cols, int lineNum) {
        String col = cols[8];
        if (col == null || col.length() == 0) {
            log.warn("No operator given in line " + lineNum);
        }
        return col;
    }

    protected String getComment(String[] cols, int lineNum) {
        if (cols.length > MIN_COLUMNS) {
            String col = cols[9];
            if (col.length() > MAX_COMMENT_LENGTH) {
                log.warn("Comment in line " + lineNum +
                    " longer than allowed " + MAX_COMMENT_LENGTH +
                    " characters. Truncated.");
                return col.substring(0, MAX_COMMENT_LENGTH);
            }
            return col;
        }
        return null;
    }
}
