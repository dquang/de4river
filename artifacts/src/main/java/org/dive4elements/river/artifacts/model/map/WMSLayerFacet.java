/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import com.vividsolutions.jts.geom.Envelope;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.utils.GeometryUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class WMSLayerFacet
extends      DefaultFacet
{
    protected ComputeType  type;
    protected List<String> layers;
    protected String       stateId;
    protected String       hash;
    protected String       url;
    protected Envelope     extent;
    protected Envelope     originalExtent;
    protected String       srid;


    private static final Logger log = LogManager.getLogger(WMSLayerFacet.class);

    public WMSLayerFacet() {
    }


    public WMSLayerFacet(int index, String name, String description) {
        this(index, name, description, ComputeType.FEED, null, null);
    }


    public WMSLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash

    ) {
        super(index, name, description);
        this.layers  = new ArrayList<String>();
        this.type    = type;
        this.stateId = stateId;
        this.hash    = hash;
    }


    public WMSLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash,
        String      url
    ) {
        this(index, name, description, type, stateId, hash);
        this.url = url;
    }


    public void addLayer(String name) {
        if (name != null && name.length() > 0) {
            layers.add(name);
        }
    }


    public List<String> getLayers() {
        return layers;
    }


    public void removeLayer(String layer) {
        if (layers != null) {
            layers.remove(layer);
        }
    }


    public void setExtent(Envelope extent) {
        if (extent != null) {
            this.extent = extent;
        }
        else {
            log.debug("setExtent(): extent is null");
        }
    }


    public Envelope getExtent() {
        return extent;
    }


    public void setOriginalExtent(Envelope originalExtent) {
        this.originalExtent = originalExtent;
    }


    public Envelope getOriginalExtent() {
        return originalExtent;
    }


    public void setSrid(String srid) {
        if (srid != null) {
            this.srid = srid;
        }
    }


    public String getSrid() {
        return srid;
    }


    @Override
    public Object getData(Artifact artifact, CallContext context) {
        return null;
    }


    @Override
    public Node toXML(Document doc) {
        ElementCreator ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element facet = ec.create("facet");
        ec.addAttr(facet, "description", description, true);
        ec.addAttr(facet, "index", String.valueOf(index), true);
        ec.addAttr(facet, "name", name, true);
        ec.addAttr(facet, "url", url, true);
        ec.addAttr(facet, "layers", layers.get(0), true);
        ec.addAttr(facet, "srid", srid != null ? srid : "", true);
        ec.addAttr(facet, "extent", originalExtent != null
            ? GeometryUtils.jtsBoundsToOLBounds(originalExtent)
            : "", true);
        ec.addAttr(facet, "queryable", String.valueOf(isQueryable()), true);

        return facet;
    }


    public boolean isQueryable() {
        return false;
    }


    /** Clone facet-bound data. */
    protected void cloneData(WMSLayerFacet copy) {
        copy.type    = type;
        copy.stateId = stateId;
        copy.hash    = hash;

        if (layers != null) {
            copy.layers  = new ArrayList<String>(layers);
        }
        else {
            copy.layers = new ArrayList<String>();
        }

        copy.originalExtent = originalExtent;
        copy.url     = url;
        copy.extent  = extent;
        copy.srid    = srid;
    }

    @Override
    public Facet deepCopy() {
        WMSLayerFacet copy = new WMSLayerFacet();
        copy.set(this);

        cloneData(copy);

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
