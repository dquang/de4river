/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.dive4elements.river.utils.DoubleUtil;

import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.ArgumentOutsideDomainException;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import java.io.Serializable;

/** Holder of a specific result from the bed quality calculation.
 *
 * Data is always a map of km to value. The type "bedload"
 * translates to german "Geschiebe" other results are either
 * specific to the top or the sublayer of the riverbed.
 *
 * The name can be the diameter of this result for bed and bedload
 * data.
 **/
public class BedQualityResultValue implements Serializable {
    public static final String[] DIAMETER_NAMES = new String[] {
        "D90",
        "D84",
        "D80",
        "D75",
        "D70",
        "D60",
        "D50",
        "D40",
        "D30",
        "D25",
        "D20",
        "D16",
        "D10",
        "DM",
        "DMIN",
        "DMAX"
    };

    /* For ease of access */
    public static final Set<String> DIAMETER_NAME_SET = new HashSet<String>(
            Arrays.asList(DIAMETER_NAMES));

    private String      name;
    private String      type;
    private double [][] data;
    private transient PolynomialSplineFunction interpolFunc;
    private boolean isInterpolatableData;

    public BedQualityResultValue() {
        isInterpolatableData = false;
    }

    public BedQualityResultValue(String name, double [][] data, String type) {
        this.name = name;
        isInterpolatableData = false;
        setData(data);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isEmpty() {
        return data == null || data.length < 2 || data[0].length == 0;
    }

    public boolean isNaN() {
        if (isEmpty()) {
            return true;
        }
        for (int i = 0; i < data[0].length; i++) {
            if (!Double.isNaN(data[1][i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isInterpolateable() {
        return isInterpolatableData;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double [][] getData() {
        return data;
    }

    public double getData(double x) {
        int idx = Arrays.binarySearch(data[0], x);
        if (idx < 0) {
            return Double.NaN;
        } else {
            return data[1][idx];
        }
    }

    public double getDataInterpolated(double x) {
        if (interpolFunc == null) {
            interpolFunc = DoubleUtil.getLinearInterpolator(data[0], data[1]);
        }
        try {
            return interpolFunc.value(x);
        } catch (ArgumentOutsideDomainException e) {
            return getData(x);
        }
    }

    public double [][] getDataInterpolated(double[] x) {
        double y[] = new double[x.length];
        int i = 0;
        for (double point: x) {
            y[i++] = getDataInterpolated(point);
        }
        return new double[][] {x, y};
    }

    public void setData(double [][] data) {
        this.data = data;

        if (!isEmpty()
            && data[0].length > 1
            && data[0].length == data[1].length
        ) {
            int usable_points = 0;
            for (double val :data[1]) {
                if (!Double.isNaN(val)) {
                    usable_points++;
                }
                if (usable_points == 2) {
                    isInterpolatableData = true;
                    return;
                }
            }
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /** Checks wether or not the name matches that of a diameter */
    public boolean isDiameterResult() {
        return DIAMETER_NAME_SET.contains(name.toUpperCase());
    }
}
