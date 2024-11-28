/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.math.BigDecimal;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportMorphWidth;
import org.dive4elements.river.importer.ImportMorphWidthValue;
import org.dive4elements.river.importer.ImportUnit;


public class MorphologicalWidthParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(MorphologicalWidthParser.class);

    public static final NumberFormat nf = NumberFormat.getInstance(
        DEFAULT_LOCALE);

    public static final Pattern META_UNIT =
        Pattern.compile("^Einheit: \\[(.*)\\].*");

    protected List<ImportMorphWidth> morphWidths;

    protected ImportMorphWidth current;


    public MorphologicalWidthParser() {
        morphWidths = new ArrayList<ImportMorphWidth>();
    }


    @Override
    protected void reset() {
        current = new ImportMorphWidth();
    }


    @Override
    protected void finish() {
        if (current != null) {
            morphWidths.add(current);
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
        if (handleMetaUnit(line)) {
            return;
        }
        else {
            log.warn("MWP: Unknown meta line: '" + line + "'");
        }
    }


    protected boolean handleMetaUnit(String line) {
        Matcher m = META_UNIT.matcher(line);

        if (m.matches()) {
            String unit = m.group(1);

            current.setUnit(new ImportUnit(unit));

            return true;
        }

        return false;
    }


    protected void handleDataLine(String line) {
        String[] vals = line.split(SEPERATOR_CHAR);

        if (vals == null || vals.length < 2) {
            log.warn("MWP: skip invalid data line: '" + line + "'");
            return;
        }

        try {
            BigDecimal km    = new BigDecimal(nf.parse(vals[0]).doubleValue());
            BigDecimal width = new BigDecimal(nf.parse(vals[1]).doubleValue());

            String desc = vals.length > 2 ? vals[2] : null;

            current.addValue(new ImportMorphWidthValue(
                km,
                width,
                desc
            ));
        }
        catch (ParseException pe) {
            log.warn("MWP: unparseable number in data row: " + line);
        }
    }


    public List<ImportMorphWidth> getMorphologicalWidths() {
        return morphWidths;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
