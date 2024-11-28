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

import org.dive4elements.river.model.Floodplain;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSFloodplainArtifact extends WMSDBArtifact {

    public static final String NAME = "floodplain";


    private static final Logger log =
        LogManager.getLogger(WMSFloodplainArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new FloodplainState(this);

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


    public static class FloodplainState extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(FloodplainState.class);

        protected int riverId;

        public FloodplainState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        protected River getRiver() {
            return RiverFactory.getRiver(getRiverId());
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_FLOODPLAIN;
        }

        @Override
        protected String getUrl() {
            return RiverUtils.getUserWMSUrl();
        }

        @Override
        protected String getSrid() {
            River river = getRiver();
            return RiverUtils.getRiverSrid(river.getName());
        }

        @Override
        protected Envelope getExtent(boolean reproject) {
            River river = getRiver();
            List<Floodplain> fps;

            String kind = getIdPart(2);

            if (kind != null && ! kind.equals("1")) {
                fps = Floodplain.getFloodplains(river.getName(),
                        getName(), Integer.parseInt(kind));
            } else {
                fps = Floodplain.getFloodplains(river.getName(), 1);
            }

            Envelope max = null;

            for (Floodplain fp: fps) {
                Envelope env = fp.getGeom().getEnvelopeInternal();

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
            String kind = getIdPart(2);
            if (kind != null && ! kind.equals("1")) {
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=" + kind +
                    " AND name='" + getName() + "'";
            }
            return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=1";
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM floodplain USING SRID " + srid;
            }
            else {
                return "geom FROM floodplain USING UNIQUE id USING SRID " +srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "POLYGON";
        }
    } // end of WMSKmState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
