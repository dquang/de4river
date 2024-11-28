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
 * The WINFO implementation of an Artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WINFOArtifact extends DefaultArtifact {

    /** The name of this artifact: 'winfo'.*/
    public static final String NAME = "winfo";


    public WINFOArtifact() {
    }


    public  WINFOArtifact(String uuid, String hash) {
        super(uuid, hash);
    }


    public WINFOArtifact(
        String                   uuid,
        String                   hash,
        boolean                  inBackground,
        List<CalculationMessage> messages
    ) {
        super(uuid, hash, inBackground, messages);
    }


    public String getName() {
        return NAME;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
