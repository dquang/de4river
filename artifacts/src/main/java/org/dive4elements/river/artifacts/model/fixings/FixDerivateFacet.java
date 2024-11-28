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
public class FixDerivateFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixDerivateFacet.class);


    /** Trivial Constructor. */
    public FixDerivateFacet() {
    }


    /**
     * @param name
     */
    public FixDerivateFacet(String name, String description) {
        super(0, name, description, ComputeType.ADVANCE, null, null);
    }

    public FixDerivateFacet(int index, String name, String description) {
        super(index, name, description, ComputeType.ADVANCE, null, null);
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {

        log.debug("FixDerivateFacet.getData");
        if (!(artifact instanceof D4EArtifact)) {
            log.debug("Not an instance of FixationArtifact.");
            return null;
        }

        D4EArtifact flys = (D4EArtifact)artifact;
        FixAnalysisAccess access = new FixAnalysisAccess(flys);

        CalculationResult res =
            (CalculationResult) flys.compute(context,
                                             ComputeType.ADVANCE,
                                             false);

        FixAnalysisResult result = (FixAnalysisResult) res.getData();
        double currentKm = getCurrentKm(context);

        String function = access.getFunction();
        Function ff = FunctionFactory.getInstance().getFunction(function);
        Function.Derivative fd = ff.getDerivative();

        Parameters params = result.getParameters();
        double maxQ = FixFacetUtils.getMaxQ(params, currentKm);

        String[] paramNames = ff.getParameterNames();

        double[] coeffs = params.interpolate("km", currentKm, paramNames);
        if (coeffs == null) {
            log.warn("getData: coeffs == null");
            return null;
        }

        org.dive4elements.river.artifacts.math.Function mf =
            fd.instantiate(coeffs);

        FixFunction fix = new FixFunction(
            "",
            fd.getDescription(),
            mf,
            maxQ);

        return fix;
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixDerivateFacet deepCopy() {
        FixDerivateFacet copy = new FixDerivateFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
