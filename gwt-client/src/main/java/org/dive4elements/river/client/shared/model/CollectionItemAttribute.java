/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class CollectionItemAttribute implements Serializable {

    /** The list of styles. */
    protected List<Style> styles;

    /** The artifact. */
    protected String artifact;


    /**
     * Creates a new CollectionItem Attribute.
     */
    public CollectionItemAttribute() {
        styles = new ArrayList<Style>();
    }


    /**
     * Append a new Style.
     * @param style The style.
     */
    public void appendStyle (Style style) {
        this.styles.add(style);
    }


    /**
     * Remove a style from the attributes.
     * @param name The style name.
     */
    public void removeStyle (String name) {
        for (int i = 0; i < styles.size(); i++) {
            if (styles.get(i).getName().equals(name)) {
                styles.remove(i);
            }
        }
    }


    /**
     * Get a style from the collection item.
     * @param facet The facet this style belongs to.
     * @param index The style index.
     *
     * @return The selected style or 'null'.
     */
    public Style getStyle(String facet, int index) {
        for (int i = 0; i < styles.size(); i++) {
            Style tmp = styles.get(i);
            if (tmp.getFacet().equals(facet) &&
                tmp.getIndex() == index) {
                return tmp;
            }
        }
        return null;
    }


    /**
     * Get the style at a postion.
     * @param i The position index.
     *
     * @return The selected style.
     */
    public Style getStyle(int i) {
        return styles.get(i);
    }


    /**
     * Get the number of styles.
     * @return The number of styles.
     */
    public int getNumStyles() {
        return styles.size();
    }


    /**
     * Set the current artifact.
     * @param The artifact uuid.
     */
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }


    /**
     * Get the associated artifact.
     * @return The artifact.
     */
    public String getArtifact () {
        return this.artifact;
    }
}
