/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.model.River;

public class IsKmUpEvaluator
implements   DiagramAttributes.Evaluator
{
    public IsKmUpEvaluator() {
    }

    @Override
    public Object evaluate(D4EArtifact artifact, CallContext context) {
        RiverAccess access = new RiverAccess(artifact);
        River river = access.getRiver();
        return river == null
            ? Boolean.FALSE
            : river.getKmUp();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
