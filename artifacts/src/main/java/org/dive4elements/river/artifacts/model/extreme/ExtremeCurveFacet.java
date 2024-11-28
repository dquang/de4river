/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.extreme;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.ExtremeAccess;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.fixings.FixingsFacet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.utils.KMIndex;
import org.dive4elements.river.utils.DoubleUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet to show the W|Q values.
 */
public class ExtremeCurveFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(ExtremeCurveFacet.class);


    /** Trivial Constructor. */
    public ExtremeCurveFacet() {
    }


    /**
     * @param description Description of the facet.
     */
    public ExtremeCurveFacet(String description) {
        super(0, EXTREME_WQ_CURVE, description,
            ComputeType.ADVANCE, null, null);
    }

    /**
     * @param description Description of the facet.
     * @param showBase if true, gimme different name.
     */
    public ExtremeCurveFacet(String description, boolean showBase) {
        super(0, EXTREME_WQ_CURVE_BASE, description,
            ComputeType.ADVANCE, null, null);
        if (!showBase) {
            this.name = EXTREME_WQ_CURVE;
        }
    }


    public ExtremeCurveFacet(int index, String description) {
        super(index, EXTREME_WQ_CURVE, description,
            ComputeType.ADVANCE, null, null);
    }


    /**
     * Returns the data (curve) this facet provides at km given in context.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext.
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("ExtremeCurveFacet.getData");
        if (artifact instanceof D4EArtifact) {
            D4EArtifact flys = (D4EArtifact)artifact;
            CalculationResult res =
                (CalculationResult) flys.compute(context,
                                                 ComputeType.ADVANCE,
                                                 false);

            ExtremeResult result = (ExtremeResult) res.getData();
            double currentKm = getCurrentKm(context);

            KMIndex<Curve> curves = result.getCurves();

            KMIndex.Entry<Curve> curveEntry = curves.search(currentKm);

            if (curveEntry != null) {
                log.debug("A curve at km = " + currentKm);
                Curve c = curveEntry.getValue();
                // Find segment in which the curr. km is located.
                ExtremeAccess access = new ExtremeAccess(flys);

                double[] ds = access.getValuesForRange(currentKm);

                if (ds != null) {
                    double m = DoubleUtil.maxInArray(ds);
                    // Add 5 percent.
                    m *= 1.05d;
                    c.setSuggestedMaxQ(m);
                }

                return c;
            }
            else {
                log.debug("No curve at km = " + currentKm);
                return null;
            }
        }
        else {
            log.debug("Not an instance of D4EArtifact / WINFOArtifact.");
            return null;
        }
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public ExtremeCurveFacet deepCopy() {
        ExtremeCurveFacet copy = new ExtremeCurveFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
