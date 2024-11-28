/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.text.NumberFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.backend.utils.StringUtil;
import org.dive4elements.river.backend.utils.DateGuesser;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.math.BigDecimal;

import org.dive4elements.river.importer.ImportWstQRange;
import org.dive4elements.river.importer.ImportWstColumn;
import org.dive4elements.river.importer.ImportTimeInterval;
import org.dive4elements.river.importer.ImportRange;
import org.dive4elements.river.importer.ImportUnit;
import org.dive4elements.river.importer.ImportWst;

public class WstParser
{
    private static Logger log = LogManager.getLogger(WstParser.class);

    public static final String COLUMN_BEZ_TEXT   = "column-bez-text";
    public static final String COLUMN_BEZ_BREITE = "column-bez-breite";
    public static final String COLUMN_QUELLE     = "column-quelle";
    public static final String COLUMN_DATUM      = "column-datum";

    public static final BigDecimal UNDEFINED_ZERO =
        new BigDecimal(0.0);
    public static final BigDecimal MIN_RANGE =
        new BigDecimal(-Double.MAX_VALUE);
    public static final BigDecimal MAX_RANGE =
        new BigDecimal(Double.MAX_VALUE);

    public static final String ENCODING = "ISO-8859-1";

    public static final Pattern UNIT_COMMENT =
        Pattern.compile("\\*\\s*[kK][mM]\\s+(.+)");

    public static final Pattern UNIT =
        Pattern.compile("[^\\[]*\\[([^]]+)\\].*");

    public static final Pattern YEAR_INTERVAL =
        Pattern.compile("(\\d{4})\\s*[-/]\\s*(\\d{4})");

    public static final double INTERVAL_GAP = 0.00001d;

    protected ImportWst wst;

    protected ImportRange lastRange;
    protected Double lastA;
    protected Double lastB;

    public WstParser() {
    }

    public WstParser(ImportWst wst) {
        this.wst = wst;
    }

    public ImportWst getWst() {
        return wst;
    }

    public void setWst(ImportWst wst) {
        this.wst = wst;
    }

    public static final class ParseException extends Exception {
        public ParseException() {
        }

        public ParseException(String msg) {
            super(msg);
        }
    } // class ParseException

    /** Returns a new ImportTimeInterval with a date guessed from string. */
    public static ImportTimeInterval guessDate(String string) {
        try {
            Matcher m = YEAR_INTERVAL.matcher(string);
            if (m.matches()) {
                return new ImportTimeInterval(
                    DateGuesser.guessDate(m.group(1)),
                    DateGuesser.guessDate(m.group(2)));
            }

            return new ImportTimeInterval(
                DateGuesser.guessDate(string));
        }
        catch (IllegalArgumentException iae) {
            log.warn("WST: String '" + string +
                     "' could not be interpreted as valid timestamp");
        }
        return null;
    }

