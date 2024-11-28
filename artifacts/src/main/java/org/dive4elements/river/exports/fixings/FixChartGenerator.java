/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.exports.XYChartGenerator;

import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

/**
 * Base class for FixChartGenerator.
 */
public abstract class FixChartGenerator
extends XYChartGenerator
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(FixChartGenerator.class);

    public static final Double INVALID_KM = Double.valueOf(-1d);
    public static final String CURRENT_KM = "currentKm";
    public static final String XPATH_CHART_CURRENTKM =
        "/art:action/art:attributes/art:currentKm/@art:km";

    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        super.init(outName, request, out, context);

        Double currentKm = getCurrentKmFromRequest(request);

        if (log.isDebugEnabled()) {
            log.debug("currentKm = " + currentKm);
        }

        if (currentKm != INVALID_KM) {
            context.putContextValue(CURRENT_KM, currentKm);
        }
    }

    public static final Double getCurrentKmFromRequest(Document request) {

        String km = XMLUtils.xpathString(
            request,
            XPATH_CHART_CURRENTKM,
            ArtifactNamespaceContext.INSTANCE);

        if (km == null) {
            return INVALID_KM;
        }

        try {
            return Double.valueOf(km);
        }
        catch (NumberFormatException nfe) {
            return INVALID_KM;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
