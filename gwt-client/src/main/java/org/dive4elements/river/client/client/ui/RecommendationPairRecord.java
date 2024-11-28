/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.grid.ListGridRecord;

import org.dive4elements.river.client.shared.model.Recommendation;


/**
 * Two strings to be displayed in a GridList, derived from two
 * Recommendations.
 */
public class RecommendationPairRecord extends ListGridRecord {

    /** First attribute-name for StringPairRecord. */
    protected static final String ATTRIBUTE_FIRST  = "first";

    /** Second attribute-name for StringPairRecord. */
    protected static final String ATTRIBUTE_SECOND = "second";

    /** The "first" recommendation (typically the minuend). */
    Recommendation first;

    /** The "second" recommendation (typically the subtrahend). */
    Recommendation second;

    /**
     * Whether the RecommendationPairRecord was restored from data and thus
     * already loaded (usually cloned) in an ArtifactCollection or not.
     */
    boolean alreadyLoaded;


    /** Trivial, blocked constructor. */
    @SuppressWarnings("unused")
    private RecommendationPairRecord() {
    }


    /**
     * Create a new RecommendationPairRecord.
     *
     * @param first  The first recommendation (typically the minuend).
     * @param second The second recommendation (typically the subtrahend).
     */
    public RecommendationPairRecord(
        Recommendation first,
        Recommendation second)
    {
        setFirst(first);
        setSecond(second);
        alreadyLoaded = false;
    }


    /**
     * Sets the first recommendation with info (minuend).
     * @param first Recommendation to store.
     */
    public void setFirst(Recommendation first) {
        this.first = first;
        setAttribute(ATTRIBUTE_FIRST, first.getDisplayName());
    }


    /**
     * Sets the second recommendation with info (subtrahend).
     * @param second Recommendation to store.
     */
    public void setSecond(Recommendation second) {
        this.second = second;
        setAttribute(ATTRIBUTE_SECOND, second.getDisplayName());
    }


    /**
     * Get first recommendation (typically the minuend).
     * @return first recommendation (typically the minuend).
     */
    public Recommendation getFirst() {
        return first;
    }


    /**
     * Get second recommendation (typically the subtrahend).
     * @return second recommendation (typically the subtrahend).
     */
    public Recommendation getSecond() {
        return second;
    }


    /**
     * Get name of first recommendation (typically the minuend).
     * @return name of first recommendation (typically the minuend).
     */
    public String getFirstName() {
        return first.getDisplayName();
    }


    /**
     * Get name of second recommendation (typically the subtrahend).
     * @return name of second recommendation (typically the subtrahend).
     */
    public String getSecondName() {
        return second.getDisplayName();
    }


    /**
     * Sets whether or not the Recommendation is already loaded (in contrast
     * to not yet loaded).
     * @param isAlreadyLoaded new value.
     */
    public void setIsAlreadyLoaded(boolean isAlreadyLoaded) {
        this.alreadyLoaded = isAlreadyLoaded;
    }


    /**
     * Whether or not this pair of recommendations is already laoded (usually
     * cloned) in an ArtifactCollection.
     * @return whether pair of recommendations is already loaded.
     */
    public boolean isAlreadyLoaded() {
        return this.alreadyLoaded;
    }
}
