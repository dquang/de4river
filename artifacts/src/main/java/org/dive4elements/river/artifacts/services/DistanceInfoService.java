/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.model.FastAnnotations;

import org.dive4elements.river.artifacts.model.LocationProvider;


/**
 * This service provides information about distances of a specified river.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DistanceInfoService extends D4EService {

    /** The log used in this service. */
    private static Logger log = LogManager.getLogger(DistanceInfoService.class);

    public static final String RIVER_XPATH = "/art:river/text()";

    public static final String FILTER_XPATH = "/art:river/art:filter/text()";


    /**
     * The default constructor.
     */
    public DistanceInfoService() {
    }


    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("DistanceInfoService.process");

        String river = XMLUtils.xpathString(
            data, RIVER_XPATH, ArtifactNamespaceContext.INSTANCE);

        String filterName  = XMLUtils.xpathString(
            data, FILTER_XPATH, ArtifactNamespaceContext.INSTANCE);

        if (river == null || (river = river.trim()).length() == 0) {
            log.warn("No river specified. Cannot return distance info!");
            return XMLUtils.newDocument();
        }

        log.debug("Search distances for river: " + river);

        FastAnnotations fas = LocationProvider.getAnnotations(river);

        FastAnnotations.Filter filter = selectFilter(filterName);

        return buildDocument(fas.filter(filter));
    }

    protected Document buildDocument(
        Iterator<FastAnnotations.Annotation> iter
    ) {
        Document result = XMLUtils.newDocument();

        Element all = result.createElement("distances");

        while (iter.hasNext()) {
            all.appendChild(buildNode(result, iter.next()));
        }

        result.appendChild(all);

        return result;
    }

    protected static FastAnnotations.Filter selectFilter(String name) {

        if (name != null) {
            if ("locations".equals(name)) return FastAnnotations.IS_POINT;
            if ("distances".equals(name)) return FastAnnotations.IS_RANGE;
            if ("measuringpoint".equals(name))
                return new FastAnnotations.NameFilter(".*[Mm]essstelle");
        }

        return FastAnnotations.ALL;
    }

    /**
     * Builds an Element for a distance info.
     *
     * @param an The Annotation that provides information about the distance.
     *
     * @return an Element that contains information about a distance.
     */
    protected static Element buildNode(
        Document                   document,
        FastAnnotations.Annotation an
    ) {
        Element distance = document.createElement("distance");

        distance.setAttribute("description", an.getPosition());

        String riverSide = an.getAttribute();

        if (riverSide != null && riverSide.length() > 0) {
            distance.setAttribute("riverside", riverSide);
        }

        distance.setAttribute("from", String.valueOf(an.getA()));

        double b      = an.getB();
        double bottom = an.getBottom();
        double top    = an.getTop();

        if (!Double.isNaN(b)) {
            distance.setAttribute("to", String.valueOf(b));
        }

        if (!Double.isNaN(bottom)) {
            distance.setAttribute("bottom", String.valueOf(bottom));
        }

        if (!Double.isNaN(top)) {
            distance.setAttribute("top", String.valueOf(top));
        }

        return distance;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
