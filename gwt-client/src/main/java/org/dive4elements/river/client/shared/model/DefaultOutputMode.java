/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.ArrayList;
import java.util.List;

import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;


/**
 * The default implementation of an Output.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultOutputMode implements OutputMode {

    /** The name of this mode.*/
    protected String name;

    /** The description of this mode.*/
    protected String description;

    /** The mime-type of this mode.*/
    protected String mimeType;

    /** The type that this output mode represents.*/
    protected String type;

    /** The list of available facets of this export mode.*/
    protected List<Facet> facets;


    /** A convinience constructor.*/
    public DefaultOutputMode() {
        facets = new ArrayList<Facet>();
    }


    /**
     * The default constructor.
     *
     * @param name The name of this mode.
     * @param description The description of this mode.
     * @param mimeType The mime-type of this mode.
     */
    public DefaultOutputMode(String name, String description, String mimeType) {
        this.name        = name;
        this.description = description;
        this.mimeType    = mimeType;
    }


    public DefaultOutputMode(
        String      name,
        String      description,
        String      mimeType,
        String      type)
    {
        this(name, description, mimeType);

        this.type = type;
    }


    public DefaultOutputMode(
        String name,
        String description,
        String mimeType,
        List<Facet> facets)
    {
        this(name, description, mimeType);
        this.type   = "";
        this.facets = facets;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public String getMimeType() {
        return mimeType;
    }


    public String getType() {
        return type;
    }


    /**
     * Adds a new facet to this export.
     *
     * @param facet The new facet.
     */
    public void addFacet(Facet facet) {
        facets.add(facet);
    }


    /**
     * Returns the number of facets supported by this export.
     *
     * @return the number of facets.
     */
    public int getFacetCount() {
        return facets.size();
    }


    /**
     * Returns the facet at a given position.
     *
     * @param idx The position of a facet.
     *
     * @return a facet.
     */
    public Facet getFacet(int idx) {
        if (idx < getFacetCount()) {
            return facets.get(idx);
        }

        return null;
    }


    public Facet getFacet(String name) {
        for (Facet facet: facets) {
            if (name.equals(facet.getName())) {
                return facet;
            }
        }

        return null;
    }


    public List<Facet> getFacets() {
        return facets;
    }


    public OutputTab createOutputTab(String t, Collection c, CollectionView p) {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
