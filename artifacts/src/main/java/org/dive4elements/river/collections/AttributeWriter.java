/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.Output;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.ManagedFacet;

/**
 * Create attribute- element of describe document of an ArtifactCollection.
 * The attribute-element contains the merged output of all outputmodes and
 * facets that are part of the collection.
 */
public class AttributeWriter {

    /** ArtifactDatabase used to fetch Artifacts. */
    protected ArtifactDatabase db = null;

    protected Map<String, Output> oldAttr;

    protected Map<String, Output> newAttr;

    /** List of already seen facets. */
    protected List<Facet>         oldFacets;

    /** List of "new" facets. */
    protected List<Facet>         newFacets;

    /**
     * "Compatibility matrix", maps list of facet names to output names.
     * Any facet that is not found in the list for a specific output will
     * not be added to the resulting document.
     */
    protected Map<String, List<String>> compatibilities;


    /** The result of the <i>write()</i> operation.*/
    protected CollectionAttribute attribute;


    private static Logger log = LogManager.getLogger(AttributeWriter.class);


    /**
     * Create a AttributeWriter.
     * Attributes not present in newAttr will not be included in the document.
     * @param db      Database to fetch artifacts.
     * @param oldAttr "Old" (possibly user-changed) outputs.
     * @param newAttr "New" (eventually re-read in its original, unchanged
     *                form) outputs.
     * @param matrix Compatibility matrix, mapping output names to list of
     *               facet names that can be included in this out.
     */
    public AttributeWriter(
        ArtifactDatabase    db,
        CollectionAttribute attribute,
        Map<String, Output> oldAttr,
        List<Facet>         oldFacets,
        Map<String, Output> newAttr,
        List<Facet>         newFacets,
        Map<String, List<String>> matrix)
    {
        this.db        = db;
        this.attribute = attribute;
        this.oldAttr   = oldAttr;
        this.newAttr   = newAttr;
        this.oldFacets = oldFacets;
        this.newFacets = newFacets;
        this.compatibilities = matrix;
    }


    /**
     * Create document by merging outputs given in
     * constructor.
     *
     * The "new" set rules about existance of attributes, so anything not
     * present in it will not be included in the resulting document.
     * The "old" set rules about the content of attributes (as user changes
     * are recorded here and not in the new set).
     *
     * @return document with merged outputs as described.
     */
    protected CollectionAttribute write() {

        boolean debug = log.isDebugEnabled();

        for (Map.Entry<String, Output> entry: newAttr.entrySet()) {
            String outName = entry.getKey();
            Output a       = entry.getValue();

            if (!attribute.hasOutput(outName)) {
                attribute.addOutput(outName, a);
            }

            attribute.clearFacets(outName);

            if (debug) {
                log.debug("Merge Output: " + outName);
                log.debug("   old Facets: " + oldFacets.size());
                log.debug("   new Facets: " + newFacets.size());
            }

            writeOutput(a.getName(), newFacets, oldFacets);
        }

        // THIS CALL IS ABSOLUTELY NECESSARY!
        attribute.cleanEmptyOutputs();

        return attribute;
    }


    /**
     * @param outputName the "new" outputs name
     * @param newOutFacets Facets of the new outputs
     * @param oldOutFacets Facets of the old outputs (can be null)
     */
    protected void writeOutput(
        String      outputName,
        List<Facet> newOutFacets,
        List<Facet> oldOutFacets
    ) {
        List<String> compatFacets = this.compatibilities.get(outputName);

        try {
            writeFacets(outputName, newOutFacets, oldOutFacets, compatFacets);
        }
        catch (ArtifactDatabaseException ade) {
            log.error(ade, ade);
        }
    }


