/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.river.importer.ImportFlowVelocityMeasurement;
import org.dive4elements.river.importer.ImportFlowVelocityMeasurementValue;

import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class FlowVelocityMeasurementParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(FlowVelocityMeasurementParser.class);

    private static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    private static final DateFormat df =
        new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    private List<ImportFlowVelocityMeasurement> measurements;

    private ImportFlowVelocityMeasurement current;


    public FlowVelocityMeasurementParser() {
        measurements = new ArrayList<ImportFlowVelocityMeasurement>();
    }


    public List<ImportFlowVelocityMeasurement> getMeasurements() {
        return measurements;
    }

    @Override
    protected void reset() {
        current = new ImportFlowVelocityMeasurement();
    }


    @Override
    protected void finish() {
        current.setDescription(fileName);
        measurements.add(current);
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


    public void handleMetaLine(String line) {
    }


    public void handleDataLine(String line) {
        String[] cols = line.split(SEPERATOR_CHAR);

        if (cols.length < 8) {
            log.warn("skip invalid data line: '" + line + "'");
            return;
        }

        try {
            double km     = nf.parse(cols[1]).doubleValue();
            double w      = nf.parse(cols[5]).doubleValue();
            double q      = nf.parse(cols[6]).doubleValue();
            double v      = nf.parse(cols[7]).doubleValue();

            String timestr     = cols[3] + " " + cols[4];
            String description = cols.length > 8 ? cols[8] : null;

            current.addValue(new ImportFlowVelocityMeasurementValue(
                df.parse(timestr),
                new BigDecimal(km),
                new BigDecimal(w),
                new BigDecimal(q),
                new BigDecimal(v),
                description
            ));
        }
        catch (ParseException pe) {
            log.warn("Unparseable flow velocity values:", pe);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
