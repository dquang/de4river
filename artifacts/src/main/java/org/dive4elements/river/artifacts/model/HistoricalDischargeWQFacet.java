/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.dive4elements.river.exports.injector.InjectorConstants.PNP;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeWQFacet
extends DataFacet
implements FacetTypes {
    private static final Logger log = LogManager
        .getLogger(HistoricalDischargeWQFacet.class);

    private double value;

    public HistoricalDischargeWQFacet() {
    }

    public HistoricalDischargeWQFacet(int index, String name,
        String description, ComputeType type, String hash, String stateId,
        double value) {

        super(index, name, description, type, hash, stateId);
        this.value = value;
    }

    @Override
    public Facet deepCopy() {
        HistoricalDischargeWQFacet copy = new HistoricalDischargeWQFacet();
        copy.set(this);
        copy.value = value;
        return copy;
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        double v = this.value;
        if (HISTORICAL_DISCHARGE_WQ_W.equals(name)) {
            if (context.getContextValue(PNP) instanceof Number) {
                v = value/100 +
                    ((Number)context.getContextValue(PNP)).doubleValue();
            }
            else {
                log.error("Missing datum. Cannot calculate W value.");
                return null;
            }
        }
        return v;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
