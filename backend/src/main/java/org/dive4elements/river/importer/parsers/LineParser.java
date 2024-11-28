/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Base-Class for parsers for line-based file formats.
 * Calls reset(), then read line by line, calling handleLine() for each,
 * then calls finish().
 */
public abstract class LineParser {

    /** Private log. */
    private static final Logger log = LogManager.getLogger(LineParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    public static final String START_META_CHAR = "#";
    public static final String SEPERATOR_CHAR  = ";";

    public static class LineParserException extends Exception {
        public LineParserException() {
        }

        public LineParserException(String msg) {
            super(msg);
        }
    } // class LineParserException


    protected abstract void handleLine(int lineNum, String line)
        throws LineParserException;

    protected abstract void reset();

    protected abstract void finish();

    /** Name of file parsed. */
    protected String fileName;

    protected File inputFile;


    /**
     * This method reads each line of <i>file</i>. At the beginning,
     * <i>reset()</i> is called;
     * afterwards for each line <i>handleLine()</i> is
     * called; at the end <i>finish</i> is called.
     *
     * @param file The file which should be parsed.
     */
    public void parse(File file) throws IOException {
        log.info("Parsing file '" + file + "'");

        inputFile = file;

        fileName = file.getName();

        reset();

        LineNumberReader in = null;
        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line    = null;
            int    lineNum = 1;
            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() == 0) {
                    lineNum++;
                    continue;
                }

                handleLine(lineNum++, line);
            }
        } catch (LineParserException lpe) {
            log.error("Error while parsing file '" + file + "'", lpe);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        finish();
    }


    /** Returns the name of the file parsed. */
    protected String getFileName() {
        return fileName;
    }

    /** Returns the file currently parsed. */
    protected File getInputFile() {
        return inputFile;
    }


    protected static String stripMetaLine(String line) {
        String tmp = line.substring(1, line.length());

        // meta-lines often have trailing semicolons in real data
        return tmp.replaceAll(SEPERATOR_CHAR + "*$", "").trim();
    }

    public static double getDouble(String doubleString)
        throws ParseException {
        NumberFormat nf = NumberFormat.getInstance(DEFAULT_LOCALE);
        Number value = nf.parse(doubleString);

        return value.doubleValue();
    }

    public static Date getDate(String dateString) throws ParseException {
        DateFormat df = SimpleDateFormat.getDateInstance(
            SimpleDateFormat.MEDIUM, DEFAULT_LOCALE);

        return df.parse(dateString);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
