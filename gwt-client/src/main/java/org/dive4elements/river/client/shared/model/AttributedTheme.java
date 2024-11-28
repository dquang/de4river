/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AttributedTheme implements Theme {

    protected Map<String, String> attributes;

    /** CollectionItem associated with this facet/themes artifact. */
    protected CollectionItem collectionItem;

    public AttributedTheme() {
        this.attributes = new HashMap<String, String>();
    }


    public Set<String> getKeys() {
        return attributes.keySet();
    }


    public void addAttr(String name, String value) {
        if (name != null && value != null) {
            attributes.put(name, value);
        }
    }


    public String getAttr(String name) {
        return attributes.get(name);
    }


    public Integer getAttrAsInt(String name) {
        String attr = getAttr(name);

        if (attr != null && attr.length() > 0) {
            try {
                return Integer.parseInt(attr);
            }
            catch (NumberFormatException nfe) {
            }
        }

        return null;
    }


    public boolean getAttrAsBoolean(String name) {
        String attr = getAttr(name);

        if (attr != null) {
            try {
                int num = Integer.valueOf(attr);
                return num > 0;
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        return Boolean.valueOf(attr);
    }


    @Override
    public int getPosition() {
        Integer pos = getAttrAsInt("pos");

        return pos != null ? pos.intValue() : -1;
    }


    @Override
    public void setPosition(int pos) {
        addAttr("pos", String.valueOf(pos));
    }


    @Override
    public int getIndex() {
        Integer idx = getAttrAsInt("index");

        return idx != null ? idx.intValue() : -1;
    }


    @Override
    public int getActive() {
        return getAttrAsInt("active");
    }


    @Override
    public void setActive(int active) {
        addAttr("active", String.valueOf(active));
    }


    @Override
    public String getArtifact() {
        return getAttr("artifact");
    }


    @Override
    public String getFacet() {
        return getAttr("facet");
    }


    @Override
    public String getDescription() {
        return getAttr("description");
    }


    @Override
    public void setDescription(String description) {
        if (description != null && description.length() > 0) {
            addAttr("description", description);
        }
    }


    @Override
    public int getVisible() {
        return getAttrAsInt("visible");
    }


    @Override
    public void setVisible(int visible) {
        addAttr("visible", String.valueOf(visible));
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttributedTheme)) {
            return false;
        }

        AttributedTheme other = (AttributedTheme) o;

        if (other.getPosition() != getPosition()) {
            return false;
        }

        if (!other.getArtifact().equals(getArtifact())) {
            return false;
        }

        if (other.getActive() != getActive()) {
            return false;
        }

        if (!other.getFacet().equals(getFacet())) {
            return false;
        }

        if (!other.getDescription().equals(getDescription())) {
            return false;
        }

        if (other.getIndex() != getIndex()) {
            return false;
        }

        if (other.getVisible() != getVisible()) {
            return false;
        }

        return true;
    }


    /** Get the CollectionItem representing the facets artifact. */
    @Override
    public CollectionItem getCollectionItem() {
        return collectionItem;
    }


    /** Set the CollectionItem representing the facets artifact. */
    @Override
    public void setCollectionItem(CollectionItem ci) {
        this.collectionItem = ci;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
