/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

/**
 * Wraps result(s) of a Calculation and eventual error reports.
 */
public class CalculationResult
implements   Serializable
{
    protected Object      data;
    protected Calculation report;

    public CalculationResult() {
    }

    public CalculationResult(Calculation report) {
        this(null, report);
    }

    /**
     * @param report report (e.g. error messages).
     */
    public CalculationResult(Object data, Calculation report) {
        this.data   = data;
        this.report = report;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Calculation getReport() {
        return report;
    }

    public void setReport(Calculation report) {
        this.report = report;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
