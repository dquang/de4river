/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.vividsolutions.jts.geom.Envelope;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Jetty;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSJettiesArtifact extends WMSDBArtifact {

    public static final String NAME = "jetties";


    private static final Logger log =
        LogManager.getLogger(WMSJettiesArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new JettiesState(this);

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


    public static class JettiesState extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(JettiesState.class);

        protected int riverId;

        public JettiesState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_JETTIES;
        }

        @Override
        protected String getUrl() {
            return RiverUtils.getUserWMSUrl();
        }

        @Override
        protected String getSrid() {
            River river = RiverFactory.getRiver(getRiverId());
            return RiverUtils.getRiverSrid(river.getName());
        }

        @Override
        protected Envelope getExtent(boolean reproject) {
            List<Jetty> jetties;
            String kind = getIdPart(2);

            jetties = Jetty.getJetties(getRiverId(), Integer.parseInt(kind));

            Envelope max = null;

            for (Jetty b: jetties) {
                Envelope env = b.getGeom().getEnvelopeInternal();

                if (max == null) {
                    max = env;
                    continue;
                }

                max.expandToInclude(env);
            }

            return max != null && reproject
                ? GeometryUtils.transform(max, getSrid())
                : max;
        }

        @Override
        protected String getFilter() {
            // The expected id string is:
            // river_id;layer-name;kind

            String kind = getIdPart(2);
            return "river_id=" + String.valueOf(getRiverId()) +
                " AND kind_id=" + kind;
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM jetties USING SRID " + srid;
            }
            else {
                return "geom FROM jetties USING UNIQUE id USING SRID " + srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "POINT";
        }
    } // end of WMSKmState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
