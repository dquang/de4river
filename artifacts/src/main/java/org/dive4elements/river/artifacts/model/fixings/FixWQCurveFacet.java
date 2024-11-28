/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.FixAnalysisAccess;

import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet to show the W|Q values.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixWQCurveFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixWQCurveFacet.class);


    /** Trivial Constructor. */
    public FixWQCurveFacet() {
    }


    /**
     * @param description Description of the facet.
     */
    public FixWQCurveFacet(String description) {
        super(0, FIX_WQ_CURVE, description, ComputeType.ADVANCE, null, null);
    }

    public FixWQCurveFacet(int index, String description) {
        super(index, FIX_WQ_CURVE, description,
            ComputeType.ADVANCE, null, null);
    }


    /**
     * Returns the data this facet provides at given km, a function.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext.
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {

        log.debug("getData");
        if (!(artifact instanceof D4EArtifact)) {
            log.debug("Not an instance of D4EArtifact / FixationArtifact.");
            return null;
        }

        D4EArtifact flys = (D4EArtifact)artifact;
        FixAnalysisAccess access = new FixAnalysisAccess(flys);

        CalculationResult res =
            (CalculationResult) flys.compute(context,
                                             ComputeType.ADVANCE,
                                             false);

        FixResult result = (FixResult) res.getData();
        double currentKm = getCurrentKm(context);

        log.debug("getData: km = " + currentKm);

        String function = access.getFunction();
        Function ff = FunctionFactory.getInstance().getFunction(function);

        if (ff == null) {
            log.warn("getData: ff == null");
            return null;
        }

        Parameters params = result.getParameters();
        String[] paramNames = ff.getParameterNames();

        double [] coeffs = params.interpolateWithLimit(
            "km", currentKm, paramNames, access.getStep() / 1000 + 1E-3);

        if (coeffs == null) {
            log.warn("getData: coeffs not in interpolation limits");
            return null;
        }

        org.dive4elements.river.artifacts.math.Function mf =
            ff.instantiate(coeffs);

        double maxQ = FixFacetUtils.getMaxQ(params, currentKm);
        log.debug("getData: maxQ = " + maxQ);

        FixFunction fix = new FixFunction(
            ff.getName(),
            ff.getDescription(),
            mf,
            maxQ);

        return fix;
    }

    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixWQCurveFacet deepCopy() {
        FixWQCurveFacet copy = new FixWQCurveFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
