/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.artifacts.math.Identity;
import org.dive4elements.river.artifacts.math.AddScaleFunction;

public class WWAxisTypes
{
    protected boolean inCm1;
    protected boolean inCm2;

    public WWAxisTypes() {
        inCm1 = inCm2 = true;
    }

    public WWAxisTypes(WW ww) {
        this();
        classify(ww);
    }

    public void classify(WW ww) {
        if (!ww.startAtGauge()) inCm1 = false;
        if (!ww.endAtGauge())   inCm2 = false;
    }

    public boolean getInCm(int index) {
        switch (index) {
            case 0:  return inCm1;
            case 1:  return inCm2;
            default: return false;
        }
    }

    public boolean getInCm1() {
        return inCm1;
    }

    public void setInCm1(boolean inCm1) {
        this.inCm1 = inCm1;
    }

    public boolean getInCm2() {
        return inCm2;
    }

    public void setInCm2(boolean inCm2) {
        this.inCm2 = inCm2;
    }

    public WW.ApplyFunctionIterator transform(WW ww) {
        return transform(ww, false);
    }

    private static final double zero(Double d) {
        return d == null ? 0 : d;
    }

    public WW.ApplyFunctionIterator transform(WW ww, boolean normalized) {

        Function function1;
        Function function2;

        if (!normalized) {
            function1 = inCm1
                ? new AddScaleFunction(-ww.getStartDatum(), 100d)
                : Identity.IDENTITY;

            function2 = inCm2
                ? new AddScaleFunction(-ww.getEndDatum(), 100d)
                : Identity.IDENTITY;
        }
        else {
            double minW1 = ww.minWs();
            double minW2 = ww.minWs2();
            double scale1 = inCm1 ? 100d : 1d;
            double scale2 = inCm2 ? 100d : 1d;
            function1 = new AddScaleFunction(-minW1, scale1);
            function2 = new AddScaleFunction(-minW2, scale2);
        }

        return new WW.ApplyFunctionIterator(ww, function1, function2);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
