/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;


/**
 * The default implementation of an artifact that might be used in the client.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultArtifact implements Artifact {

    /** The artifact's uuid. */
    protected String uuid;

    /** The artifacts hash value. */
    protected String hash;

    /** Determines if the artifact is in background mode.*/
    protected boolean inBackground;

    protected List<CalculationMessage> messages;

    /** The current artifact description. */
    protected ArtifactDescription artifactDescription;

    /**
     * This constructor should not be used to create new instances of this
     * class. An empty artifact without uuid and hash will be the result of
     * this constructor call.
     */
    public DefaultArtifact() {
    }


    public DefaultArtifact(String uuid, String hash) {
        this(uuid, hash, false, null);
    }


    /**
     * This constructor creates a new artifact instance with a uuid and a hash.
     *
     * @param uuid The artifact's uuid.
     * @param hash The artifact's hash.
     */
    public DefaultArtifact(
        String                   uuid,
        String                   hash,
        boolean                  inBackground,
        List<CalculationMessage> messages
    ) {
        this.uuid         = uuid;
        this.hash         = hash;
        this.inBackground = inBackground;
        this.messages     = messages;
    }


    public String getUuid() {
        return uuid;
    }


    public String getHash() {
        return hash;
    }


    public String getName() {
        return "default";
    }


    public boolean isInBackground() {
        return inBackground;
    }


    public List<CalculationMessage> getBackgroundMessages() {
        return messages;
    }


    public ArtifactDescription getArtifactDescription() {
        return artifactDescription;
    }


    public void setArtifactDescription(ArtifactDescription description) {
        this.artifactDescription = description;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
