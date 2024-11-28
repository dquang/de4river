/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class OutFilter
implements   Serializable
{
    protected String out;

    protected List<FacetFilter> facetFilters;

    public OutFilter() {
        facetFilters = new ArrayList<FacetFilter>();
    }

    public OutFilter(String out) {
        this();
        this.out = out;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public void add(String name, String num) {
        FacetFilter facetFilter = new FacetFilter(name, num);
        if (!facetFilters.contains(facetFilter)) {
            facetFilters.add(facetFilter);
        }
    }

    public List<FacetFilter> getFacetFilters() {
        return facetFilters;
    }

    public void setFacetFilters(List<FacetFilter> facetFilters) {
        this.facetFilters = facetFilters;
    }

    public void collectIds(StringBuilder sb) {
        for (FacetFilter facetFilter: facetFilters) {
            facetFilter.collectIds(sb);
        }
    }

    public List<Recommendation.Facet> toFacets() {
        List<Recommendation.Facet> facets =
            new ArrayList<Recommendation.Facet>(facetFilters.size());
        for (FacetFilter facetFilter: facetFilters) {
            facets.add(facetFilter.toFacet());
        }
        return facets;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
