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

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.utils.CompareUtil;

/**
 * Facet with user-supplied theme-control-information (pos in list,
 * active/disabled etc) attached.
 */
public class ManagedFacet extends DefaultFacet implements Comparable {

    /** The uuid of the owner artifact. */
    protected String uuid;

    /** A property that determines the position of this facet. */
    protected int position;

    /** A property that determines if this facet is active or not. */
    protected int active;

    /** A property that determines if this facet is visible or not. */
    protected int visible;

    public ManagedFacet() {
    }

    public ManagedFacet(String name, int index, String desc, String uuid,
        int pos, int active, int visible) {
        this(name, index, desc, uuid, pos, active, visible, null);
    }

    public ManagedFacet(String name, int index, String desc, String uuid,
        int pos, int active, int visible, String boundToOut) {
        super(index, name, desc);

        this.uuid = uuid;
        this.position = pos;
        this.active = active;
        this.visible = visible;
        this.boundToOut = boundToOut;
    }

    /**
     * Sets position (will be merged to position in ThemeList).
     */
    public void setPosition(int pos) {
        this.position = pos;
    }

    public int getPosition() {
        return position;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getActive() {
        return active;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public int getVisible() {
        return visible;
    }

    /**
     * Get uuid of related artifact.
     *
     * @return uuid of related artifact.
     */
    public String getArtifact() {
        return uuid;
    }

    public Node toXML(Document doc) {
        ElementCreator ec = new ElementCreator(doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element facet = ec.create("theme");
        ec.addAttr(facet, "artifact", getArtifact(), true);
        ec.addAttr(facet, "facet", getName(), true);
        ec.addAttr(facet, "pos", String.valueOf(getPosition()), true);
        ec.addAttr(facet, "active", String.valueOf(getActive()), true);
        ec.addAttr(facet, "index", String.valueOf(getIndex()), true);
        ec.addAttr(facet, "description", getDescription(), true);
        ec.addAttr(facet, "visible", String.valueOf(getVisible()), true);

        return facet;
    }

    public void set(ManagedFacet other) {
        uuid = other.uuid;
        position = other.position;
        active = other.active;
    }

    @Override
    public Facet deepCopy() {
        ManagedFacet copy = new ManagedFacet();
        copy.set((DefaultFacet) this);
        copy.set((ManagedFacet) this);
        return copy;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof ManagedFacet)) {
            return -1;
        }

        ManagedFacet other = (ManagedFacet) o;

        if (position < other.position) {
            return -1;
        }
        else if (position > other.position) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns true if the other is likely the same facet.
     * This happens if a facet is defined for two outs.
     */
    public boolean isSame(Object other) {
        if (!(other instanceof ManagedFacet)) {
            return false;
        }
        ManagedFacet otherFacet = (ManagedFacet) other;
        return  this.getVisible() == otherFacet.getVisible() &&
                this.getActive() == otherFacet.getActive() &&
                CompareUtil.areSame(
                    this.getArtifact(), otherFacet.getArtifact()) &&
                this.getIndex() == otherFacet.getIndex() &&
                CompareUtil.areSame(this.getName(), otherFacet.getName()) &&
                CompareUtil.areSame(
                    this.getBoundToOut(), otherFacet.getBoundToOut()) &&
                CompareUtil.areSame(
                    this.getDescription(), otherFacet.getDescription());
        // Missing properties are blackboard, data, position.
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
