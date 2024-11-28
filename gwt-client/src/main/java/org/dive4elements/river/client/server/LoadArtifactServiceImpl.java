/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Recommendation;

import org.dive4elements.river.client.client.services.LoadArtifactService;

/**
 * This service creates a new Artifact based on a given Recommendation and puts
 * this new artifact into a specified Collection.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LoadArtifactServiceImpl
extends      ArtifactServiceImpl
implements   LoadArtifactService
{
    private static final Logger log =
        LogManager.getLogger(LoadArtifactServiceImpl.class);

    /** Error. */
    public static final String ERROR_LOAD_ARTIFACT = "error_load_artifact";


    /**
     * Clones or creates a single artifact and adds it to a collection.
     *
     * Note that in contrast to loadMany, always the given factory is used
     * to clone the artifact.
     *
     * @param parent  collection to add recommendation to.
     * @param recom   recommendation to create clone for.
     * @param factory factory to use.
     * @param locale  the locale to translate messages.
     */
    public Artifact load(
        Collection     parent,
        Recommendation recom,
        String         factory,
        String         locale
    )
    throws ServerException {
        log.info(
            "LoadArtifactServiceImpl.load: " + recom.getMasterArtifact());

        String url  = getServletContext().getInitParameter("server-url");

        // 1) Clone the Artifact specified in >>recom<<
        Artifact clone = ArtifactHelper.createArtifact(
            url, locale, factory, recom);

        if (clone != null) {
            log.debug("Successfully create Artifact Clone. Add now!");
            Collection c = CollectionHelper.addArtifact(
                parent, clone, url, locale);

            if (c != null) {
                log.debug("Successfully added Clone to Collection.");

                return clone;
            }
        }

        throw new ServerException(ERROR_LOAD_ARTIFACT);
    }


    /**
     * Clone/create one or more artifacts and add it to a collection, avoiding
     * duplicates.
     *
     * @param parent  Collection where clones will be added to.
     * @param recoms  definitions of source of clone.
     * @param factory name of factory to use when cloning artifacts (can be
     *                null in which case the recommendations getFactory() will
     *                be used.
     * @param locale  the locale to translate messages.
     *
     * @return cloned artifacts (same artifact might be contained multiple
     *         times).
     */
    public Artifact[] loadMany(
        Collection       parent,
        Recommendation[] recoms,
        String           factory,
        String           locale
    )
    throws ServerException {
        log.debug("LoadArtifactServiceImpl.loadMany");

        String url = getServletContext().getInitParameter("server-url");

        ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
        HashMap<Recommendation, Artifact> cloneMap =
            new HashMap<Recommendation, Artifact>();

        // TODO Respect the index of what to clone.

        // 1) Clone the Artifacts specified in >>recoms<<
        for (Recommendation recom : recoms) {
            // Do not do two clones of two identical recommendations.
            Artifact prevClone = cloneMap.get(recom);
            if (prevClone != null) {
                // Already cloned a recommendation like this.
                log.debug("LoadArtifactServiceImpl: Avoid reclones, "
                    + "clone already exists.");
                artifacts.add(prevClone);
            }
            else {
                // Not already cloned.
                String realFactory = factory != null
                    ? factory
                    : recom.getFactory();

                log.debug("One will be cloned with : " + realFactory);

                Artifact clone = ArtifactHelper.createArtifact(
                    url, locale, realFactory, recom);

                if (clone != null) {
                    log.debug("LoadArtifactServiceImple: Successfully "
                        + "loaded Artifact Clone.");
                    Collection c = CollectionHelper.addArtifact(
                        parent, clone, url, locale);

                    if (c != null) {
                        artifacts.add(clone);
                        // Remember we cloned a recommendation like this.
                        cloneMap.put(recom, clone);
                    }
                    else {
                        throw new ServerException(ERROR_LOAD_ARTIFACT);
                    }
                }
            }
        }
        return artifacts.toArray(new Artifact[artifacts.size()]);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
