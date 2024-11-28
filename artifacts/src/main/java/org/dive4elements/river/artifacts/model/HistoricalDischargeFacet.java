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

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeFacet extends DataFacet {

    private static final Logger log =
        LogManager.getLogger(HistoricalDischargeFacet.class);


    public HistoricalDischargeFacet() {
    }


    public HistoricalDischargeFacet(int index, String name, String desc) {
        super(index, name, desc, ComputeType.ADVANCE, null, null);
    }


    public HistoricalDischargeFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateID,
        String      hash

    ) {
        super(index, name, description, type, hash, stateID);
    }


    @Override
    public Facet deepCopy() {
        HistoricalDischargeFacet copy = new HistoricalDischargeFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }


    @Override
    public Object getData(Artifact artifact, CallContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Get data for historical discharge curves at index: " +
                index + " / stateId: " + stateId);
        }

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult)
            flys.compute(context, hash, stateId, type,  false);

        HistoricalDischargeData data = (HistoricalDischargeData) res.getData();
        WQTimerange[] wqts = (WQTimerange[]) data.getWQTimeranges();

        return wqts[index];
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
