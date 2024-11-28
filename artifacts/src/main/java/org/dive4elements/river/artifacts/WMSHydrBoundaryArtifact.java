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
import org.dive4elements.river.model.HydrBoundary;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSHydrBoundaryArtifact extends WMSDBArtifact {

    public static final String NAME = "hydr_boundary";


    private static final Logger log =
        LogManager.getLogger(WMSHydrBoundaryArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new HydrBoundaryState(this);

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


    public static class HydrBoundaryState
        extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(HydrBoundaryState.class);

        protected int riverId;

        public HydrBoundaryState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_HYDR_BOUNDARY;
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
            List<HydrBoundary> boundaries;
            String kind = getIdPart(2);
            if (kind != null) {
                boundaries = HydrBoundary.getHydrBoundaries(getRiverId(),
                        getName(), Integer.parseInt(kind));
            } else {
                boundaries = HydrBoundary.getHydrBoundaries(
                    getRiverId(), getName());
            }

            Envelope max = null;

            for (HydrBoundary b: boundaries) {
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
            String kind = getIdPart(2);
            if (kind != null) {
                return "river_id=" + String.valueOf(getRiverId())
                    + " AND name='" + getName() + "'"
                    + " AND kind = " + kind;
            }
            return "river_id=" + String.valueOf(getRiverId())
                + " AND name='" + getName() + "'";
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM hydr_boundaries USING SRID " + srid;
            }
            else {
                return "geom FROM hydr_boundaries USING UNIQUE id USING SRID "
                    + srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "LINE";
        }
    } // end of HydrBoundaryState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
