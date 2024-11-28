/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;
import java.util.Map;


/**
 * The default implementation of a CollectionItem (artifact).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultCollectionItem implements CollectionItem {

    /** The identifier that specifies the artifact related to this item. */
    protected String identifier;

    /** The hash that specifies the artifact related to this item. */
    protected String hash;

    /** The list of output modes supported by the artifact of this item. */
    protected List<OutputMode> outputModes;

    /** The map of datanames to data values. */
    protected Map<String, String> data;


    /**
     * An empty constructor.
     */
    public DefaultCollectionItem() {
    }


    /**
     * The default constructor to create a new CollectionItem related to an
     * artifact with output modes.
     *
     * @param identifier The identifier of an artifact.
     * @param outputModes The output modes supported by this item.
     */
    public DefaultCollectionItem(
        String           identifier,
        String           hash,
        List<OutputMode> modes,
        Map<String,String> data
    ) {
        this.identifier  = identifier;
        this.hash        = hash;
        this.outputModes = modes;
        this.data        = data;
    }



    public String identifier() {
        return identifier;
    }


    public String hash() {
        return hash;
    }


    public List<OutputMode> getOutputModes() {
        return outputModes;
    }


    public List<Facet> getFacets(String outputmode) {
        for (OutputMode mode: outputModes) {
            if (outputmode.equals(mode.getName())) {
                // TODO Return facets, but facets are not implemented for
                // OutputModes yet!
            }
        }

        return null;
    }


    /**
     * Returns artifact data.
     * @return key/value data map
     */
    public Map<String, String> getData() {
        return this.data;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
