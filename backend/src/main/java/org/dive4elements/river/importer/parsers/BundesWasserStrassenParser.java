/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Parse CSV file that contains official numbers for rivers. */
public class BundesWasserStrassenParser extends LineParser {

    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(BundesWasserStrassenParser.class);

    /** Map from rivernames to Official numbers. */
    private HashMap<String,Long> numberMap;


    public BundesWasserStrassenParser() {
        numberMap = new HashMap<String,Long>();
    }


    /** No need to reset. */
    @Override
    protected void reset() {
    }


    /** No action needed on eof. */
    @Override
    protected void finish() {
    }


    /** Handle a line of the bwastr-id file. */
    @Override
    protected void handleLine(int lineNum, String line) {
        String[] vals = line.split(",");
        // Try both "," and ";" as separator.
        if (vals.length != 2) {
            vals = line.split(";");
            if (vals.length != 2) {
                log.warn("Invalid bwastr-id line:\n" + line);
                return;
            }
        }
        try {
            String name = unwrap(vals[0].toLowerCase());
            String numberStr = unwrap(vals[1]);
            Long number = Long.valueOf(numberStr);
            numberMap.put(name, number);
        }
        catch (NumberFormatException e) {
            log.warn("Invalid number in bwastr-id line:\n" + line);
        }
    }


    /** Get river -&gt; official number mapping. */
    public HashMap<String,Long> getMap() {
        return numberMap;
    }


    /** Remove leading and trailing quotes. */
    protected String unwrap(String input) {
        if (input.startsWith("\"")) {
            input = input.substring(1);
        }
        if (input.endsWith("\"")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
