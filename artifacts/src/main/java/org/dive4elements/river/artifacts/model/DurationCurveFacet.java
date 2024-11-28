/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Data of a duration curve.
 */
public class DurationCurveFacet extends DefaultFacet {

    private static Logger log = LogManager.getLogger(DurationCurveFacet.class);

    /** Blackboard data provider key for durationcurve (wqday) data. */
    public static String BB_DURATIONCURVE = "durationcurve";

    /** Blackboard data provider key for km of durationcurve. */
    public static String BB_DURATIONCURVE_KM = "durationcurve.km";

    public DurationCurveFacet() {
    }

    public DurationCurveFacet(String name, String description) {
        super(0, name, description);
    }


    /**
     * Expose state computation from WINFOArtifact.
     */
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for duration curve data");

        WINFOArtifact winfo = (WINFOArtifact)artifact;

        CalculationResult cr = (CalculationResult)winfo.compute(
            context, ComputeType.ADVANCE, false);

        return cr.getData();
    }


    @Override
    public List getStaticDataProviderKeys(Artifact art) {
        List list = new ArrayList();
        list.add(BB_DURATIONCURVE);
        list.add(BB_DURATIONCURVE_KM);
        return list;
    }


    /**
     * Can provide whatever getData returns and additionally the location.
     * @param key      will respond on BB_DURATIONCURVE +KM
     * @param param    ignored
     * @param context  ignored
     * @return whatever getData delivers or location.
     */
    @Override
    public Object provideBlackboardData(Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        if (key.equals(BB_DURATIONCURVE)) {
            return getData(artifact, context);
        }
        else if (key.equals(BB_DURATIONCURVE_KM)) {
            return ((D4EArtifact)artifact).getDataAsString("ld_locations");
        }
        else {
            return null;
        }
    }


    /** Create a deep copy. */
    @Override
    public Facet deepCopy() {
        DurationCurveFacet copy = new DurationCurveFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
