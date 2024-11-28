/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Facet to show the curve in a sq relation.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SQCurveFacet extends DataFacet implements FacetTypes {

    private static final Logger log = LogManager.getLogger(SQCurveFacet.class);


    public static final String FUNCTION = "sq-pow";


    private int fractionIdx;


    public SQCurveFacet() {
    }


    public SQCurveFacet(
        int    idx,
        int    fractionIdx,
        String name,
        String description,
        String hash,
        String stateId
    ) {
        super(idx, name, description, ComputeType.ADVANCE, hash, stateId);
        this.fractionIdx = fractionIdx;
        this.metaData.put("X", "chart.sq_relation.xaxis.label");
        this.metaData.put("Y", "chart.sq_relation.yaxis.label");
    }


    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("SQCurveFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            return null;
        }

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(
            context, ComputeType.ADVANCE, false);

        SQResult[]       results = (SQResult[]) res.getData();
        SQFractionResult result  = results[index].getFraction(fractionIdx);

        Parameters params = result.getParameters();

        if (params == null) {
            log.debug("no parameters found");
            return null;
        }

        Function func = FunctionFactory.getInstance().getFunction(FUNCTION);
        String[] paramNames = func.getParameterNames();

        double [] coeffs = params.get(0, paramNames);

        if (log.isDebugEnabled()) {
            for (int i = 0, N = paramNames.length; i < N; i++) {
                log.debug("retrieved parameter " + paramNames[i] +
                          " = " + coeffs[i]);
            }
        }

        org.dive4elements.river.artifacts.math.Function mf =
            func.instantiate(coeffs);

        double [] extent = result.getQExtent();
        return new SQFunction(mf, extent[0], extent[1]);
    }


    @Override
    public Facet deepCopy() {
        SQCurveFacet copy = new SQCurveFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        copy.fractionIdx = fractionIdx;

        return copy;
    }


    @Override
    public List getStaticDataProviderKeys(Artifact art) {
        List list = super.getStaticDataProviderKeys(art);
        list.add(name);
        return list;
    }

    @Override
    public Object provideBlackboardData(Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        log.debug("I should provide date for key: " + key +" name " + name);
        if (key.equals(name)) {
            return getData(artifact, context);
        }
        return super.provideBlackboardData(artifact, key,
                param, context);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
