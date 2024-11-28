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
import java.util.Map;


/**
 * The CollectionItem interface that provides methods to get information about
 * artifacts and its output modes.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface CollectionItem extends Serializable {

    /**
     * Returns the identifier of the wrapped artifact.
     *
     * @return the identifier of the wrapped artifact.
     */
    String identifier();


    /**
     * Returns the hash of the wrapped artifact.
     *
     * @return the hash of the wrapped artifact.
     */
    String hash();


    /**
     * Returns the output modes of the wrapped artifact.
     *
     * @return the output modes of the wrapped artifact.
     */
    List<OutputMode> getOutputModes();


    /**
     * Returns the facets of the wrapped artifact for a specific output mode.
     *
     * @param outputmode The name of an output mode that is supported by this
     * item.
     *
     * @return the facets of the wrapped artifact for a specific output mode.
     */
    List<Facet> getFacets(String outputmode);


    /**
     * Returns data key/value map.
     * @return key/value data map
     */
    Map<String, String> getData();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
