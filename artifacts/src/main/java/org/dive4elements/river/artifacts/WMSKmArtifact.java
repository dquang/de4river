/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.List;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.RiverAxisKm;

import org.dive4elements.river.artifacts.WMSDBArtifact.WMSDBState;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;


public class WMSKmArtifact extends WMSDBArtifact {

    public static final String NAME = "wmskm";


    private static final Logger log = LogManager.getLogger(WMSKmArtifact.class);


    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("WMSKmArtifact.setup");

        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public State getCurrentState(Object cc) {
        State s = new WMSKmState(this);

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



    public static class WMSKmState extends WMSDBState implements FacetTypes {

        private static final Logger log = LogManager.getLogger(WMSKmState.class);

        protected Geometry geom;
        protected int      riverId;

        public WMSKmState(WMSDBArtifact artifact) {
            super(artifact);
            riverId = 0;
        }

        public int getRiverId() {
            if (riverId == 0) {
                String ids = artifact.getDataAsString("ids");

                try {
                    riverId = Integer.parseInt(ids);
                }
                catch (NumberFormatException nfe) {
                    log.error("Cannot parse river id from '" + ids + "'");
                }
            }

            return riverId;
        }

        @Override
        protected String getFacetType() {
            return FLOODMAP_KMS;
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
            List<RiverAxisKm> kms = RiverAxisKm.getRiverAxisKms(getRiverId());

            Envelope max = null;

            for (RiverAxisKm km: kms) {
                Envelope env = km.getGeom().getEnvelopeInternal();

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
            return "river_id=" + String.valueOf(getRiverId());
        }

        @Override
        protected String getDataString() {
            String srid = getSrid();

            if (RiverUtils.isUsingOracle()) {
                return "geom FROM river_axes_km USING SRID " + srid;
            }
            else {
                return "geom FROM river_axes_km " +
                       "USING UNIQUE id USING SRID " + srid;
            }
        }

        @Override
        protected String getLabelItem() {
            return "km";
        }

        @Override
        protected String getGeometryType() {
            return "POINT";
        }
    } // end of WMSKmState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
