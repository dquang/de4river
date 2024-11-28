/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.sq;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;
import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;
import org.dive4elements.river.artifacts.model.sq.SQFunction;
import org.dive4elements.river.artifacts.model.sq.StaticSQRelation;


public class StaticSQRelationFacet
extends DefaultFacet
implements Facet
{
    public static final String FUNCTION = "sq-pow";

    private StaticSQRelation relation;

    private static final Logger log =
        LogManager.getLogger(StaticSQRelationFacet.class);

    public StaticSQRelationFacet(
        int ndx,
        String name,
        String description,
        StaticSQRelation relation) {
        super(ndx, name, description);
        this.relation = relation;
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        double qmax = relation.getQmax();
        double[] coeffs = new double[] {relation.getA(), relation.getB()};
        Function func = FunctionFactory.getInstance().getFunction(FUNCTION);
        org.dive4elements.river.artifacts.math.Function function =
            func.instantiate(coeffs);

        /* Figure out a good starting point by checking for calculated
         * SQ Curves and using their starting point */

        // this is ok because we are a DefaultFacet and not a DataFacet
        // and so we are not registred with Mr. Blackboard
        List<DataProvider> providers = context.getDataProvider(name);

        double startingPoint = Double.MAX_VALUE;

        for (DataProvider dp: providers) {
            SQFunction other = (SQFunction) dp.provideData(
                name,
                null,
                context);
            if (other == null) {
                // name is not really unique here but it's our only key
                // should not happen anyhow.
                log.error("Did not get data from: " + name);
                continue;
            }
            startingPoint = Math.min(other.getMinQ(), startingPoint);
        }
        if (startingPoint == Double.MAX_VALUE) {
            startingPoint = 0;
        }

        SQFunction sqf = new SQFunction(function, startingPoint, qmax);
        return sqf;
    }

    @Override
    public Facet deepCopy() {
        StaticSQRelationFacet copy =
            new StaticSQRelationFacet(index, name, description, relation);
        copy.set(this);
        return copy;
    }
}
