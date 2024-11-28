/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;


public class StaticSQRelationArtifact
extends DefaultArtifact
{

    /** The name of this artifact */
    public static final String NAME = "static_sqrelation";



    public StaticSQRelationArtifact() {
    }

    public StaticSQRelationArtifact(String uuid, String hash) {
        super(uuid, hash);
    }


    public StaticSQRelationArtifact(
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
