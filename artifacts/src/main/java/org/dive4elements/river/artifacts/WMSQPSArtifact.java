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
import org.dive4elements.river.model.CrossSectionTrack;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSQPSArtifact extends WMSDBArtifact {

    public static final String NAME = "qps";


    private static final Logger log =
        LogManager.getLogger(WMSQPSArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new WMSQPSState(this);

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


    public static class WMSQPSState extends WMSDBState implements FacetTypes {

        private static final Logger log =
            LogManager.getLogger(WMSQPSState.class);

        public WMSQPSState(WMSDBArtifact artifact) {
            super(artifact);
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_QPS;
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
            River river = RiverFactory.getRiver(getRiverId());
            List<CrossSectionTrack> qps;

            String kind = getIdPart(2);

            if (kind != null && kind.equals("1")) {
                qps = CrossSectionTrack.getCrossSectionTrack(river.getName(),
                        Integer.parseInt(kind));
            } else if (kind != null) {
                qps = CrossSectionTrack.getCrossSectionTrack(river.getName(),
                        getName(), Integer.parseInt(kind));
            } else {
                qps = CrossSectionTrack.getCrossSectionTrack(river.getName(),
                        getName());
            }

            Envelope max = null;

            for (CrossSectionTrack qp: qps) {
                Envelope env = qp.getGeom().getEnvelopeInternal();

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
            if (kind != null && kind.equals("1")) {
                // There can be several layers named qps that differ in kind
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=" + kind;
            } else if (kind != null) {
                return "river_id=" + String.valueOf(getRiverId()) +
                    " AND kind_id=" + kind +
                    " AND name='" + getName() + "'";
            }
            return "river_id=" + String.valueOf(getRiverId()) +
                " AND name='" + getName() + "'";
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM cross_section_tracks USING SRID " + srid;
            }
            else {
                return "geom FROM cross_section_tracks " +
                       "USING UNIQUE id USING SRID " + srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "LINE";
        }
    } // end of WMSQPSState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
