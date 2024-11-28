/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.river.importer.ImportDepth;
import org.dive4elements.river.importer.ImportPorosity;
import org.dive4elements.river.importer.ImportPorosityValue;
import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.backend.utils.DateUtil;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PorosityParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(PorosityParser.class);

    public static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);

    public static final Pattern META_DEPTH =
        Pattern.compile("^Tiefe: (\\d++)-(\\d++).*");

    public static final Pattern META_TIMEINTERVAL =
        Pattern.compile("^Zeitraum: (\\d{4})-(\\d{4}).*");

    protected List<ImportPorosity> porosities;

    protected ImportPorosity current;

    protected String currentDescription;

    public PorosityParser() {
        porosities = new ArrayList<ImportPorosity>();
    }


    @Override
    public void parse(File file) throws IOException {
        currentDescription = file.getName().replaceAll("\\.csv", "");

        super.parse(file);
    }


    @Override
    protected void reset() {
        current = new ImportPorosity(currentDescription);
    }


    @Override
    protected void finish() {
        if (current != null) {
            porosities.add(current);
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
        if (handleMetaTimeInterval(line)) {
            return;
        }
        log.warn("Unknown meta line: '" + line + "'");
    }

    protected boolean handleMetaTimeInterval(String line) {
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

                current.setTimeInterval(
                    new ImportTimeInterval(fromYear, toYear));
            }
            catch (NumberFormatException e) {
                log.warn("PP: could not parse timeinterval", e);
            }

            return true;
        }

        return false;
    }

    protected boolean handleMetaDepth(String line) {
        Matcher m = META_DEPTH.matcher(line);

        if (m.matches()) {
            String lo   = m.group(1);
            String up   = m.group(2);

            log.info("Found porosity depth: " + lo + " - " + up + " cm");

            ImportDepth depth = null;
            try {
                depth = new ImportDepth(
                    new BigDecimal(lo),
                    new BigDecimal(up)
                );
            }
            catch (NumberFormatException nfe) {
                log.warn("Unparsable number for depth: " + line, nfe);
            }

            if (depth != null) {
                current.setDepth(depth);
                return true;
            }
            return false;
        }
        else {
            log.debug("Meta line doesn't contain depth information: " + line);
        }

        return false;
    }

    protected void handleDataLine(String line) {
        String[] vals = line.split(SEPERATOR_CHAR);
        log.debug("handle line: " + line);

        if (vals == null || vals.length < 3) {
            log.warn("skip invalid data line: '" + line + "'");
            return;
        }

        BigDecimal km = null;
        BigDecimal shoreOffset = null;
        BigDecimal porosity = null;
        vals[0] = vals[0].replace(",", ".");
        vals[2] = vals[2].replace(",", ".");
        try {
            km          = new BigDecimal(vals[0]);
            porosity     = new BigDecimal(vals[2]);
            if (!vals[1].isEmpty()) {
                vals[1] = vals[1].replace(",", ".");
                shoreOffset = new BigDecimal(vals[1]);
            }
        }
        catch(NumberFormatException nfe) {
            log.warn("Unparsable number in line: " + line, nfe);
        }

        if (km == null || porosity == null) {
            log.warn("PP: No km nor porosity given. Skip line");
            return;
        }
        log.debug("add new value.");
        current.addValue(new ImportPorosityValue(
            km,
            shoreOffset,
            porosity,
            currentDescription));
    }


    public List<ImportPorosity> getPorosities() {
        return porosities;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
