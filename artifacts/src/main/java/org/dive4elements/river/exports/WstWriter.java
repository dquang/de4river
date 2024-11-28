/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.lang.StringUtils;

import org.dive4elements.river.artifacts.model.WstLine;


/**
 * A writer that creates WSTs.
 *
 * Wst files follow this basic structure:
 *
 * HEADER
 * Q-LINE
 * W-LINES
 * Q-LINE
 * W-LINES
 * ...
 *
 * where each *LINE consists of X columns that are specified in the header.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WstWriter {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(WstWriter.class);

    /** The default unit that is written into the header of the WST. */
    public static final String DEFAULT_UNIT = "Wassserstand [NN + m]";

    /** The lines that need to be included for the export. */
    protected Map<Double, WstLine> lines;

    /** The column names. */
    protected List<String> columnNames;

    /** The locale used to format the values. */
    protected Locale locale;

    /** The number of discharge columns. */
    protected int cols;

    /** The last Q values. */
    protected double[] qs;

    /** Workaround for one use of wrongly imported files: ignore the Qs at
     * all. */
    protected boolean ignoreQs;

    /**
     * This constructor creates a new WstWriter with a number of Q columns.
     *
     * @param columns The number of columns of the resulting WST.
     */
    public WstWriter(int columns) {
        this(columns, false);
    }

    /**
     * This constructor creates a new WstWriter with a number of Q columns.
     *
     * @param columns The number of columns of the resulting WST.
     * @param workaroundIgnoreQs do not write QLines to shadow broken data.
     */
    public WstWriter(int columns, boolean workaroundIgnoreQs) {
        this.cols        = columns;
        this.columnNames = new ArrayList<String>(cols);
        this.lines       = new HashMap<Double, WstLine>();
        this.qs          = new double[cols];
        this.locale      = Locale.US;
        this.ignoreQs    = workaroundIgnoreQs;
    }


    /**
     * This method is used to create the WST from the data that has been
     * inserted using add(double[]) before.
     * @param out Where to write to.
     */
    public void write(OutputStream out) {
        log.info("WstWriter.write");

        PrintWriter writer = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(out)));

        this.qs = new double[cols];

        writeHeader(writer);

        Collection<WstLine> collection = new TreeMap(lines).values();

        for (WstLine line: collection) {
            writeWLine(writer, line);
        }

        writer.flush();
        writer.close();
    }


    /**
     * This method is used to add a new line to the WST.
     *
     * @param wqkms A 3dim double array with [W, Q, KM].
     */
    public void add(double[] wqkms) {
        Double km = wqkms[2];

        WstLine line = lines.get(km);

        if (line == null) {
            line = new WstLine(km.doubleValue());
            lines.put(km, line);
        }

        line.add(wqkms[0], wqkms[1]);
    }


    public void addCorrected(double[] wqckms) {
        Double km = wqckms[2];

        WstLine line = lines.get(km);

        if (line == null) {
            line = new WstLine(km.doubleValue());
            lines.put(km, line);
        }

        line.add(wqckms[3], wqckms[1]);
    }


    /**
     * Adds a further column name.
     *
     * @param name The name of the new column.
     */
    public void addColumn(String name) {
        if (name != null) {
            cols++;

            int i = 0;
            String basename = name;
            while (columnNames.contains(name)) {
                name = basename + "_" + i++;
            }

            columnNames.add(name);
        }
    }


    /**
     * This method writes the header of the WST.
     *
     * @param writer The PrintWriter that creates the output.
     */
    protected void writeHeader(PrintWriter writer) {
        log.debug("WstWriter.writeHeader");

        writer.println(cols);

        writer.print("*!column-bez-text ");

        List<String> quotedNames = new ArrayList<String>(columnNames.size());

        for (String name: columnNames) {
            if (name.contains(" ")) {
                name = '"' + name + '"';
            }
            quotedNames.add(name);
        }
        writer.println(StringUtils.join(quotedNames, " "));
        writer.print("        ");

        for (String columnName: columnNames) {
            if (columnName.length() > 9) {
                writer.printf(locale, "%9s",
                        columnName.substring(columnName.length() - 9));
            } else {
                writer.printf(locale, "%9s", columnName);
                // This is weird but i was to lazy to lookup
                // how to do this another way.
                for (int i = 9 - columnName.length(); i > 0; i--) {
                    writer.print(" ");
                }
            }
        }

        writer.println();

        writer.write("*   KM     ");
        writer.write(DEFAULT_UNIT);
        writer.println();
    }


    /**
     * This method writes a line with W values and a certain kilometer.
     *
     * @param writer The PrintWriter that is used to create the output.
     * @param line The WstLine that should be written to the output.
     */
    protected void writeWLine(PrintWriter writer, WstLine line) {
        double   km  = line.getKm();
        double[] qs  = line.getQs();
        int      num = line.getSize();

        if (!ignoreQs && dischargesChanged(qs)) {
            writeQLine(writer, qs);
        }

        writer.printf(locale, "%8.3f", km);

        for (int i = 0; i < num; i++) {
            writer.printf(locale, "%9.2f", line.getW(i));
        }

        writer.println();
    }


    /**
     * Writes a discharge line (Q values) into a WST.
     *
     * @param qs the Q values for the next range.
     */
    protected void writeQLine(PrintWriter writer, double[] qs) {
        writer.write("*\u001f      ");

        for (int i = 0; i < qs.length; i++) {
            this.qs[i] = qs[i];

            writer.printf(locale, "%9.2f", qs[i]);
        }

        writer.println();
    }


    /**
     * This method determines if a Q has changed from the last line to the
     * current one.
     *
     * @param newQs The Q values of the next line.
     *
     * @return true, if a Q value have changed, otherwise false.
     */
    protected boolean dischargesChanged(double[] newQs) {
        // XXX maybe there is a way to do this faster
        for (int i = 0; i < cols && i < qs.length && i < newQs.length; i++) {
            if (Math.abs(newQs[i] - qs[i]) >= 0.001) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the lines that have alreay been added to this writer
     * lines are a map with km as the key and a wstline as value.
     */
    public Map<Double, WstLine> getLines() {
        return lines;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
