/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MinMaxWQ;
import org.dive4elements.river.model.Range;


/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugeOverviewInfoService extends RiverInfoService {

    private static final Logger log = LogManager.getLogger(
            GaugeOverviewInfoService.class);

    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        Document result = super.doProcess(data, globalContext, callMeta);

        log.debug("GaugeOverviewInfoService.process");

        Element egs = ec.create("gauges");

        List<Gauge> gauges = river.getGauges();

        if (log.isDebugEnabled()) {
            log.debug("Loaded gauges: " + gauges);
        }

        for (Gauge gauge: river.getGauges()) {
            Element eg = ec.create("gauge");

            String name = gauge.getName();
            if (name != null) {
                ec.addAttr(eg, "name", gauge.getName(), true);
            }

            String aeo = getStringValue(gauge.getAeo());
            if (aeo != null) {
                ec.addAttr(eg, "aeo", aeo, true);
            }

            String datum = getStringValue(gauge.getDatum());
            if (datum != null) {
                ec.addAttr(eg, "datum", datum, true);
            }

            Range range = gauge.getRange();
            if (range != null) {
                BigDecimal a = range.getA();
                if (a != null) {
                    double min = a.doubleValue();
                    ec.addAttr(eg, "start", Double.toString(min), true);
                }

                BigDecimal b = range.getB();
                if (b != null) {
                    double max = range.getB().doubleValue();
                    ec.addAttr(eg, "end", Double.toString(max), true);
                }
            }
            MinMaxWQ minmaxwq = gauge.fetchMaxMinWQ();
            String minw = getStringValue(minmaxwq.getMinW());
            String maxw = getStringValue(minmaxwq.getMaxW());
            String minq = getStringValue(minmaxwq.getMinQ());
            String maxq = getStringValue(minmaxwq.getMaxQ());

            if (minw != null) {
                ec.addAttr(eg, "minw", minw, true);
            }
            if (maxw != null) {
                ec.addAttr(eg, "maxw", maxw, true);
            }
            if (minq != null) {
                ec.addAttr(eg, "minq", minq, true);
            }
            if (maxq != null) {
                ec.addAttr(eg, "maxq", maxq, true);
            }

            String station = getStringValue(gauge.getStation());
            if (station != null) {
                ec.addAttr(eg, "station", station, true);
            }

            Long official = gauge.getOfficialNumber();
            if (official != null) {
                ec.addAttr(eg, "official", official.toString(), true);
            }

            egs.appendChild(eg);
        }

        riverele.appendChild(egs);

        return result;
    }
}
