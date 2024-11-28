/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.extreme;

import java.io.Serializable;

import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WQKmsResult;

import org.dive4elements.river.utils.KMIndex;

/** Result from an extreme value (extrapolation) calculation. */
public class ExtremeResult
implements   Serializable, WQKmsResult
{
    /** Curves that refer to actual values and a function for extrapolation. */
    protected KMIndex<Curve> curves;

    protected WQKms [] wqkms;

    public ExtremeResult() {
    }

    public ExtremeResult(KMIndex<Curve> curves, WQKms [] wqkms) {
        this.curves = curves;
        this.wqkms = wqkms;
    }

    /**
     * Gets the curves for this instance.
     *
     * @return The curves.
     */
    public KMIndex<Curve> getCurves() {
        return this.curves;
    }

    /**
     * Sets the curves for this instance.
     *
     * @param curves The curves.
     */
    public void setCurves(KMIndex<Curve> curves) {
        this.curves = curves;
    }

    /**
     * Gets the wqkms for this instance.
     *
     * @return The wqkms.
     */
    @Override
    public WQKms[] getWQKms() {
        return this.wqkms;
    }

    /**
     * Gets the wqkms for this instance.
     *
     * @param index The index to get.
     * @return The wqkms.
     */
    public WQKms getWQKms(int index) {
        return this.wqkms[index];
    }

    /**
     * Sets the wqkms for this instance.
     *
     * @param wqkms The wqkms.
     */
    public void setWQKms(WQKms[] wqkms) {
        this.wqkms = wqkms;
    }

    /**
     * Sets the wqkms for this instance.
     *
     * @param index The index to set.
     * @param wqkms The wqkms.
     */
    public void setWQKms(int index, WQKms wqkms) {
        this.wqkms[index] = wqkms;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
