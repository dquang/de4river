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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.MeasurementStation;

import org.dive4elements.river.importer.ImporterSession;
import org.dive4elements.river.importer.ImportSQRelation;
import org.dive4elements.river.importer.ImportSQRelationValue;
import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.backend.utils.DateUtil;


public class SQRelationParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(SQRelationParser.class);

    private static final Pattern TIMERANGE_REGEX =
        Pattern.compile(".*Zeitraum.*\\s(\\w*)-(\\w*).*");

    private static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    private List<ImportSQRelation> relations;

    private ImportSQRelation current;

    private String currentDescription;

    protected River river;

    public SQRelationParser(River river) {
        relations = new ArrayList<ImportSQRelation>();
        this.river = river;
    }


    public List<ImportSQRelation> getSQRelations() {
        return relations;
    }

    @Override
    public void parse(File file) throws IOException {
        this.currentDescription = file.getName();
        super.parse(file);
    }


    @Override
    protected void reset() {
        current = new ImportSQRelation();
    }


    @Override
    protected void finish() {
        if (current != null) {
            current.setDescription(currentDescription);
            relations.add(current);
        }
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


    protected void handleMetaLine(String line) {
        Matcher m = TIMERANGE_REGEX.matcher(line);

        if (m.matches()) {
            String lo = m.group(1);
            String hi = m.group(2);

            log.debug("Found timerange " + lo + " - " + hi);

            try {
                int low  = nf.parse(lo).intValue();
                int high = nf.parse(hi).intValue();

                current.setTimeInterval(new ImportTimeInterval(
                    DateUtil.getStartDateFromYear(low),
                    DateUtil.getEndDateFromYear(high)
                ));
            }
            catch (ParseException nfe) {
                log.warn("Cannot parse time range.", nfe);
            }
        }
    }


    protected void handleDataLine(String line) {
        String[] cols = line.split(SEPERATOR_CHAR);

        String parameter = cols[1].trim();
        Double km = parseDouble(cols, 3);
        Double a = parseDouble(cols, 6);
        Double b = parseDouble(cols, 7);
        Double qMax = parseDouble(cols, 8);
        Double rSq = parseDouble(cols, 9);
        Integer nTot = parseInteger(cols, 10);
        Integer nOutlier = parseInteger(cols, 11);
        Double cFer = parseDouble(cols, 12);
        Double cDuan = parseDouble(cols, 13);

        if (km == null || a == null || b == null
        || qMax == null || parameter.length() == 0
        ) {
            if (km == null) {
                log.error(
                    "No km for measurement station: "
                    + "Can not reference measurement station: "
                    + line);
            }
            if (a == null || b == null
            || qMax == null || parameter.length() == 0
            ) {
                log.error(
                    "Incomplete SQ-relation row "
                    + "(missing a, b, Qmax or parameter): "
                    + line);
            }
            return;
        }

        MeasurementStation mStation = ImporterSession.getInstance()
            .getMeasurementStation(
                river,
                km,
                parameter.equals("A") || parameter.equals("B")
                ? MeasurementStation.MEASUREMENT_TYPE_SUSP
                : MeasurementStation.MEASUREMENT_TYPE_BEDLOAD);

        if (mStation == null) {
            log.warn("No measurement station fitting parameter " +
                parameter + " at km " + km + ". Line ignored.");
            return;
        }

        current.addValue(new ImportSQRelationValue(
                parameter,
                mStation,
                a,
                b,
                qMax,
                rSq,
                nTot,
                nOutlier,
                cFer,
                cDuan
            ));
    }

    private Double parseDouble(String[] values, int idx) {
        if (idx >= 0 && idx < values.length && !values[idx].isEmpty()) {
            try {
                return nf.parse(values[idx]).doubleValue();
            }
            catch (ParseException e) {
                log.warn("Unparseable value '" + values[idx] + "'");
            }
        }
        return null;
    }

    private Integer parseInteger(String[] values, int idx) {
        if (idx >= 0 && idx < values.length && !values[idx].isEmpty()) {
            try {
                return nf.parse(values[idx]).intValue();
            }
            catch (ParseException e) {
                log.warn("Unparseable value '" + values[idx] + "'");
            }
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
