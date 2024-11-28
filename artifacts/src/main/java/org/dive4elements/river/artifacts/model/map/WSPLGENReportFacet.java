/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * This facet is used to provide WSPLGEN reports <b>only</b>.
 */
public class WSPLGENReportFacet extends ReportFacet {

    private static Logger log = LogManager.getLogger(WSPLGENReportFacet.class);


    protected CalculationResult result;


    public WSPLGENReportFacet() {
    }


    public WSPLGENReportFacet(
        ComputeType       type,
        String            hash,
        String            stateId,
        CalculationResult result
    ) {
        super(type, hash, stateId);
        this.result = result;
    }


    @Override
    public Object getData(Artifact artifact, CallContext context) {
        return result.getReport();
    }


    @Override
    public Facet deepCopy() {
        WSPLGENReportFacet copy = new WSPLGENReportFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        copy.result  = result;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
