/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import com.vividsolutions.jts.geom.Envelope;

import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.river.artifacts.model.map.WMSDBLayerFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.MapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;


public abstract class WMSDBArtifact extends StaticD4EArtifact {

    private static final Logger log = LogManager.getLogger(WMSDBArtifact.class);

    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("WMSDBArtifact.setup");

        super.setup(identifier, factory, context, callMeta, data, loadFacets);

        String ids = getDatacageIDValue(data);

        if (ids != null && ids.length() > 0) {
            addData("ids", new DefaultStateData("ids", null, null, ids));
        }
        else {
            throw new IllegalArgumentException("No attribute 'ids' found!");
        }

        List<Facet> fs = new ArrayList<Facet>();

        WMSDBState state = (WMSDBState) getCurrentState(context);
        state.computeInit(this, hash(), context, callMeta, fs);

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
    protected State getState(Object context, String stateID) {
        return getCurrentState(context);
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



    public static abstract class WMSDBState extends DefaultState {
        private static final Logger log = LogManager.getLogger(WMSDBState.class);

        protected D4EArtifact artifact;

        protected String name;
        protected int    riverId;


        public WMSDBState() {}

        public WMSDBState(D4EArtifact artifact) {
            this.artifact = artifact;
            this.name     = null;
            this.riverId  = 0;
        }

        @Override
        public Object computeInit(
            D4EArtifact artifact,
            String       hash,
            Object       context,
            CallMeta     meta,
            List<Facet>  facets
        ) {
            log.debug("WMSDBState.computeInit");

            String type = getFacetType();

            WMSDBLayerFacet facet = new WMSDBLayerFacet(
                0,
                type,
                getTitle(meta),
                ComputeType.INIT,
                getID(), hash,
                getUrl());

            facet.addLayer(getLayer());
            facet.setExtent(getExtent());
            facet.setOriginalExtent(getExtent(true));
            facet.setSrid(getSrid());
            facet.setData(getDataString());
            facet.setFilter(getFilter());
            facet.setGeometryType(getGeometryType());
            facet.setConnection(MapUtils.getConnection());
            facet.setConnectionType(MapUtils.getConnectionType());
            facet.setLabelItem(getLabelItem());

            facets.add(facet);

            return null;
        }

        protected String getLabelItem() {
            return null;
        }

        public int getRiverId() {
            if (riverId == 0) {
                String rid = getIdPart(0);

                try {
                    riverId = Integer.parseInt(rid);
                }
                catch (NumberFormatException nfe) {
                    log.error("Cannot parse river id from '" +
                            artifact.getDataAsString("ids") + "'");
                }
            }
            return riverId;
        }

        protected String getLayer() {
            String type = getFacetType();
            String name = type + "-" + artifact.identifier();
            return name;
        }

        /**
         * Returns the name of the WMS layer. This method extracts the name
         * from 'ids' data string. It is expected, that the 'ids' string is
         * seperated by ';' and that the name is placed at index 1.
         *
         * @return the name of the WMS layer.
         */
        public String getName() {
            if (name == null) {
                name = getIdPart(1);
            }

            return name;
        }

        /**
         * Returns a part of the ID string. This method splits the
         * 'ids' data string. It is expected, that the 'ids' string is
         * seperated by ';'.
         *
         * @param number the position of the id data string
         *
         * @return the part of the id string at position number.
         *         Null if number was out of bounds.
         */
        public String getIdPart(int number) {
            String ids = artifact.getDataAsString("ids");

            String parts[] = ids != null ? ids.split(";") : null;

            if (parts != null && parts.length >= number + 1) {
                return parts[number];
            }
            return null;
        }


        /**
         * Returns the name of the layer (returned by getName()) or the layer
         * type if the name is empty. The layer type is created by an i18n
         * string of getFacetType().
         *
         * @param meta A CallMeta used for i18n.
         *
         * @return the name of the layer or its type if name is empty.
         */
        protected String getTitle(CallMeta meta) {
            String name = getName();

            return name != null && name.length() > 0
                ? name
                : Resources.getMsg(
                    meta,
                    getFacetType(),
                    getFacetType());
        }


        @Override
        public void endOfLife(Artifact owner, Object context) {
            log.info("Destroy WMSDBState: " + getID());

            String p = RiverUtils.getXPathString(
                RiverUtils.XPATH_MAPFILES_PATH);
            File dir = new File(p, owner.identifier());

            if (dir != null && dir.exists()) {
                log.debug("Try to delete directory '" + dir + "'");

                FileTools.deleteRecursive(dir);
            }
        }

        /**
         * This method returns the extent of a DB layer
         * in the projection of the database.
         *
         * @return the extent of the DB layer
         *         in the projection of the database.
         */
        protected Envelope getExtent() {
            return getExtent(false);
        }


        protected abstract String getFacetType();

        protected abstract String getUrl();

        protected abstract String getSrid();

        /**
         * Returns the extent of the DB layer. The projection of the
         * extent depends on the <i>reproject</i> parameter.
         * If reproject is set,
         * the extent is reprojected into the original projection which is
         * specified in the configuration. Otherwise, the projection of the
         * database is used.
         *
         * @param reproject True, to reproject the extent into the projection
         * specified in the configuration.
         *
         * @return the extent of the database layer.
         */
        protected abstract Envelope getExtent(boolean reproject);

        protected abstract String getFilter();

        protected abstract String getDataString();

        protected abstract String getGeometryType();
    } // end of WMSDBState
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
