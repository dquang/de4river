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

import org.dive4elements.river.model.HWSLine;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSHWSLinesArtifact extends WMSDBArtifact {

    public static final String NAME = "hws_lines";


    private static final Logger log =
        LogManager.getLogger(WMSHWSLinesArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new HWSLinesState(this);

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


    public static class HWSLinesState extends WMSDBState implements FacetTypes
    {
        private static final Logger log =
            LogManager.getLogger(HWSLinesState.class);

        protected int riverId;

        public HWSLinesState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        public int getRiverId() {
            if (riverId == 0) {
                String   ids   = artifact.getDataAsString("ids");
                String[] parts = ids.split(";");

                try {
                    riverId = Integer.parseInt(parts[0]);
                }
                catch (NumberFormatException nfe) {
                    log.error("Cannot parse river id from '" + parts[0] + "'");
                }
            }

            return riverId;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_HWS_LINES;
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
            List<HWSLine> hws = HWSLine.getLines(getRiverId(), getName());

            Envelope max = null;

            for (HWSLine h: hws) {
                Envelope env = h.getGeom().getEnvelopeInternal();

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
            return "river_id=" + String.valueOf(getRiverId())
                + " AND name='" + getName() + "'";
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM hws_lines USING SRID " + srid;
            }
            else {
                return "geom FROM hws_lines USING UNIQUE id USING SRID " + srid;
            }
        }

        @Override
        protected String getGeometryType() {
            return "LINE";
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
