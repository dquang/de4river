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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportSedimentLoadLS;
import org.dive4elements.river.importer.ImportSedimentLoadLSValue;
import org.dive4elements.river.importer.ImportUnit;


/** Parses sediment load longitudinal section files. */
public class SedimentLoadLSParser extends AbstractSedimentLoadParser {

    private static final Logger log =
        LogManager.getLogger(SedimentLoadLSParser.class);


    public static final Pattern META_UNIT =
        Pattern.compile("^Einheit: \\[(.*)\\].*");


    protected List<ImportSedimentLoadLS> sedimentLoadLSs;

    protected ImportSedimentLoadLS[] current;

    protected ImportUnit unit;


    public SedimentLoadLSParser() {
        sedimentLoadLSs = new ArrayList<ImportSedimentLoadLS>();
    }


    @Override
    protected void reset() {
        current       = null;
        grainFraction = null;
        unit          = null;
    }


    @Override
    protected void finish() {
        if (current != null) {
            for (ImportSedimentLoadLS isy: current) {
                sedimentLoadLSs.add(isy);
            }
        }

        description = null;
    }


    @Override
    protected void handleMetaLine(String line) throws LineParserException {
        if (handleMetaUnit(line)) {
            return;
        }
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


    protected boolean handleMetaUnit(String line) {
        Matcher m = META_UNIT.matcher(line);

        if (m.matches()) {
            unit = new ImportUnit(m.group(1));
            return true;
        }

        return false;
    }


    @Override
    protected void handleDataLine(String line) {
        String[] vals = line.split(SEPERATOR_CHAR);

        if (vals == null || vals.length < columnNames.length-1) {
            log.warn("SLLSP: skip invalid data line: '" + line + "'");
            return;
        }

        try {
            Double km = nf.parse(vals[0]).doubleValue();

            for (int i = 1, n = columnNames.length-1; i < n; i++) {
                String curVal = vals[i];

                if (curVal != null && curVal.length() > 0) {
                    current[i-1].addValue(new ImportSedimentLoadLSValue(
                        km, nf.parse(vals[i]).doubleValue()
                    ));
                }
            }
        }
        catch (ParseException pe) {
            log.warn(
                "SLLSP: unparseable number in data row '" + line + "':", pe);
        }
    }


    @Override
    protected void initializeSedimentLoads() {
        // skip first column (Fluss-km) and last column (Hinweise)
        current = new ImportSedimentLoadLS[columnNames.length-2];

        Integer kind;

        if (inputFile.getAbsolutePath().contains("amtliche Epochen")) {
            kind = new Integer(1);
        }
        else {
            kind = new Integer(0);
        }

        for (int i = 0, n = columnNames.length; i < n-2; i++) {
            current[i] = new ImportSedimentLoadLS(this.description);
            current[i].setTimeInterval(getTimeInterval(columnNames[i+1]));
            current[i].setSQTimeInterval(sqTimeInterval);
            current[i].setUnit(unit);
            current[i].setGrainFraction(grainFraction);
            current[i].setKind(kind);
        }
    }


    public List<ImportSedimentLoadLS> getSedimentLoadLSs() {
        return sedimentLoadLSs;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
