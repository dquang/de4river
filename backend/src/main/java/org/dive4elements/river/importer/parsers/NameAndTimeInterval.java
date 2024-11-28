/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.importer.parsers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.backend.utils.DateGuesser;

public class NameAndTimeInterval {

    private static Logger log = LogManager.getLogger(NameAndTimeInterval.class);

    // TODO: To be extented.
    private static final Pattern MAIN_VALUE = Pattern.compile(
        "^(HQ|HSQ|MHW|GLQ|NMQ|HQEXT)(\\d*)$");

    private String             name;
    private ImportTimeInterval timeInterval;

    public NameAndTimeInterval() {
    }

    public NameAndTimeInterval(String name) {
        this(name, null);
    }

    public NameAndTimeInterval(String name, ImportTimeInterval timeInterval) {
        this.name         = name;
        this.timeInterval = timeInterval;
    }

    public String getName() {
        return name;
    }

    public ImportTimeInterval getTimeInterval() {
        return timeInterval;
    }

    @Override
    public String toString() {
        return "name: " + name + " time interval: " + timeInterval;
    }

    public static boolean isMainValue(String s) {
        s = s.replace(" ", "").toUpperCase();
        return MAIN_VALUE.matcher(s).matches();
    }

    public static NameAndTimeInterval parseName(String name) {
        List<String> result = new ArrayList<String>();

        unbracket(name, 0, result);

        int length = result.size();

        if (length < 1) { // Should not happen.
            return new NameAndTimeInterval(name);
        }

        if (length == 1) { // No date at all -> use first part.
            return new NameAndTimeInterval(result.get(0).trim());
        }

        if (length == 2) { // e.g. HQ(1994) or HQ(1994 - 1999)

            String type = result.get(0).trim();
            ImportTimeInterval timeInterval = null;

            String datePart = result.get(1).trim();
            if (isMainValue(datePart)) { // e.g. W(HQ100)
                type += "(" + datePart + ")";
                timeInterval = null;
            }
            else {
                timeInterval = getTimeInterval(result.get(1).trim());

                if (timeInterval == null) { // No date at all.
                    type = name;
                }
            }

            return new NameAndTimeInterval(type, timeInterval);
        }

        if (length == 3) { // e.g W(Q(1994)) or W(Q(1994 - 1999))

            String type =
                result.get(0).trim() + "(" +
                result.get(1).trim() + ")";

            ImportTimeInterval timeInterval = getTimeInterval(
                result.get(2).trim());

            if (timeInterval == null) { // No date at all.
                type = name;
            }

            return new NameAndTimeInterval(type, timeInterval);
        }

        // more than 3 elements return unmodified.

        return new NameAndTimeInterval(name);
    }

    private static ImportTimeInterval getTimeInterval(String datePart) {

        int minus = datePart.indexOf('-');

        if (minus < 0) { // '-' not found

            Date date = null;
            try {
                date = DateGuesser.guessDate(datePart);
            }
            catch (IllegalArgumentException iae) {
                log.warn("STA: Invalid date '" + datePart + "'");
                return null;
            }

            return new ImportTimeInterval(date);
        }

        // Found '-' so we have <from> - <to>
        String startPart = datePart.substring(0, minus).trim();
        String endPart   = datePart.substring(minus).trim();

        Date startDate = null;
        Date endDate   = null;

        try {
            startDate = DateGuesser.guessDate(startPart);
        }
        catch (IllegalArgumentException iae) {
            log.warn("STA: Invalid start date '" + startPart + "'");
        }

        try {
            endDate = DateGuesser.guessDate(endPart);
        }
        catch (IllegalArgumentException iae) {
            log.warn("STA: Invalid end date '" + endPart + "'");
        }

        if (startDate == null) {
            log.warn("STA: Need start date.");
            return null;
        }

        return new ImportTimeInterval(startDate, endDate);
    }

    private static int unbracket(String s, int index, List<String> result) {
        StringBuilder sb = new StringBuilder();
        int length = s.length();
        while (index < length) {
            char c = s.charAt(index);
            switch (c) {
                case '(':
                    index = unbracket(s, index+1, result);
                    break;
                case ')':
                    result.add(0, sb.toString());
                    return index+1;
                default:
                    sb.append(c);
                    ++index;
            }
        }
        result.add(0, sb.toString());

        return index;
    }

    /*
    public static void main(String [] args) {
        for (String arg: args) {
            NameAndTimeInterval nti = parseName(arg);
            System.out.println(arg + " -> " + nti);
        }
    }
    */
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

