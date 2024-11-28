/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.RiverAxis;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Deprecated
public class RiverAxisArtifact extends WMSDBArtifact {

    public static final String NAME = "riveraxis";


    private static final Logger log =
        LogManager.getLogger(RiverAxisArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new RiverAxisState(this);

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


    public static class RiverAxisState extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(RiverAxisState.class);

        protected Geometry geom;
        protected int      riverId;

        public RiverAxisState(){}

        public RiverAxisState(D4EArtifact artifact) {
            super(artifact);
        }

        private boolean isUnofficial() {
            return getIdPart(2) != null && !getIdPart(2).equals("1");
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_RIVERAXIS;
        }

        @Override
        protected String getLayer() {
            if (isUnofficial()) {
                return super.getLayer();
            }
            return RiverFactory.getRiver(getRiverId()).getName();
        }

        @Override
        protected String getUrl() {
            if (isUnofficial()) {
                return RiverUtils.getUserWMSUrl();
            } else {
                return RiverUtils.getRiverWMSUrl();
            }
        }

        @Override
        protected String getSrid() {
            River river = RiverFactory.getRiver(getRiverId());
            return RiverUtils.getRiverSrid(river.getName());
        }

        @Override
        protected Envelope getExtent(boolean reproject) {
            River river = RiverFactory.getRiver(getRiverId());
            List<RiverAxis> axes;

            String kind = getIdPart(2);

            if (kind != null && !kind.equals(RiverAxis.KIND_CURRENT)) {
                axes = RiverAxis.getRiverAxis(river.getName(),
                        getName(), Integer.parseInt(kind));
            } else {
                if (reproject) {
                    log.debug("Query extent for RiverAxis with Srid: "
                        + getSrid());
                    return GeometryUtils.transform(
                            GeometryUtils.getRiverBoundary(river.getName()),
                            getSrid());
                }
                return GeometryUtils.getRiverBoundary(river.getName());
            }

            Envelope max = null;

            for (RiverAxis ax: axes) {
                Envelope env = ax.getGeom().getEnvelopeInternal();

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
            if (kind != null && kind.equals(RiverAxis.KIND_CURRENT)) {
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=" + kind;
            }
            if (kind != null) {
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=" + kind +
                    " AND name='" + getName() + "'";
            }
            if (getIdPart(1) != null) {
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND name='" + getName() + "'";
            }
            return "river_id=" + String.valueOf(getRiverId()) +
                " AND kind_id=" + kind;
        }

        @Override
        protected String getDataString() {
            if (RiverUtils.isUsingOracle()) {
                return "geom FROM river_axes USING SRID " + getSrid();
            }
            return "geom FROM river_axes USING UNIQUE id";
        }

        @Override
        protected String getGeometryType() {
            return "LINE";
        }
    } // end of WMSKmState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
