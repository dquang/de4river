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
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.ContextInjector;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.dive4elements.river.exports.injector.InjectorConstants.CURRENT_KM;


public class KmFromRequestInjector
implements ContextInjector
{
    private static final Logger log = LogManager.getLogger(
        KmFromRequestInjector.class);

    public static final String XPATH_CHART_CURRENTKM =
        "/art:action/art:attributes/art:currentKm/@art:km";
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

        Double km = getCurrentKmFromRequest(request);
        if (Double.isNaN(km)) {
            RangeAccess access = new RangeAccess((D4EArtifact)artifact);
            km = access.getFrom();
        }
        ctx.putContextValue(CURRENT_KM, km);
    }

    private Double getCurrentKmFromRequest(Document request) {

        String km = XMLUtils.xpathString(
            request,
            XPATH_CHART_CURRENTKM,
            ArtifactNamespaceContext.INSTANCE);

        if (km == null) {
            return Double.NaN;
        }

        try {
            return Double.valueOf(km);
        }
        catch (NumberFormatException nfe) {
            return Double.NaN;
        }
    }
}
