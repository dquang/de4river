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
import org.dive4elements.river.model.Gauge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;


public class KmFromGaugeNameInjector
implements ContextInjector
{

    private static Logger log = LogManager.getLogger(
        KmFromGaugeNameInjector.class);

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

        D4EArtifact d4e = (D4EArtifact)artifact;
        RangeAccess access = new RangeAccess(d4e);
        String name = d4e.getDataAsString("gauge_name");
        if (name == null || name.equals("")) {
            return;
        }
        Gauge gauge = access.getRiver().determineGaugeByName(name);
        if (gauge == null) {
            log.error("No Gauge could be found for name " + name + "!");
            return;
        }
        ctx.putContextValue(CURRENT_KM, gauge.getStation().doubleValue());
        return;
    }

}
