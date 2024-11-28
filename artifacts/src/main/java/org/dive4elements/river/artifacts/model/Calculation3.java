/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Calculation3
extends      Calculation
{
    private static Logger log = LogManager.getLogger(Calculation3.class);

    protected double    km;
    protected int    [] days;
    protected double [] qs;

    public Calculation3() {
    }

    public Calculation3(double km, int [] days, double [] qs) {
        this.km   = km;
        this.days = days;
        this.qs   = qs;
    }

    public CalculationResult calculate(WstValueTable wst) {

        double [] ws = wst.interpolateW(km, qs, new double[qs.length], this);

        if (days == null || days.length == 0) {
            addProblem(km, "cannot.find.ds");
        }

        if (log.isDebugEnabled()) {
            log.debug("Calculate duration curve data:");
            log.debug("    km       : " + km);
            log.debug("    num Days : " + (days != null ? days.length : 0));
            log.debug("    num Qs   : " + (qs != null ? qs.length : 0));
            log.debug("    result Ws: " + (ws != null ? ws.length : 0));
        }

        WQDay wqday = new WQDay(days, ws, qs);

        if (hasProblems()) {
            log.debug("calculation caused "+numProblems()+" problem(s).");
            wqday.removeNaNs();
        }

        return new CalculationResult(wqday, this);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
