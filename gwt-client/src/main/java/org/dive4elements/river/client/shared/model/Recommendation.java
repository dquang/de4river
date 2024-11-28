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
import java.util.HashMap;

import java.io.Serializable;

/**
 * Information bundle to let client create/clone an artifact with facets.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Recommendation implements Serializable {

    /** Index and name of a facet. */
    public static class Facet implements Serializable {

        /** Facet name. */
        protected String name;

        /** Facet index. */
        protected String index;

        public Facet() {
        }

        public Facet(String name, String index) {
            this.name  = name;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public String getIndex() {
            return index;
        }


        @Override
        public int hashCode() {
            int hash = 0;
            if (getName() != null) {
                hash += getName().hashCode();
            }
            if (getIndex() != null) {
                hash += getIndex().hashCode();
            }
            return hash;
        }


        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Facet) || other == null) {
                return false;
            }
            Facet facet = (Facet) other;
            return (same(facet.getIndex(), this.getIndex()))
                && (same(facet.getName(),  this.getName()));
        }
    } // class Facet


    /** Mapping of outnames to Facet-Lists. */
    public static class Filter implements Serializable {

        protected Map<String, List<Facet>> outs;

        public Filter() {
            outs = new HashMap<String, List<Facet>>();
        }

        public void add(String out, List<Facet> facets) {
            outs.put(out, facets);
        }

        public Map<String, List<Facet>> getOuts() {
            return outs;
        }


        @Override
        public int hashCode() {
            if (getOuts() != null) {
                return getOuts().hashCode();
            }
            return 0;
        }


        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Filter) || other == null) {
                return false;
            }
            Filter filter = (Filter) other;
            return Recommendation.same(filter.getOuts(), this.getOuts());
        }
    } // class Filter

    /** Factory to speak to when creating/cloning. */
    protected String factory;
    /** Sometimes database ids, sometimes other freeform text. */
    protected String ids;
    /** Artifacts uuid that should serve as master artifact. */
    protected String masterArtifact;
    /** Optional facet filter. */
    protected Filter filter;
    /** The out this Artifact should be added to **/
    protected String targetOut;

    protected String displayName = null;

    public Recommendation() {
    }

    public Recommendation(String factory, String ids) {
        this(factory, ids, null, null);
    }

    public Recommendation(String factory, String ids, String targetOut) {
        this(factory, ids, null, null, targetOut);
    }

    public Recommendation(
        String factory,
        String ids,
        String masterArtifact,
        Filter filter
    ) {
        this(factory, ids, masterArtifact, filter, null);
    }

    public Recommendation(
        String factory,
        String ids,
        String masterArtifact,
        Filter filter,
        String targetOut
    ) {
        this.factory        = factory;
        this.ids            = ids;
        this.masterArtifact = masterArtifact;
        this.filter         = filter;
        this.targetOut      = targetOut;
    }

    public String getTargetOut() {
        return targetOut;
    }

    public void setTargetOut(String value) {
        targetOut = value;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIDs() {
        return ids;
    }

    public String getMasterArtifact() {
        return masterArtifact;
    }

    public void setMasterArtifact(String masterArtifact) {
        this.masterArtifact = masterArtifact;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getFactory() != null)
            ? getFactory().hashCode()
            : 0;
        hash += (getIDs() != null)
            ? getIDs().hashCode()
            : 0;
        hash += (getFilter() != null)
            ? getFilter().hashCode()
            : 0;
        hash += (getMasterArtifact() != null)
            ? getMasterArtifact().hashCode()
            : 0;
        hash += (getTargetOut() != null)
            ? getTargetOut().hashCode()
            : 0;
        return hash;
    }


    /**
     * Null-pointer guarded equals.
     * Two null's are assumed equal (returns true);
     * @param a Object to compare against parameter b.
     * @param b Object to compare against parameter a.
     * @return true if either a and b are null or a.equals(b) returns true.
     */
    protected static boolean same(Object a, Object b) {
        // Do null-check.
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        }
        return a.equals(b);
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Recommendation) || other == null) {
            return false;
        }
        Recommendation rec = (Recommendation) other;
        return (same(this.getFactory(), rec.getFactory()))
            && (same(this.getIDs(),     rec.getIDs()))
            && (same(this.getFilter(),  rec.getFilter()))
            && (same(this.getMasterArtifact(), rec.getMasterArtifact()))
            && (same(this.getTargetOut(), rec.getTargetOut()));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
