/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.river.importer.ImportDepth;
import org.dive4elements.river.importer.ImportSedimentDensity;
import org.dive4elements.river.importer.ImportSedimentDensityValue;

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

public class SedimentDensityParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(SedimentDensityParser.class);

    public static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    public static final Pattern META_DEPTH =
        Pattern.compile("^Tiefe: (\\d++)-(\\d++).*");

    public static final Pattern META_YEAR =
        Pattern.compile("^Jahr: (\\d{4}).*");

    protected List<ImportSedimentDensity> sedimentDensities;

    protected ImportSedimentDensity current;

    protected String currentDescription;

    protected String yearString;

    public SedimentDensityParser() {
        sedimentDensities = new ArrayList<ImportSedimentDensity>();
    }


    @Override
    public void parse(File file) throws IOException {
        currentDescription = file.getName();

        super.parse(file);
    }


    @Override
    protected void reset() {
        current = new ImportSedimentDensity(currentDescription);
    }


    @Override
    protected void finish() {
        if (current != null) {
            sedimentDensities.add(current);
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
        if (handleMetaDepth(line)) {
            return;
        }
        if (handleMetaYear(line)) {
            return;
        }
        log.warn("Unknown meta line: '" + line + "'");
    }


    protected boolean handleMetaDepth(String line) {
        Matcher m = META_DEPTH.matcher(line);

        if (m.matches()) {
            String lo   = m.group(1);
            String up   = m.group(2);

            log.info("Found sediment density depth: "
                + lo + " - " + up + " cm");

            try {
                ImportDepth depth = new ImportDepth(
                    new BigDecimal(nf.parse(lo).doubleValue()),
                    new BigDecimal(nf.parse(up).doubleValue())
                );

                current.setDepth(depth);

                return true;
            }
            catch (ParseException pe) {
                log.warn("Unparseable numbers in: '" + line + "'");
            }
        }
        else {
            log.debug("Meta line doesn't contain depth information: " + line);
        }

        return false;
    }

    protected boolean handleMetaYear(String line) {
        Matcher m = META_YEAR.matcher(line);

        if (m.matches()) {
            yearString = m.group(1);

            log.info("Found sediment density year: " + yearString);

            return true;
        }

        log.debug("Meta line doesn't contain year: " + line);

        return false;
    }


    protected void handleDataLine(String line) {
        String[] vals = line.split(SEPERATOR_CHAR);

        if (vals == null || vals.length < 3) {
            log.warn("skip invalid data line: '" + line + "'");
            return;
        }

        BigDecimal km = null;
        BigDecimal shoreOffset = null;
        BigDecimal density = null;
        try {
            km          = new BigDecimal(nf.parse(vals[0]).doubleValue());
            density     = new BigDecimal(nf.parse(vals[2]).doubleValue());
            if (!vals[1].isEmpty()) {
                shoreOffset = new BigDecimal(nf.parse(vals[1]).doubleValue());
            }
        }
        catch (ParseException pe) {
            log.warn("Unparseable numbers in '" + line + "'");
        }

        if (km == null || density == null) {
            log.warn("SDP: No km nor density given. Skip line");
            return;
        }

        BigDecimal year = null;
        if (yearString != null) {
            try {
                year = new BigDecimal(nf.parse(yearString).doubleValue());
            }
            catch (ParseException pe) {
                log.warn("Unparseable year string");
            }
        }

        current.addValue(new ImportSedimentDensityValue(
            km,
            shoreOffset,
            density,
            year,
            currentDescription));
    }


    public List<ImportSedimentDensity> getSedimentDensities() {
        return sedimentDensities;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
