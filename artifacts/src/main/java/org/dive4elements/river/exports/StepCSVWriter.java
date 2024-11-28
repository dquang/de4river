/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import au.com.bytecode.opencsv.CSVWriter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to overcome shortcoming of CSVWriter to accept String-Arrays only.
 * The StepCSVWriter buffers incoming values, such that rows in a csv can be
 * created more dynamically. Do not forget to call flush().
 */
public class StepCSVWriter {

    /** Writer to use when calling flush. */
    CSVWriter writer = null;
    /** Buffer of strings (values). */
    ArrayList<String> buffer;


    /** Trivial constructor. */
    public StepCSVWriter() {
        buffer = new ArrayList<String>();
    }


    /** Set writer. */
    public void setCSVWriter(CSVWriter writer) {
        this.writer = writer;
    }


    /** Add a value to next flush. */
    public void addNext(String value) {
        buffer.add(value);
    }


    /** Add many values to next flush. */
    public void addNexts(String ... values) {
        buffer.addAll(Arrays.asList(values));
    }


    /** Write the row with csvwriter. */
    public void flush() {
        writer.writeNext(buffer.toArray(new String[buffer.size()]));
        buffer.clear();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
