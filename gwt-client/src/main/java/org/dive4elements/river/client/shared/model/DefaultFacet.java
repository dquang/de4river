/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * The default implementation of a Facet.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultFacet implements Facet {

    /** The name of the facet.*/
    protected String name;

    /** The description of the facet.*/
    protected String description;

    /** The index of the facet.*/
    protected int index;


    /**
     * An empty constructor.
     */
    public DefaultFacet() {
    }


    /**
     * The default constructor to create new DefaultFacets.
     *
     * @param name The name of the facet.
     */
    public DefaultFacet(String name) {
        this.name = name;
    }


    public DefaultFacet(String name, int index, String description) {
        this(name);

        this.index       = index;
        this.description = description;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public int getIndex() {
        return index;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
