/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;

import org.dive4elements.river.client.shared.model.Artifact;


/**
 * This events stores references to the old artifact and the new one.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ParameterChangeEvent {

    public static enum Type {
        FORWARD, BACK
    }


    protected Artifact oldArtifact;
    protected Artifact newArtifact;

    protected Type     type;


    public ParameterChangeEvent(Artifact old, Artifact newArt) {
        this(old, newArt, Type.FORWARD);
    }


    public ParameterChangeEvent(Artifact oArt, Artifact nArt, Type type) {
        oldArtifact = oArt;
        newArtifact = nArt;
        this.type   = type;
    }


    public Artifact getOldValue() {
        return oldArtifact;
    }

    public Artifact getNewValue() {
        return newArtifact;
    }

    public Type getType() {
        return type;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
