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


/**
 * This class represents an artifact for the client. It contains the necessary
 * information for the client and the communication with the artifact server.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Artifact extends Serializable {

    /**
     * Returns the UUID of the artifact.
     *
     * @return the UUID.
     */
    public String getUuid();


    /**
     * Returns the hash of the artifact.
     *
     * @return the hash.
     */
    public String getHash();


    /**
     * Returns the name of the artifact.
     * This happens to be the factory name, too.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Returns the ArtifactDescription.
     *
     * @return the artifact description.
     */
    public ArtifactDescription getArtifactDescription();


    /**
     * Returns true, if the Artifact is in Background mode.
     *
     * @return true, if the artifact is in background mode.
     */
    public boolean isInBackground();


    /**
     * Return a list of background messages.
     *
     * @return a list of background messages.
     */
    public List<CalculationMessage> getBackgroundMessages();


    /**
     * Sets a new ArtifactDescription.
     *
     * @param artifactDescription The new artifact description.
     */
    public void setArtifactDescription(ArtifactDescription artifactDescription);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
