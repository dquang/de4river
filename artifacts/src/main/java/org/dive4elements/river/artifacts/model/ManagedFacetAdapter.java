/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


public class ManagedFacetAdapter extends ManagedFacet {

    protected Facet facet;

    public ManagedFacetAdapter() {
    }


    protected Logger log = LogManager.getLogger(ManagedFacetAdapter.class);

    public ManagedFacetAdapter(
        Facet   facet,
        String  uuid,
        int     pos,
        int     active,
        int     visible
    ) {
        super(
            facet.getName(),
            facet.getIndex(),
            facet.getDescription(),
            uuid,
            pos,
            active,
            visible,
            facet.getBoundToOut());

        this.facet = facet;
    }


    @Override
    public Node toXML(Document doc) {
        ElementCreator ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element e = (Element) facet.toXML(doc);
        ec.addAttr(e, "artifact", getArtifact(), true);
        ec.addAttr(e, "facet", getName(), true);
        ec.addAttr(e, "pos", String.valueOf(getPosition()), true);
        ec.addAttr(e, "active", String.valueOf(getActive()), true);
        ec.addAttr(e, "visible", String.valueOf(getVisible()), true);

        return e;
    }

    @Override
    public Facet deepCopy() {
        ManagedFacetAdapter copy = new ManagedFacetAdapter();
        copy.set((DefaultFacet)this);
        copy.set((ManagedFacet)this);
        copy.facet = facet.deepCopy();
        return facet;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
