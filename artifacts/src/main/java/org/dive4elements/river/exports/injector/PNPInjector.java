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
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Gauge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;
import static org.dive4elements.river.exports.injector.InjectorConstants.PNP;

public class PNPInjector
implements ContextInjector
{

    private Logger log = LogManager.getLogger(PNPInjector.class);
    @Override
    public void setup(Element cfg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void injectContext(
        CallContext ctx,
        Artifact artifact,
        Document request
    ) {
        Object currentKm = ctx.getContextValue(CURRENT_KM);
        if (currentKm == null) {
            log.debug("no current km. not injecting pnp");
            return;
        }

        Double km = Double.valueOf(currentKm.toString());
        if (Double.isNaN(km) || Double.isInfinite(km)) {
            log.debug("current km is NAN or infinte. not injecting pnp.");
            return;
        }

        River river = new RiverAccess((D4EArtifact)artifact).getRiver();
        if (river == null) {
            log.error("River not accessible from artifact. Not injecting PNP");
            return;
        }

        Gauge gauge =
            river.determineGaugeAtStation(km);
        if (gauge == null) {
            log.debug("no gauge found at current km. not injecting pnp");
            return;
        }
        log.debug("injecting pnp: " + gauge.getDatum());
        ctx.putContextValue(PNP, gauge.getDatum());
    }

}