    public void parse(File file) throws IOException, ParseException {

        log.info("Parsing WST file '" + file + "'");

        if (wst == null) {
            wst = new ImportWst(file.getName());
        }
        else {
            wst.setDescription(file.getName());
        }

        LineNumberReader in =
            new LineNumberReader(
            new InputStreamReader(
            new FileInputStream(file), ENCODING));
        try {
            String input;
            boolean first = true;
            int columnCount = 0;

            String [] lsBezeichner   = null;
            String [] langBezeichner = null;
            int    [] colNaWidths    = null;
            String [] quellen        = null;
            String [] daten          = null;

            BigDecimal [] aktAbfluesse   = null;
            BigDecimal [] firstAbfluesse = null;

            BigDecimal minKm   = MAX_RANGE;
            BigDecimal maxKm   = MIN_RANGE;
            BigDecimal kmHist1 = null;
            BigDecimal kmHist2 = null;

            boolean columnHeaderChecked = false;

            /* Default string for altitude reference
             * if none is found in WST-file.
             * Use in case no unit comment is found in file */
            String einheit = "m ü. unbekannte Referenz";
            boolean unitFound = false;

            HashSet<BigDecimal> kms = new HashSet<BigDecimal>();

            while ((input = in.readLine()) != null) {
                String line = input;
                if (first) { // fetch number of columns
                    if ((line = line.trim()).length() == 0) {
                        continue;
                    }
                    try {
                        columnCount = Integer.parseInt(line);
                        if (columnCount <= 0) {
                            throw new NumberFormatException(
                                "number of columns <= 0");
                        }
                        log.debug("Number of columns: " + columnCount);
                        wst.setNumberColumns(columnCount);
                        lsBezeichner = new String[columnCount];
                    }
                    catch (NumberFormatException nfe) {
                        log.warn("WST: invalid number.", nfe);
                        continue;
                    }
                    first = false;
                    continue;
                }

                line = line.replace(',', '.');

                // handle Q-lines
                if (line.startsWith("*\u001f")) {
                    BigDecimal [] data = parseLineAsDouble(
                        line, columnCount, false, true);

                    if (aktAbfluesse != null) {
                        // add Q-ranges obtained from previous lines
                        if (kmHist1 != null && kmHist2 != null
                        && kmHist1.compareTo(kmHist2) < 0) {
                            // stations descending in file
                            BigDecimal t = minKm; minKm = maxKm; maxKm = t;
                        }
                        addInterval(minKm, maxKm, aktAbfluesse);
                        minKm = MAX_RANGE;
                        maxKm = MIN_RANGE;
                    }

                    // obtain Q-values from current line
                    aktAbfluesse = new BigDecimal[data.length];
                    log.debug("new q range: " + columnCount);
                    for (int i = 0; i < data.length; ++i) {
                        if (data[i] != null) {
                            log.debug("  column: " + data[i]);
                            aktAbfluesse[i] = data[i];
                        }
                    }

                    // remember Q-values from first Q-line
                    // for header generation
                    if (firstAbfluesse == null) {
                        firstAbfluesse = (BigDecimal [])aktAbfluesse.clone();
                    }
                    continue;
                }

                // handle special column identifiers
                if (line.startsWith("*!")) {
                    String spezial = line.substring(2).trim();

                    if (spezial.length() == 0) {
                        continue;
                    }

                    if (spezial.startsWith(COLUMN_BEZ_TEXT)) {
                        spezial = spezial.substring(
                            COLUMN_BEZ_TEXT.length()).trim();
                        if (spezial.length() == 0) {
                            continue;
                        }
                        langBezeichner = StringUtil.splitQuoted(spezial, '"');
                    }
                    else if (spezial.startsWith(COLUMN_BEZ_BREITE)) {
                        spezial = spezial.substring(
                            COLUMN_BEZ_BREITE.length()).trim();

                        if (spezial.length() == 0) {
                            continue;
                        }

                        String[] split = spezial.split("\\s+");

                        colNaWidths = new int[split.length];
                        for (int i=0; i < split.length; i++) {
                            colNaWidths[i] = Integer.parseInt(split[i]);
                        }
                    }
                    else if (spezial.startsWith(COLUMN_QUELLE)) {
                        spezial = spezial.substring(
                            COLUMN_QUELLE.length()).trim();
                        if (spezial.length() == 0) {
                            continue;
                        }
                        quellen = StringUtil.splitQuoted(spezial, '"');
                        log.debug("sources: " + Arrays.toString(quellen));
                    }
                    else if (spezial.startsWith(COLUMN_DATUM)) {
                        spezial = spezial.substring(
                            COLUMN_DATUM.length()).trim();
                        if (spezial.length() == 0) {
                            continue;
                        }
                        daten = StringUtil.splitQuoted(spezial, '"');
                    }
                    continue;
                }

                if (line.length() < 11) {
                    continue;
                }

                // handle comment lines to fetch unit
                if (line.startsWith("*")) {
                    Matcher m = UNIT_COMMENT.matcher(line);
                    if (m.matches()) {
                        log.debug("unit comment found");
                        // XXX: This hack is needed because desktop
                        // FLYS is broken figuring out the unit
                        String [] units = m.group(1).split("\\s{2,}");
                        m = UNIT.matcher(units[0]);
                        einheit = m.matches() ? m.group(1) : units[0];
                        log.debug("unit: " + einheit);
                        unitFound = true;
                    }

                    continue;
                }

                if (firstAbfluesse != null) {
                    if (!columnHeaderChecked) {
                        int unknownCount = 0;
                        HashSet<String> uniqueColumnNames =
                            new HashSet<String>();
                        if (langBezeichner != null) {
                            // use column name from '*!column-bez-text'-line
                            lsBezeichner = StringUtil.fitArray(
                                langBezeichner, lsBezeichner);
                        }
                        for (int i = 0; i < lsBezeichner.length; ++i) {
                            if (lsBezeichner[i] == null
                            || lsBezeichner[i].length() == 0) {
                                // generate alternative column names
                                double q = firstAbfluesse.length > i ?
                                    firstAbfluesse[i].doubleValue() : 0d;
                                if (q < 0.001) {
                                    lsBezeichner[i] =
                                        "<unbekannt #" + unknownCount + ">";
                                    ++unknownCount;
                                }
                                else {
                                    lsBezeichner[i] = "Q="+format(q);
                                }
                            }
                            String candidate = lsBezeichner[i];
                            int collision = 1;
                            while (!uniqueColumnNames.add(candidate)) {
                                candidate = lsBezeichner[i] +
                                    " (" + collision + ")";
                                ++collision;
                            }
                            ImportWstColumn iwc = wst.getColumn(i);
                            iwc.setName(candidate);
                            if (quellen != null && i < quellen.length) {
                                iwc.setSource(quellen[i]);
                            }
                            String potentialDate =
                                daten != null && i < daten.length
                                ? daten[i]
                                : candidate;
                            iwc.setTimeInterval(guessDate(potentialDate));
                        }
                        columnHeaderChecked = true;
                    }

                    BigDecimal [] data = parseLineAsDouble(
                        line, columnCount, true, false);

                    BigDecimal kaem = data[0];

                    if (!kms.add(kaem)) {
                        log.warn(
                            "WST: km " + kaem +
                            " (line " + in.getLineNumber() +
                            ") found more than once. -> ignored");
                        continue;
                    }

                    // check consistence of station ordering in file
                    if (kmHist2 != null &&
                        kmHist2.compareTo(kmHist1) != kmHist1.compareTo(kaem)
                    ) {
                        throw new ParseException("WST: Stations in " + file +
                            " near line " + in.getLineNumber() +
                            " not ordered. File rejected.");
                    }

                    // remember stations in two previous lines
                    kmHist2 = kmHist1;
                    kmHist1 = kaem;

                    // iteratively determine actual km-range
                    if (kaem.compareTo(minKm) < 0) {
                        minKm = kaem;
                    }
                    if (kaem.compareTo(maxKm) > 0) {
                        maxKm = kaem;
                    }

                    // extract values
                    for (int i = 0; i < data.length - 1; ++i) {
                        addValue(kaem, data[i+1], i);
                    }

                }
                else { // firstAbfluesse == null
                    if (langBezeichner != null) {
                        // nothing to do
                    }
                    else if (colNaWidths != null) {
                        for (int j = 0, i = 0, N = input.length();
                             j < colNaWidths.length && i < N;
                             i += colNaWidths[j++]
                        ) {
                            lsBezeichner[j] = input.substring(
                                i, i+colNaWidths[j]).trim();
                        }
                    }
                    else { // fetch column names from non-comment header line
                           // (above first Qs)
                        // first column begins at position 8 in line
                        for (int i = 8, col = 0; i < input.length(); i += 9) {
                            // one column header is 9 chars wide
                            // but the last one may be shorter
                            if (col < lsBezeichner.length) {
                                lsBezeichner[col++] =
                                    input.substring(
                                        i,
                                        Math.min(i + 9, input.length())
                                    ).trim();
                            }
                            if (col == lsBezeichner.length) {
                                break;
                            }
                        }
                    }
                }

            } // for all lines in WST file

            if (!unitFound) {
                log.warn("no unit and height reference found. Using default.");
            }
            wst.setUnit(new ImportUnit(einheit));

            // add Q-ranges obtained from previous lines
            // in case there was no further Q-line
            // but only if there were values following the last Q-line
            if (minKm != MAX_RANGE && maxKm != MIN_RANGE) {
                if (kmHist1 != null && kmHist2 != null
                && kmHist1.compareTo(kmHist2) < 0) {
                    // stations descending in file
                    BigDecimal t = minKm; minKm = maxKm; maxKm = t;
                }
                addInterval(minKm, maxKm, aktAbfluesse);
            }
        }
        finally {
            in.close();
        }
    }

