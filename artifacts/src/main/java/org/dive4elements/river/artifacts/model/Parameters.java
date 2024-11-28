/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.math.Linear;

import org.dive4elements.river.utils.DoubleUtil;

import gnu.trove.TDoubleArrayList;

import java.io.Serializable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Parameters
implements   Serializable
{
    private static Logger log = LogManager.getLogger(Parameters.class);

    public interface Visitor {

        void visit(double [] row);

    } // interface Visitor

    public static final double EPSILON = 1e-4;

    protected String []           columnNames;
    protected TDoubleArrayList [] columns;

    public Parameters() {
    }

    public Parameters(String [] columnNames) {
        if (columnNames == null || columnNames.length < 1) {
            throw new IllegalArgumentException("columnNames too short.");
        }
        this.columnNames = columnNames;
        columns = new TDoubleArrayList[columnNames.length];
        for (int i = 0; i < columns.length; ++i) {
            columns[i] = new TDoubleArrayList();
        }
    }

    public int columnIndex(String name) {
        for (int i = 0; i < columnNames.length; ++i) {
            if (columnNames[i].equals(name)) {
                return i;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("columnIndex: " + name + " not found in columnNames");
        }
        return -1;
    }

    public int newRow() {

        int N = columns[0].size();

        for (int i = 0; i < columns.length; ++i) {
            columns[i].add(Double.NaN);
        }

        return N;
    }

    public double get(int row, int index) {
        return columns[index].getQuick(row);
    }

    public double get(int i, String columnName) {
        int index = columnIndex(columnName);
        return index >= 0
            ? columns[index].getQuick(i)
            : Double.NaN;
    }

    public void set(int row, int index, double value) {
        columns[index].setQuick(row, value);
    }

    public void set(int i, String columnName, double value) {
        int idx = columnIndex(columnName);
        if (idx >= 0) {
            columns[idx].setQuick(i, value);
        }
    }

    public boolean set(int row, int [] indices, double [] values) {
        boolean invalid = false;
        for (int i = 0; i < indices.length; ++i) {
            double v = values[i];
            if (Double.isNaN(v)) {
                invalid = true;
            }
            else {
                columns[indices[i]].setQuick(row, v);
            }
        }
        return invalid;
    }

    public boolean set(int row, String [] names, double [] values) {
        boolean success = true;
        for (int i = 0; i < names.length; ++i) {
            int idx = columnIndex(names[i]);
            if (idx >= 0) {
                columns[idx].setQuick(row, values[i]);
            }
            else {
                success = false;
            }
        }
        return success;
    }

    public int size() {
        return columns[0].size();
    }

    public int getNumberColumns() {
        return columnNames.length;
    }

    public String [] getColumnNames() {
        return columnNames;
    }

    public void removeNaNs() {
        DoubleUtil.removeNaNs(columns);
    }

    public int [] columnIndices(String [] columns) {
        int [] indices = new int[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            indices[i] = columnIndex(columns[i]);
        }
        return indices;
    }

    public double getValue(int row, String column) {
        int idx = columnIndex(column);
        return idx >= 0
            ? columns[idx].getQuick(row)
            : Double.NaN;
    }

    public double [] get(int row, String [] columns) {
        return get(row, columns, new double[columns.length]);
    }

    public double [] get(int row, String [] columns, double [] values) {
        for (int i = 0; i < columns.length; ++i) {
            int idx = columnIndex(columns[i]);
            values[i] = idx < 0
                ? Double.NaN
                : this.columns[idx].getQuick(row);
        }

        return values;
    }

    public void get(int row, int [] columnIndices, double [] values) {
        for (int i = 0; i < columnIndices.length; ++i) {
            int index = columnIndices[i];
            values[i] = index >= 0 && index < columns.length
                ? columns[index].getQuick(row)
                : Double.NaN;
        }
    }

    public int binarySearch(String columnName, double value) {
        return binarySearch(columnIndex(columnName), value);
    }

    /**
     * Performes a binary search in the column identified by its
     * index.
     * @return Index of found element or negative insertion point
     *         (shifted by one)
     */
    public int binarySearch(int columnIndex, double value) {
        TDoubleArrayList column = columns[columnIndex];
        return column.binarySearch(value);
    }

    public int binarySearch(String columnName, double value, double epsilon) {
        return binarySearch(columnIndex(columnName), value, epsilon);
    }

    public int binarySearch(int columnIndex, double value, double epsilon) {
        if (epsilon < 0d) epsilon = -epsilon;
        double vl = value - epsilon;
        double vh = value + epsilon;

        TDoubleArrayList column = columns[columnIndex];
        int lo = 0, hi = column.size()-1;
        while (hi >= lo) {
            int mid = (lo + hi) >> 1;
            double v = column.getQuick(mid);
            if      (v < vl) lo = mid + 1;
            else if (v > vh) hi = mid - 1;
            else             return mid;
        }

        return -(lo + 1);
    }

    public double [] interpolate(int columnIndex, double key) {
        return interpolate(columnIndex, key, new double[columns.length]);
    }

    public double [] interpolate(String columnName, double key) {
        return interpolate(
            columnIndex(columnName), key, new double[columns.length]);
    }

    public double [] interpolate(
        String    columnName,
        double    key,
        double [] values
    ) {
        return interpolate(columnIndex(columnName), key, values);
    }

    public double [] interpolate(
        int       columnIndex,
        double    key,
        double [] values
    ) {
        int row = binarySearch(columnIndex, key, EPSILON);

        if (row >= 0) { // direct hit
            for (int i = 0; i < values.length; ++i) {
                values[i] = columns[i].getQuick(row);
            }
        }
        else {
            row = -row - 1;
            if (row < 1 || row >= size()) {
                return null;
            }
            double v1 = columns[columnIndex].getQuick(row-1);
            double v2 = columns[columnIndex].getQuick(row);
            double factor = Linear.factor(key, v1, v2);
            for (int i = 0; i < values.length; ++i) {
                values[i] = Linear.weight(
                    factor,
                    columns[i].getQuick(row-1),
                    columns[i].getQuick(row));
            }
        }
        return values;
    }


    public double [] interpolate(
        String    keyName,
        double    key,
        String [] columnNames
    ) {
        int keyIndex = columnIndex(keyName);
        return keyIndex < 0
            ? null
            : interpolate(keyIndex, key, columnNames);
    }

    public double [] interpolate(
        int       keyIndex,
        double    key,
        String [] columnNames
    ) {
        int row = binarySearch(keyIndex, key, EPSILON);

        if (row >= 0) { // direct match
            double [] values = new double[columnNames.length];
            for (int i = 0; i < values.length; ++i) {
                int ci = columnIndex(columnNames[i]);
                values[i] = ci < 0
                    ? Double.NaN
                    : columns[ci].getQuick(row);
            }
            return values;
        }

        row = -row - 1;
        if (row < 1 || row >= size()) {
            log.debug("interpolate: row is out of bounds");
            return null;
        }

        double v1 = columns[keyIndex].getQuick(row-1);
        double v2 = columns[keyIndex].getQuick(row);
        double factor = Linear.factor(key, v1, v2);

        double [] values = new double[columnNames.length];

        for (int i = 0; i < values.length; ++i) {
            int ci = columnIndex(columnNames[i]);
            values[i] = ci < 0
                ? Double.NaN
                : Linear.weight(
                    factor,
                    columns[ci].getQuick(row-1),
                    columns[ci].getQuick(row));
        }

        return values;
    }

    public double [] interpolateWithLimit(
        String    keyName,
        double    key,
        String [] columnNames,
        double    limit
    ) {
        int keyIndex = columnIndex(keyName);
        return keyIndex < 0
            ? null
            : interpolateWithLimit(keyIndex, key, columnNames, limit);
    }

    /* Only interpolate if the difference between the two key's
     * is less then limit */
    public double [] interpolateWithLimit(
        int       keyIndex,
        double    key,
        String [] columnNames,
        double    limit
    ) {
        int row = binarySearch(keyIndex, key, EPSILON);

        if (row >= 0) { // direct match
            double [] values = new double[columnNames.length];
            for (int i = 0; i < values.length; ++i) {
                int ci = columnIndex(columnNames[i]);
                values[i] = ci < 0
                    ? Double.NaN
                    : columns[ci].getQuick(row);
            }
            return values;
        }

        row = -row - 1;
        if (row < 1 || row >= size()) {
            log.debug("interpolate: row is out of bounds");
            return null;
        }

        double v1 = columns[keyIndex].getQuick(row-1);
        double v2 = columns[keyIndex].getQuick(row);
        if (Math.abs(v1-v2) > limit) {
            return null;
        }
        double factor = Linear.factor(key, v1, v2);

        double [] values = new double[columnNames.length];

        for (int i = 0; i < values.length; ++i) {
            int ci = columnIndex(columnNames[i]);
            values[i] = ci < 0
                ? Double.NaN
                : Linear.weight(
                    factor,
                    columns[ci].getQuick(row-1),
                    columns[ci].getQuick(row));
        }

        return values;
    }

    public boolean isSorted(String columnName) {
        return isSorted(columnIndex(columnName));
    }

    public boolean isSorted(int columnIndex) {
        TDoubleArrayList column = columns[columnIndex];
        for (int i = 1, N = column.size(); i < N; ++i) {
            if (column.getQuick(i-1) > column.getQuick(i)) {
                return false;
            }
        }
        return true;
    }

    public void visit(Visitor visitor) {
        visit(visitor, new double[columns.length]);
    }

    public void visit(Visitor visitor, double [] data) {
        for (int i = 0, R = size(); i < R; ++i) {
            for (int j = 0; j < data.length; ++j) {
                data[j] = columns[j].getQuick(i);
            }
            visitor.visit(data);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
