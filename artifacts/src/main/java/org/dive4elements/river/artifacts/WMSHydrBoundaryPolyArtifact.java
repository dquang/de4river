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
import org.dive4elements.river.model.HydrBoundaryPoly;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSHydrBoundaryPolyArtifact extends WMSDBArtifact {

    public static final String NAME = "hydr_boundary_poly";


    private static final Logger log =
        LogManager.getLogger(WMSHydrBoundaryPolyArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new HydrBoundaryPolyState(this);

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


    public static class HydrBoundaryPolyState
        extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(HydrBoundaryPolyState.class);

        protected int riverId;

        public HydrBoundaryPolyState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_HYDR_BOUNDARY_POLY;
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
            String kind = getIdPart(2);
            String sectie = getIdPart(3);
            String sobek = getIdPart(4);
            int kindId = -1;
            int sectieId = -1;
            int sobekId = -1;

            if (kind != null) {
                kindId = Integer.parseInt(kind);
            }
            if (sectie != null && !sectie.equals("-1")) {
                sectieId = Integer.parseInt(sectie);
            }
            if (sobek != null && !sobek.equals("-1")) {
                sobekId = Integer.parseInt(sobek);
            }

            List<HydrBoundaryPoly> boundaries;
            if (kindId == -1 && sobekId == -1 && sectieId == -1) {
                boundaries = HydrBoundaryPoly.getHydrBoundaries(
                        getRiverId(), getName());
            } else {
                boundaries = HydrBoundaryPoly.getHydrBoundaries(
                        getRiverId(), kindId, sectieId, sobekId);
            }

            Envelope max = null;

            for (HydrBoundaryPoly b: boundaries) {
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
            // Expected id string:
            // river_id;layer_name;kind;sectie;sobek
            String kind = getIdPart(2);
            String sectie = getIdPart(3);
            String sobek = getIdPart(4);

            String filter = "";
            if (kind != null && !kind.equals("-1")) {
                filter += " AND kind = " + kind;
            }
            if (sectie != null && !sectie.equals("-1")) {
                filter += " AND sectie = " + sectie;
            }
            if (sobek != null && !sobek.equals("-1")) {
                filter += " AND sobek = " + sobek;
            }

            if (filter.isEmpty()) {
                filter = " AND name='" + getName() + "'";
            }

            return "river_id=" + String.valueOf(getRiverId())
                + filter;
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM hydr_boundaries_poly USING SRID " + srid;
            }
            else {
                return "geom FROM hydr_boundaries_poly "
                    + "USING UNIQUE id USING SRID "
                    + srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "POLYGON";
        }
    } // end of HydrBoundaryState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
