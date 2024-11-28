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
import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess;
import org.dive4elements.river.model.Gauge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;

public class KmFromGaugeNumberInjector
implements ContextInjector
{
    private static Logger log =
        LogManager.getLogger(KmFromGaugeNumberInjector.class);

    @Override
    public void setup(Element cfg) {

    }

    @Override
    public void injectContext(
        CallContext ctx,
        Artifact artifact,
        Document request
    ) {
        if (ctx.getContextValue(CURRENT_KM) instanceof Number) {
            return;
        }

        HistoricalDischargeAccess access =
            new HistoricalDischargeAccess((D4EArtifact)artifact);
        Long gaugeNumber = access.getOfficialGaugeNumber();
        if (gaugeNumber == null) {
            return;
        }
        Gauge gauge = Gauge.getGaugeByOfficialNumber(gaugeNumber);
        if (gauge == null) {
            return;
        }
        double km = gauge.getStation().doubleValue();
        ctx.putContextValue(CURRENT_KM, km);
    }
}