    /**
     * @param newFacets the new facets
     * @param oldFacets the old facets
     * @param compatibleFacets List of facets to accept
     * @return true if any facets are written to the out.
     */
    protected boolean writeFacets(
        String        outputName,
        List<Facet>   newFacets,
        List<Facet>   oldFacets,
        List<String>  compatibleFacets)
    throws ArtifactDatabaseException
    {
        if (compatibleFacets == null) {
            log.warn("No compatible facets, not generating out "
                + outputName + ".");
            return false;
        }

        int num = newFacets.size();

        // Add all new Facets either in their old state or (if really
        // new) as they are.
        List<ManagedFacet> currentFacets      = new ArrayList<ManagedFacet>();
        List<ManagedFacet> genuinelyNewFacets = new ArrayList<ManagedFacet>();

        boolean debug = log.isDebugEnabled();
        if (debug) {
           log.debug("Compatible facets are " + compatibleFacets);
        }

        for (Facet fac: newFacets) {
            ManagedFacet facet = (ManagedFacet) fac;

            String bondage = facet.getBoundToOut();
            if (bondage != null && bondage.equals(outputName)) {
                log.debug("Adding bound facet regardless of compatibility: " +
                    facet.getName());
            } else if (!compatibleFacets.contains(facet.getName())) {
                log.debug("Skip incompatible facet " + facet.getName());
                continue;
            } else if (facet.getBoundToOut() != null &&
                    !facet.getBoundToOut().equals(outputName)) {
                log.debug("Skip facet " + facet.getName() +
                        " because it is bound to: " + facet.getBoundToOut());
                continue;
            } else {
                log.debug("Compatible facet " + facet.getName() +
                    " is bound to: " + facet.getBoundToOut());
            }

            ManagedFacet picked = pickFacet(facet, oldFacets);

            if (facet.equals(picked)) {
                if (!facetInTwoOuts(facet, genuinelyNewFacets)) {
                    genuinelyNewFacets.add(picked);
                }
                else {
                    log.debug(
                        "Skip clone facet that shall be present in two outs");
                }
            }
            else {
                currentFacets.add(picked);
            }
        }

        FacetActivity.Registry registry = FacetActivity.Registry.getInstance();

        // With each genuinely new Facet, figure out whether it comes to live
        // in/activate.
        for (ManagedFacet newMF: genuinelyNewFacets) {
            D4EArtifact flys =
                (D4EArtifact)db.getRawArtifact(newMF.getArtifact());

            boolean isActive = registry.isInitialActive(
                flys.getName(), flys, newMF, outputName);

            newMF.setActive(isActive ? 1 : 0);
        }

        // For each genuinely new Facet check positional conflicts.
        for (ManagedFacet newMF: genuinelyNewFacets) {
            boolean conflicts = true;
            // Loop until all conflicts resolved.
            while (conflicts) {
                conflicts = false;
                for (ManagedFacet oldMF: currentFacets) {
                    if (newMF.getPosition() == oldMF.getPosition()) {
                        conflicts = true;
                        if (debug) {
                            log.debug(
                                "Positional conflict while merging " +
                                "facets, pushing newest facet 1 up (" +
                                newMF.getPosition() + ")");
                        }
                        newMF.setPosition(newMF.getPosition() + 1);
                        break;
                    }
                }
            }
            currentFacets.add(newMF);
        }

        // Fill "gaps" (e.g. position 1,2,5 are taken, after gap filling
        // expect positions 1,2,3 [5->3])
        // Preparations to be able to detect gaps.
        Map<Integer, ManagedFacet> mfmap =
            new HashMap<Integer, ManagedFacet>();
        int maxPosition = 0;
        for (ManagedFacet mf: currentFacets) {
            int pos = mf.getPosition();
            mfmap.put(Integer.valueOf(pos), mf);
            if (pos > maxPosition) maxPosition = pos;
        }

        // TODO issue1458: debug what happens

        // Finally do gap correction
        // (note that posistions start at 1, not at zero).
        if (maxPosition != currentFacets.size()) {
            int gap = 0;
            for (int i = 1; i <= maxPosition; i++) {
                ManagedFacet mf = mfmap.get(Integer.valueOf(i));
                if (mf == null) {
                    gap++;
                    continue;
                }
                mf.setPosition(mf.getPosition() - gap);
            }
        }

        // Now add all facets.
        for (ManagedFacet facet: currentFacets) {
            attribute.addFacet(outputName, facet);
        }

        return !currentFacets.isEmpty();
    }


    /** Returns true if a likely clone of facet is
     * contained in genuinelyNewFacets, as happens when same facet is defined
     * for two outs. */
    private boolean facetInTwoOuts(
        ManagedFacet facet,
        List<ManagedFacet> genuinelyNewFacets
    ) {
        for (ManagedFacet otherFacet: genuinelyNewFacets) {
            if (facet.isSame(otherFacet)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the facet to be added to Document.
     * Return the new facet only if the "same" facet was not present before.
     * Return the "old" facet otherwise (user-defined information sticks
     * to it).
     * @param facet     the new facet.
     * @param oldFacets the old facets, new facet is compared against each of
     *                  these.
     * @return facet if genuinely new, matching old facet otherwise.
     */
    protected ManagedFacet pickFacet(ManagedFacet facet, List<Facet> oldFacets)
    {
        if (oldFacets == null) {
            log.debug("No old facets to compare a new to found.");
            return facet;
        }

        String hash = facet.getName() + facet.getIndex() + facet.getArtifact();

        // Compare "new" facet with all old facets.
        // Take oldFacet if that facet was already present (otherwise
        // information is lost, the new one otherwise.
        for (Facet oFacet: oldFacets) {
            ManagedFacet oldFacet = (ManagedFacet) oFacet;
            String oldHash = oldFacet.getName()
                           + oldFacet.getIndex()
                           + oldFacet.getArtifact();
            if (hash.equals(oldHash)) {
                return oldFacet;
            }
        }
        return facet;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
