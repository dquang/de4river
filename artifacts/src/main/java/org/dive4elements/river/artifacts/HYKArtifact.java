/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;


import org.dive4elements.river.artifacts.states.DefaultState;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;


/**
 * Artifact to get hydr zones (HYKs).
 */
public class HYKArtifact extends StaticD4EArtifact {

    /** Name of Artifact. */
    public static final String HYK_ARTIFACT_NAME = "hyk";

    /** Name of data item keeping the hyk id to load formations from. */
    public static final String HYK_ID = "hyk_artifact.data.id";

    /** Name of data item keeping the km of cs master. */
    public static final String HYK_KM = "hyk_artifact.data.km";

    /** Own log. */
    private static final Logger log =
        LogManager.getLogger(HYKArtifact.class);

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(HYK_ARTIFACT_NAME, FacetActivity.INACTIVE);
    }

    /** Return given name. */
    @Override
    public String getName() {
        return HYK_ARTIFACT_NAME;
    }


    /** Store ids, do super.setup. */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.info("HYKArtifact.setup");

        String ids = getDatacageIDValue(data);

        log.info("HYKArtifact.setup: id is " + ids);

        addStringData(HYK_ID, ids);

        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /** Set km as Data. */
    public void setKm(double km) {
        addStringData(HYK_KM, Double.toString(km));
    }


    /** Get km from state data. */
    public double getKm() {
        Double km = getDataAsDouble(HYK_KM);
        if (km == null) {
            // XXX returning 0 is to be compatible to older versions that had an
            // own method getDataAsDouble that returned 0 if parsing the
            // parameter failed.
            return 0;
        }
        else {
            return km;
        }
    }


    /** Get hyk-id from state data. */
    public int getHykId() {
        return getDataAsInteger(HYK_ID);
    }


    /** Do not copy data from daddyfact. */
    @Override
    protected void initialize(
        Artifact artifact,
        Object   context,
        CallMeta callMeta)
    {
        log.debug("HYKArtifact.initialize");
        importData((D4EArtifact)artifact, "river");

        List<Facet> fs = new ArrayList<Facet>();

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, callMeta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in HYKArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in HYKArtifact.initialize ("
                + state.getID() + ").");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