    protected void addValue(BigDecimal km, BigDecimal w, int index) {
        if (w != null) {
            ImportWstColumn column = wst.getColumn(index);
            column.addColumnValue(km, w);
        }
    }

    private static final NumberFormat NF = getNumberFormat();

    private static final NumberFormat getNumberFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf;
    }

    protected static String format(double value) {
        return NF.format(value);
    }

    protected void addInterval(
        BigDecimal    from,
        BigDecimal    to,
        BigDecimal [] values
    ) {
        log.debug("addInterval: " + from + " " + to);

        if (values == null || from == MAX_RANGE || from == MIN_RANGE) {
            return;
        }

        // expand single-line i.e. 0-lenght Q-range to minimal length
        if (from == to) {
            if (lastRange != null && lastA > lastB) {
                to = new BigDecimal(from.doubleValue() - INTERVAL_GAP);
            }
            else {
                to = new BigDecimal(from.doubleValue() + INTERVAL_GAP);
            }
        }

        ImportRange range = new ImportRange(from, to);

        // little workaround to make the q ranges tightly fit.
        // Leave a very small gap to ensure that the range queries
        // still work.

        if (lastRange != null) {
            double a2 = range.getA().doubleValue();
            double b2 = range.getB().doubleValue();

            if (lastA < lastB) {
                lastRange.setB(new BigDecimal(a2 - INTERVAL_GAP));
            }
            else { // lastA >= lastB
                lastRange.setA(new BigDecimal(b2 + INTERVAL_GAP));
            }
        }

        for (int i = 0; i < values.length; ++i) {
            ImportWstColumn column = wst.getColumn(i);
            ImportWstQRange wstQRange = new ImportWstQRange(range, values[i]);
            column.addColumnQRange(wstQRange);
        }

        lastA = from.doubleValue();
        lastB = to.doubleValue();
        lastRange = range;
    }

    private static final BigDecimal [] parseLineAsDouble(
        String  line,
        int     count,
        boolean bStation,
        boolean bParseEmptyAsZero
    ) throws ParseException {
        String [] tokens = parseLine(line, count, bStation);

        BigDecimal [] doubles = new BigDecimal[tokens.length];

        for (int i = 0; i < doubles.length; ++i) {
            String token = tokens[i].trim();
            if (token.length() != 0) {
                doubles[i] = new BigDecimal(token);
            }
            else if (bParseEmptyAsZero) {
                doubles[i] = UNDEFINED_ZERO;
            }
        }

        return doubles;
    }

    private static String [] parseLine(
        String  line,
        int     tokenCount,
        boolean bParseStation
    ) throws ParseException {
        ArrayList<String> strings = new ArrayList<String>();

        if (bParseStation) {
            if (line.length() < 8) {
                throw new IllegalArgumentException("station too short");
            }
            strings.add(line.substring(0, 8));
        }

        int pos = 0;
        for (int i = 0; i < tokenCount; ++i) {
            pos += 9;
            if (pos >= line.length()) {
                break;
            }
            strings.add(line.substring(pos,
                Math.min(pos + 8, line.length())));
        }

        return strings.toArray(new String[strings.size()]);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
