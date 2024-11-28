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

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.river.artifacts.states.WMSBackgroundState;


public class ExternalWMSArtifact extends StaticD4EArtifact {

    public static final String NAME = "external_wms";

    private static final Logger log =
        LogManager.getLogger(ExternalWMSArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.info("ExternalWMSArtifact.setup");

        super.setup(identifier, factory, context, callMeta, data, loadFacets);

        String ids = getDatacageIDValue(data);

        if (ids != null && ids.length() > 0) {
            addStringData("ids", ids);
        }
        else {
            throw new IllegalArgumentException("No attribute 'ids' found!");
        }

        List<Facet> fs = new ArrayList<Facet>();

        WMSBackgroundState s = (WMSBackgroundState) getCurrentState(context);
        s.computeInit(this, hash(), context, callMeta, fs);

        if (!fs.isEmpty()) {
            addFacets(getCurrentStateId(), fs);
        }
    }


    @Override
    protected void initialize(
        Artifact artifact,
        Object   context,
        CallMeta callMeta)
    {
        // do nothing
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new ExternalWMSState(this);

        List<Facet> fs = getFacets(getCurrentStateId());

        DefaultOutput o = new DefaultOutput(
            "floodmap",
            "floodmap",
            "image/png",
            fs,
            "map");

        s.getOutputs().add(o);

        return s;
    }


    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getCurrentState(context));

        return states;
    }


    public static class ExternalWMSState extends WMSBackgroundState {

        protected ExternalWMSArtifact artifact;

        protected String ids;


        public ExternalWMSState(ExternalWMSArtifact artifact) {
            super();
            this.artifact = artifact;
        }

        protected String getIds() {
            if (ids == null || ids.length() == 0) {
                ids = artifact.getDataAsString("ids");
            }

            return ids;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_EXTERNAL_WMS;
        }

        @Override
        protected String getSrid() {
            return "";
        }

        @Override
        protected String getUrl() {
            String   ids   = getIds();
            String[] parts = ids.split(";");

            return parts[0];
        }

        @Override
        protected String getLayer() {
            String   ids   = getIds();
            String[] parts = ids.split(";");

            return parts[1];
        }

        @Override
        protected String getTitle(CallMeta meta) {
            String   ids   = getIds();
            String[] parts = ids.split(";");

            return parts[2];
        }
    } // end of class ExternalWMSState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
