/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared;


public final class DoubleUtils {

    public static final String DEFAULT_DELIM = " ";


    private DoubleUtils() {
    }


    public static Double getDouble(String value) {
        try {
            return Double.valueOf(value);
        }
        catch (NumberFormatException nfe) {
            // do nothing
        }

        return null;
    }


    public static double[] getMinMax(String value) {
        return getMinMax(value, DEFAULT_DELIM);
    }


    public static double[] getMinMax(String value, String delim) {
        if (value == null) {
            return null;
        }

        String[] values = value.split(delim);

        int     len = values != null ? values.length : 0;
        double[] mm = new double[] { Double.MAX_VALUE, -Double.MAX_VALUE };

        for (int i = 0; i < len; i++) {
            Double d = getDouble(values[i]);

            if (d != null) {
                mm[0] = mm[0] < d ? mm[0] : d;
                mm[1] = mm[1] > d ? mm[1] : d;
            }
        }

        return mm[0] != Double.MAX_VALUE && mm[1] != -Double.MAX_VALUE
            ? mm
            : null;
    }

    /** toIndex is not inclusive, fromIndex is. */
    static void fill(double[] array, int fromIndex, int toIndex, double val) {
        for (int i = fromIndex; i < toIndex; i++) {
            array[i] = val;
        }
    }

    /** @see java.util.Arrays.copyOf */
    public static double[] copyOf(double[] toCopy, int newLen) {
        double[] nArray = new double[newLen];

        if (toCopy == null) {
            fill(nArray, 0, nArray.length, 0d);
            return nArray;
        }

        int goTo = (newLen < toCopy.length)
            ? newLen
            : toCopy.length;

        for (int i = 0; i < goTo; i++) {
            nArray[i] = toCopy[i];
        }
        fill (nArray, goTo, nArray.length, 0d);
        return nArray;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
