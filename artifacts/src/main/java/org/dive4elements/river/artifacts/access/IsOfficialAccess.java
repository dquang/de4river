/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;


/** Access data used to mark a certain Wst column as an official line. */
public class IsOfficialAccess extends Access
{
    protected Boolean isOfficial;


    public IsOfficialAccess(D4EArtifact givenArtifact) {
        super(givenArtifact);
    }

    /** Returns whether the artifact marked its wst col as official. */
    public Boolean isOfficial() {

        if (isOfficial == null) {
            String value = getString("official");
            isOfficial = (value != null && value.equals("1"));
        }

        return isOfficial;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
