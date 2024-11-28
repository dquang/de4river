/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportDischargeTable;
import org.dive4elements.river.importer.ImportDischargeTableValue;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Date;
import java.util.Calendar;

import org.dive4elements.river.importer.ImportTimeInterval;

/** Parse *.at (Abflusstafeln?) files. */
public class AtFileParser {

    public static final String ENCODING = "ISO-8859-1";

    private static Logger log = LogManager.getLogger(AtFileParser.class);

    // regular expression from hell to find out time range
    public static final Pattern DATE_LINE = Pattern.compile(
        "^\\*\\s*Abflu[^t]+tafel?\\s*([^\\d]+)"  +
        "(\\d{1,2})?\\.?(\\d{1,2})?\\.?(\\d{2,4})\\s*(?:(?:bis)|-)?\\s*" +
        "(?:(\\d{1,2})?\\.?(\\d{1,2})?\\.?(\\d{2,4}))?\\s*.*$");

    public AtFileParser() {
    }


    public ImportDischargeTable parse(File file) throws IOException {
        return parse(file, "", 0);
    }

    public ImportDischargeTable parse(
        File   file,
        String prefix,
        int    kind
    )
    throws IOException {

        log.info("parsing AT file: " + file);

        BufferedReader br = null;

        String line       = null;

        boolean beginning = true;

        ImportDischargeTable dischargeTable =
            new ImportDischargeTable(kind, prefix + file.getName());

        Date from = null;
        Date to   = null;

        try {
            br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(file), ENCODING));

            while ((line = br.readLine()) != null) {

                String tmp = line.trim();

                if (tmp.length() == 0) {
                    continue;
                }

                Matcher m = DATE_LINE.matcher(tmp);
                if (m.matches()) {
                    from = guessDate(m.group(2), m.group(3), m.group(4));
                    to   = guessDate(m.group(5), m.group(6), m.group(7));
                    if (from == null) {
                        Date t = from; from = to; to = t;
                    }
                    continue;
                }

                if (tmp.startsWith("#! name=")) {
                    // XXX Skip the name,  because we don't know where to save
                    // it at the moment

                    //String name = tmp.substring(8);
                    continue;
                }

                if (tmp.startsWith("#") || tmp.startsWith("*")) {
                    continue;
                }

                String[] splits = tmp.replace(',', '.').split("\\s+");

                if ((splits.length < 2) || (splits.length > 11)) {
                    log.warn("Found an invalid row in the AT file.");
                    continue;
                }

                String strW = splits[0].trim();
                double W    = Double.parseDouble(strW);

                /* shift is used to differenciate between lines with
                 * exactly 10 Qs and lines with less than 10 Qs. The shift
                 * is only modified when it is the first line.
                 */
                int shift = -1;

                if (splits.length != 11 && beginning) {
                    shift = 10 - splits.length;
                }


                for (int i = 1; i < splits.length; i++) {
                    double iW = W + shift + i;
                    double iQ = Double.parseDouble(splits[i].trim());

                    dischargeTable.addDischargeTableValue(
                        new ImportDischargeTableValue(
                            new BigDecimal(iQ),
                            new BigDecimal(iW)));
                }

                beginning = false;
            }
        }
        catch (NumberFormatException pe) {
            log.warn("AT: invalid number " + pe.getMessage());
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe.getMessage());
        }
        finally {
            if (br != null) {
                br.close();
            }
        }

        if (from != null) {
            if (to != null && from.compareTo(to) > 0) {
                Date t = from; from = to; to = t;
            }
            log.info("from: " + from + " to: " + to);
            ImportTimeInterval interval = new ImportTimeInterval(from, to);
            dischargeTable.setTimeInterval(interval);
        }

        log.info("Finished parsing AT file: " + file);

        return dischargeTable;
    }

    public static Date guessDate(String day, String month, String year) {
        // TODO evaluate whether DateGuesser class can do that.
        if (day == null && month == null && year == null) {
            return null;
        }

        log.debug("day: " + day + " month: " + month + " year: " + year);

        int dayI = 15;
        if (day != null) {
            try {
                dayI = Integer.parseInt(day.trim());
            }
            catch (NumberFormatException nfe) {
            }
        }

        int monthI = 6;
        if (month != null) {
            try {
                monthI = Integer.parseInt(month.trim());
            }
            catch (NumberFormatException nfe) {
            }
        }

        int yearI = 1900;
        if (year != null) {
            try {
                yearI = Integer.parseInt(year.trim());
                if (yearI < 100) {
                    if (yearI < 20) {
                        yearI += 2000;
                    }
                    else {
                        yearI += 1900;
                    }
                }
            }
            catch (NumberFormatException nfe) {
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.set(yearI, monthI-1, dayI, 12, 0, 0);
        long ms = cal.getTimeInMillis();
        cal.setTimeInMillis(ms - ms%1000);
        return cal.getTime();
    }


    /** Parse one or more files, (useful for debugging), */
    public static void main(String [] args) {

        AtFileParser parser = new AtFileParser();

        try {
            for (String arg: args) {
                parser.parse(new File(arg));
            }
        } catch(Exception e) {
            log.error("Exception caught " + e);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
