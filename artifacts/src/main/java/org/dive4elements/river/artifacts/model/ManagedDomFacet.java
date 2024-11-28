/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.ArtifactNamespaceContext;


/**
 * Use an Element (DOM) to store the information about a facet.
 * The intent of this facet type is to represent a facet
 * stored in an Collection attribute. Different facets can have different
 * attributes that we need to parse, but the only thing ManagedFacets need
 * to do, is to adjust the attributes "active" and "position". So, those
 * values are set directly on the Element, the other attributes aren't
 * touched.
 */
public class ManagedDomFacet extends ManagedFacet
{

    protected Element facet;

    public ManagedDomFacet(Element facet) {
        super(null, -1, null, null, -1, -1, -1);
        this.facet = facet;
    }


    @Override
    public int getIndex() {
        if (this.index < 0) {
            String index = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "index");

            if (index != null && index.length() > 0) {
                this.index = Integer.parseInt(index);
            }
        }

        return this.index;
    }


    @Override
    public String getName() {
        if (this.name == null || this.name.length() == 0) {
            String name = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "facet");

            this.name = name;
        }

        return this.name;
    }


    @Override
    public String getDescription() {
        if (this.description == null || this.description.length() == 0) {
            String description = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "description");

            this.description = description;
        }

        return this.description;
    }


    @Override
    public int getPosition() {
        if (this.position < 0) {
            String position = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI,
                "pos");

            if (position != null && position.length() > 0) {
                this.position = Integer.parseInt(position);
            }
        }

        return this.position;
    }


    @Override
    public void setPosition(int position) {
        this.position = position;

        // TODO Evaluate whether other set/getAttributes also need
        // to use the NAMESPACE_PREFIX.
        facet.setAttributeNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX + ":" + "pos",
            String.valueOf(position));
    }


    @Override
    public int getActive() {
        if (this.active < 0) {
            String active = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "active");

            if (active != null && active.length() > 0) {
                this.active = Integer.parseInt(active);
            }
        }

        return this.active;
    }


    @Override
    public void setActive(int active) {
        this.active = active;

        facet.setAttributeNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            "art:active",
            String.valueOf(active));
    }


    @Override
    public int getVisible() {
        if (this.visible < 0) {
            String visible = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "visible");

            if (visible != null && visible.length() > 0) {
                this.visible = Integer.parseInt(visible);
            }
        }

        return this.visible;
    }


    @Override
    public void setVisible(int visible) {
        this.visible = visible;

        facet.setAttributeNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            "visible",
            String.valueOf(getVisible()));
    }


    @Override
    public String getArtifact() {
        if (this.uuid == null || this.uuid.length() == 0) {
            String uuid = facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "artifact");

            this.uuid = uuid;
        }

        return this.uuid;
    }

    @Override
    public String getBoundToOut() {
        if (boundToOut == null) {
            String bondageAttr = this.facet.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI,
                "boundToOut");
            if (bondageAttr != null && !bondageAttr.isEmpty()) {
                boundToOut = bondageAttr;
            }
        }
        return boundToOut;
    }


    @Override
    public void setBoundToOut(String value) {
        boundToOut = value;

        facet.setAttributeNS(
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX + ":" + "boundToOut",
            getBoundToOut());
    }

    /**
     * Import into document.
     * @param doc Document to be imported to.
     */
    @Override
    public Node toXML(Document doc) {
        return doc.importNode(facet, true);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
