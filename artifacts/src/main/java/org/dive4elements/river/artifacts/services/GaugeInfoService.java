/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.model.RiverFactory;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GaugeInfoService extends D4EService {

    interface Filter {
        boolean apply(Gauge gauge);
    }


    private static final class ReferenceNumberFilter implements Filter {
        private long refNr;

        public ReferenceNumberFilter(long refNr) {
            this.refNr = refNr;
        }

        @Override
        public boolean apply(Gauge  gauge) {
            if (log.isDebugEnabled()) {
                log.debug("Test gauge '" + gauge.getName() + "'");
            }

            return gauge != null && gauge.getOfficialNumber() == refNr;
        }
    } // end of ReferenceNumberFilter class


    /** The log that is used by this service.*/
    private static Logger log = LogManager.getLogger(GaugeInfoService.class);


    public static final String XPATH_RIVERNAME = "/art:river/@name";

    public static final String XPATH_REFERENCE_NR
        = "/art:river/art:filter/art:gauge/text()";


    public GaugeInfoService() {
    }


    @Override
    public Document doProcess(
        Document      data,
        GlobalContext context,
        CallMeta      callMeta
    ) {
        log.debug("GaugeInfoService.process");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        River river = getRiverFromRequest(data);

        List<Filter> filters  = getFilters(data);
        List<Gauge> allGauges = river.getGauges();
        List<Gauge> filtered  = new ArrayList<Gauge>();

        for (Gauge g: allGauges) {
            for (Filter f: filters) {
                if (f.apply(g)) {
                    filtered.add(g);
                    break;
                }
            }
        }

        return buildInfoDocument(filtered);
    }


    protected River getRiverFromRequest(Document data) {
        String rivername = XMLUtils.xpathString(
            data,
            XPATH_RIVERNAME,
            ArtifactNamespaceContext.INSTANCE);

        log.debug("Return Gauge info for River '" + rivername + "'");

        return rivername != null ? RiverFactory.getRiver(rivername) : null;
    }


    protected List<Filter> getFilters(Document data) {
        List<Filter> filters = new ArrayList<Filter>();

        String refNr = XMLUtils.xpathString(
            data,
            XPATH_REFERENCE_NR,
            ArtifactNamespaceContext.INSTANCE);

        if (refNr != null && refNr.length() > 0) {
            try {
                filters.add(
                    new ReferenceNumberFilter(Long.parseLong(refNr)));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe, nfe);
            }
        }

        return filters;
    }


    protected Document buildInfoDocument(List<Gauge> gauges) {
        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element service = cr.create("service");

        log.debug("Append " + gauges.size() + " gauges to info doc.");

        for (Gauge g: gauges) {
            Range r = g.getRange();

            Element el = cr.create("gauge");
            cr.addAttr(el, "name", g.getName());
            cr.addAttr(el, "lower", String.valueOf(r.getA().doubleValue()));
            cr.addAttr(el, "upper", String.valueOf(r.getB().doubleValue()));

            service.appendChild(el);
        }

        doc.appendChild(service);

        return doc;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
