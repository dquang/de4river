/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.User;

import org.dive4elements.artifactdatabase.BackendListener;
import org.dive4elements.artifactdatabase.Backend;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/** Triggers Datacage to update db. */
public class DatacageBackendListener
implements   BackendListener
{
    private static Logger log =
        LogManager.getLogger(DatacageBackendListener.class);

    protected GlobalContext context;

    public DatacageBackendListener() {
        log.debug("new DatacageBackendListener");
    }

    protected Datacage getDatacage() {
        Object listener = context.get(Datacage.DATACAGE_KEY);
        return listener instanceof Datacage
            ? (Datacage)listener
            : null;
    }

    @Override
    public void setup(GlobalContext context) {
        log.debug("setup");
        this.context = context;
        Datacage l = getDatacage();
        if (l != null) {
            l.setup(context);
        }
    }

    @Override
    public void createdArtifact(Artifact artifact, Backend backend) {
        log.debug("createdArtifact");
        Datacage l = getDatacage();
        if (l != null) {
            l.createdArtifact(artifact, backend, context);
        }
    }

    /** Stores the artifact in artifact-db, if any. */
    @Override
    public void storedArtifact(Artifact artifact, Backend backend) {
        log.debug("storedArtifact");
        Datacage l = getDatacage();
        if (l != null) {
            l.storedArtifact(artifact, backend, context);
        }
    }

    @Override
    public void createdUser(User user, Backend backend) {
        log.debug("createdUser");
        Datacage l = getDatacage();
        if (l != null) {
            l.createdUser(user, backend, context);
        }
    }

    @Override
    public void deletedUser(String identifier, Backend backend) {
        log.debug("deletedUser");
        Datacage l = getDatacage();
        if (l != null) {
            l.deletedUser(identifier, backend, context);
        }
    }

    @Override
    public void createdCollection(
        ArtifactCollection collection,
        Backend            backend
    ) {
        log.debug("createdCollection");
        Datacage l = getDatacage();
        if (l != null) {
            l.createdCollection(collection, backend, context);
        }
    }

    @Override
    public void deletedCollection(String identifier, Backend backend) {
        log.debug("deletedCollection");
        Datacage l = getDatacage();
        if (l != null) {
            l.deletedCollection(identifier, backend, context);
        }
    }

    @Override
    public void changedCollectionAttribute(
        String   identifier,
        Document document,
        Backend  backend
    ) {
        log.debug("changedCollectionAttribute");
        Datacage l = getDatacage();
        if (l != null) {
            l.changedCollectionAttribute(
                identifier, document, backend, context);
        }
    }

    @Override
    public void changedCollectionItemAttribute(
        String   collectionId,
        String   artifactId,
        Document document,
        Backend  backend
    ) {
        log.debug("changedCollectionItemAttribute");
        Datacage l = getDatacage();
        if (l != null) {
            l.changedCollectionItemAttribute(
                collectionId, artifactId, document, backend, context);
        }
    }

    @Override
    public void addedArtifactToCollection(
        String  artifactId,
        String  collectionId,
        Backend backend
    ) {
        log.debug("addedArtifactToCollection");
        Datacage l = getDatacage();
        if (l != null) {
            l.addedArtifactToCollection(
                artifactId, collectionId, backend, context);
        }
    }

    @Override
    public void removedArtifactFromCollection(
        String  artifactId,
        String  collectionId,
        Backend backend
    ) {
        log.debug("removedArtifactFromCollection");
        Datacage l = getDatacage();
        if (l != null) {
            l.removedArtifactFromCollection(
                artifactId, collectionId, backend, context);
        }
    }

    @Override
    public void setCollectionName(
        String collectionId,
        String name
    ) {
        log.debug("setCollectionName");
        Datacage l = getDatacage();
        if (l != null) {
            l.setCollectionName(collectionId, name, context);
        }
    }

    @Override
    public void killedCollections(List<String> identifiers, Backend backend) {
        log.debug("killedCollections");
        Datacage l = getDatacage();
        if (l != null) {
            l.killedCollections(identifiers, context);
        }
    }

    @Override
    public void killedArtifacts(List<String> identifiers, Backend backend) {
        log.debug("killedArtifacts");
        Datacage l = getDatacage();
        if (l != null) {
            l.killedArtifacts(identifiers, context);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
