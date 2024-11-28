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

import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;


/**
 * This interface describes an output mode of an artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface OutputMode extends Serializable {

    /**
     * Retrieves the name of this mode.
     *
     * @return the name of this mode.
     */
    String getName();


    /**
     * Retrieves the description of this mode.
     *
     * @return the description of this mode.
     */
    String getDescription();


    /**
     * Retrieves the mime-type of this mode.
     *
     *
     * @return the mime-type of this mode.
     */
    String getMimeType();


    /**
     * Returns the type of this mode.
     *
     * @return the type of this mode.
     */
    String getType();


    /**
     * Adds a new facet to this mode.
     *
     * @param facet The new facet.
     */
    void addFacet(Facet facet);


    /**
     * Returns the number of facets supported by this mode.
     *
     * @return the number of facets.
     */
    int getFacetCount();


    /**
     * Returns the facet at a given position.
     *
     * @param idx The position of a facet.
     *
     * @return a facet.
     */
    Facet getFacet(int idx);


    /**
     * Returns a facet based on its name.
     *
     * @param name The name of the facet.
     *
     * @return a facet or null if no such facet is available.
     */
    Facet getFacet(String name);


    /**
     * Returns all facets of this mode.
     *
     * @return all facets.
     */
    List<Facet> getFacets();


    /**
     * Returns an OutputTab that is used to render the output mode.
     *
     * @param t The title.
     * @param c The Collection.
     * @param p The parent CollectionView.
     *
     * @return an OutputTab.
     */
    OutputTab createOutputTab(String t, Collection c, CollectionView p);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
