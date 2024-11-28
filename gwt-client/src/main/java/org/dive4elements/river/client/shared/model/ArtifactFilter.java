/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ArtifactFilter
implements   Serializable
{
    protected String factoryName;

    protected Map<String, OutFilter> outFilters;

    public ArtifactFilter() {
        outFilters = new HashMap<String, OutFilter>();
    }

    public ArtifactFilter(String factoryName) {
        this();
        this.factoryName = factoryName;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public void add(String out, String name, String num) {
        if (out == null) {
            out = ToLoad.uniqueKey(outFilters);
        }

        OutFilter outFilter = outFilters.get(out);

        if (outFilter == null) {
            outFilter = new OutFilter(out);
            outFilters.put(out, outFilter);
        }
        outFilter.add(name, num);
    }

    public String collectIds() {
        StringBuilder sb = new StringBuilder();
        for (OutFilter outFilter: outFilters.values()) {
            outFilter.collectIds(sb);
        }
        return sb.toString();
    }

    public Recommendation.Filter toFilter() {
        Recommendation.Filter rf = new Recommendation.Filter();
        for (Map.Entry<String, OutFilter> entry: outFilters.entrySet()) {
            List<Recommendation.Facet> facets = entry.getValue().toFacets();
            rf.add(entry.getKey(), facets);
        }
        return rf;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
