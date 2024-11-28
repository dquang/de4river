/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.river.artifacts.model.Parameters;

import java.io.Serializable;

import java.util.List;

public class SQFractionResult
implements   Serializable
{
    public static class Iteration
    implements          Serializable
    {
        protected Parameters parameters;
        protected SQ []      measurements;
        protected SQ []      outliers;

        public Iteration() {
        }

        public Iteration(
            Parameters parameters,
            SQ []      measurements,
            SQ []      outliers
        ) {
            this.parameters   = parameters;
            this.measurements = measurements;
            this.outliers     = outliers;
        }

        public Parameters getParameters() {
            return parameters;
        }

        public void setParameters(Parameters parameters) {
            this.parameters = parameters;
        }

        public SQ [] getMeasurements() {
            return measurements;
        }

        public void setMeasurements(SQ [] measurements) {
            this.measurements = measurements;
        }

        public SQ [] getOutliers() {
            return outliers;
        }

        public void setOutliers(SQ [] outliers) {
            this.outliers = outliers;
        }

        public boolean isValid() {
            return parameters   != null
                && measurements != null
                && outliers     != null;
        }

        public int numOutliers() {
            return outliers != null
                ? outliers.length
                : 0;
        }

        public int numMeasurements() {
            return measurements != null
                ? measurements.length
                : 0;
        }
    } // class Iteration

    protected SQ []           measurements;
    protected List<Iteration> iterations;

    public SQFractionResult() {
    }

    public SQFractionResult(
        SQ []           measurements,
        List<Iteration> iterations
    ) {
        this.measurements = measurements;
        this.iterations   = iterations;
    }

    public SQ [] getMeasurements() {
        return measurements;
    }

    public void setMeasurements(SQ [] measurements) {
        this.measurements = measurements;
    }

    public List<Iteration> getIterations() {
        return iterations;
    }

    public void setIterations(List<Iteration> iterations) {
        this.iterations = iterations;
    }

    public double [] getQExtent() {
        return getQExtent(new double[2]);
    }

    public double [] getQExtent(double extent[]) {
        extent[0] =  Double.MAX_VALUE;
        extent[1] = -Double.MIN_VALUE;

        for (SQ sq: measurements) {
            double q = sq.getQ();
            if (q < extent[0]) extent[0] = q;
            if (q > extent[1]) extent[1] = q;
        }

        return extent;
    }

    public int numIterations() {
        return iterations != null ? iterations.size() : 0;
    }

    public Parameters getParameters() {
        return iterations != null && !iterations.isEmpty()
            ? iterations.get(iterations.size()-1).getParameters()
            : null;
    }

    public SQ [] getOutliers(int index) {
        return index >= 0 && index < iterations.size()
            ? iterations.get(index).getOutliers()
            : null;
    }

    public Parameters getParameters(int index) {
        return index >= 0 && index < iterations.size()
            ? iterations.get(index).getParameters()
            : null;
    }

    public SQ [] getMeasurements(int index) {
        return index >= 0 && index < iterations.size()
            ? iterations.get(index).getMeasurements()
            : null;
    }

    public int totalNumOutliers() {
        int sum = 0;
        if (iterations != null) {
            for (Iteration iteration: iterations) {
                sum += iteration.numOutliers();
            }
        }
        return sum;
    }

    public int numMeasurements() {
        return measurements != null
            ? measurements.length
            : 0;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
