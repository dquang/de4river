/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.injector;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.ContextInjector;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;

public class KmFromLocationInjector
implements ContextInjector
{

    private static Logger log = LogManager.getLogger(KmFromLocationInjector.class);

    @Override
    public void setup(Element cfg) {
    }

    @Override
    public void injectContext(
        CallContext ctx,
        Artifact artifact,
        Document doc
    ) {
        if (ctx.getContextValue(CURRENT_KM) instanceof Number) {
            return;
        }

        RangeAccess access = new RangeAccess((D4EArtifact)artifact);
        if (access.getLocations() != null &&
            access.getLocations().length > 0) {
            ctx.putContextValue(CURRENT_KM, access.getLocations()[0]);
            return;
        }
        log.warn("No locations accessible.");
    }
}
