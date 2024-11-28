/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

/**
 * The default MINFO artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MINFOArtifact
extends      D4EArtifact
{
    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "minfo";


    /**
     * The default constructor.
     */
    public MINFOArtifact() {
    }

    /**
     * Returns the name of the concrete artifact.
     *
     * @return the name of the concrete artifact.
     */
    @Override
    public String getName() {
        return ARTIFACT_NAME;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
