/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImporterSession;
import org.dive4elements.river.importer.ImportSedimentLoad;
import org.dive4elements.river.importer.ImportSedimentLoadValue;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.MeasurementStation;

/** Parses sediment load longitudinal section files. */
public class SedimentLoadParser extends AbstractSedimentLoadParser {
    private static final Logger log =
        LogManager.getLogger(SedimentLoadParser.class);


    public static final String GRAINFRACTION_NAME_SUSP = "suspended_sediment";

    public static final String GRAINFRACTION_NAME_TOTAL = "total";

    protected List<ImportSedimentLoad> sedimentLoads;

    protected ImportSedimentLoad[] current;

    protected River river;

    public SedimentLoadParser() {
        sedimentLoads = new ArrayList<ImportSedimentLoad>();
    }

    public SedimentLoadParser(River river) {
        sedimentLoads = new ArrayList<ImportSedimentLoad>();
        this.river = river;
    }



    @Override
    protected void reset() {
        current       = null;
        grainFraction = null;
    }


    @Override
    protected void finish() {
        if (current != null) {
            for (ImportSedimentLoad isy: current) {
                sedimentLoads.add(isy);
            }
        }

        description = null;
    }


    @Override
    protected void handleMetaLine(String line) throws LineParserException {
        if (handleMetaFraction(line)) {
            return;
        }
        if (handleMetaFractionName(line)) {
            return;
        }
        if (handleMetaSQTimeInterval(line)) {
            return;
        }
        if (handleColumnNames(line)) {
            return;
        }
        log.warn("ASLP: Unknown meta line: '" + line + "'");
    }


    private void initializeSedimentLoadValues(String[] vals,
        MeasurementStation m) throws ParseException {
        for (int i = 1, n = columnNames.length-1; i < n; i++) {
            String curVal = vals[i];

            if (curVal != null && curVal.length() > 0) {
                current[i-1].addValue(new ImportSedimentLoadValue(
                    m, nf.parse(curVal).doubleValue()
                ));
            }
        }
    }

    @Override
    protected void handleDataLine(String line) {
        String[] vals = line.split(SEPERATOR_CHAR);

        if (vals == null || vals.length < columnNames.length-1) {
            log.warn("SLP: skip invalid data line: '" + line + "'");
            return;
        }

        try {
            Double km = nf.parse(vals[0]).doubleValue();

            List<MeasurementStation> ms =
                ImporterSession.getInstance().getMeasurementStations(
                    river, km);

            String gfn = grainFraction.getPeer().getName();

            if (ms != null && !ms.isEmpty()) {

                // Check for measurement station at km fitting grain fraction
                for (MeasurementStation m : ms) {
                    if (gfn.equals(GRAINFRACTION_NAME_TOTAL)) {
                        // total load can be at any station type
                        initializeSedimentLoadValues(vals, m);
                        return;
                    }
                    if (gfn.equals(GRAINFRACTION_NAME_SUSP) &&
                        m.getMeasurementType().equals(
                            MeasurementStation.MEASUREMENT_TYPE_SUSP)) {
                        // susp. sediment can only be at respective stations
                        initializeSedimentLoadValues(vals, m);
                        return;
                    }
                    if (!gfn.equals(GRAINFRACTION_NAME_SUSP) &&
                        m.getMeasurementType().equals(
                            MeasurementStation.MEASUREMENT_TYPE_BEDLOAD)) {
                        /** anything but total load and susp. sediment
                            can only be at bed load measurement stations */
                        initializeSedimentLoadValues(vals, m);
                        return;
                    }
                }
                log.error("SLP: No measurement station at km " + km +
                    " fitting grain fraction " + gfn +
                    " on river " + river.getName());
                return;
            }
            else {
                log.error("SLP: No measurement station at km " + km +
                    " on river " + river.getName());
                return;
            }
        }
        catch (ParseException pe) {
            log.warn("SLP: unparseable number in data row '" + line + "':", pe);
        }
    }


    @Override
    protected void initializeSedimentLoads() {
        // skip first column (Fluss-km) and last column (Hinweise)
        current = new ImportSedimentLoad[columnNames.length-2];

        Integer kind;

        if (inputFile.getAbsolutePath().contains("amtliche Epochen")) {
            kind = new Integer(1);
        }
        else {
            kind = new Integer(0);
        }

        for (int i = 0, n = columnNames.length; i < n-2; i++) {
            current[i] = new ImportSedimentLoad(
                grainFraction,
                getTimeInterval(columnNames[i+1]),
                sqTimeInterval,
                description,
                kind);
        }
    }


    public List<ImportSedimentLoad> getSedimentLoads() {
        return sedimentLoads;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
