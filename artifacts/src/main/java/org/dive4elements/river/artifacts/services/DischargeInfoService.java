/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.Collections;
import java.util.List;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.DischargeTables;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.TimeInterval;

/**
 * This service provides information about discharges at a defined gauge.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DischargeInfoService extends D4EService {

    /** The log used in this service. */
    private static Logger log = LogManager.getLogger(DischargeInfoService.class);

    public static final String GAUGE_XPATH = "/art:gauge/text()";

    public static final String RIVER_NAME_XPATH =
        "/art:gauge/art:river/text()";

    public DischargeInfoService() {
    }


    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        if (log.isDebugEnabled()) {
            log.debug("DischargeInfoService.process");
            log.debug(XMLUtils.toString(data));
        }

        String gaugeNumber = XMLUtils.xpathString(
            data, GAUGE_XPATH, ArtifactNamespaceContext.INSTANCE);

        String river = XMLUtils.xpathString(
            data, RIVER_NAME_XPATH, ArtifactNamespaceContext.INSTANCE);

        if (gaugeNumber == null ||
           (gaugeNumber = gaugeNumber.trim()).length() == 0) {
            log.warn("No gauge specified. Cannot return discharge info.");
            return XMLUtils.newDocument();
        }

        log.debug("Getting discharge for gauge: " + gaugeNumber
            + " at river: " + river);

        long gn;
        try {
            gn = Long.parseLong(gaugeNumber);
        }
        catch (NumberFormatException nfe) {
            log.warn("Invalid gauge number. Cannot return discharge info.");
            return XMLUtils.newDocument();
        }

        Gauge gauge;
        if (river == null || river.isEmpty()) {
            gauge = Gauge.getGaugeByOfficialNumber(gn);
        } else {
            gauge = Gauge.getGaugeByOfficialNumber(gn, river);
        }

        if (gauge == null) {
            log.warn("No such gauge found.");
            return XMLUtils.newDocument();
        }

        log.debug("Found gauge: " + gauge.getName() + " id: " + gauge.getId());

        return buildDocument(gauge);
    }


    protected Document buildDocument(Gauge gauge) {
        Document result = XMLUtils.newDocument();

        List<DischargeTable> tables = gauge.getDischargeTables();
        Collections.sort(tables);

        log.debug("# of tables:" + tables.size());

        Element all = result.createElement("discharges");
        for (DischargeTable dt: tables) {
            if (dt.getKind() == DischargeTables.MASTER) {
                continue;
            }
            Element discharge = result.createElement("discharge");
            discharge.setAttribute("description", dt.getDescription());

            // Get time interval.
            TimeInterval ti = dt.getTimeInterval();

            if (ti == null) {
                log.warn("DischargeTable has no TimeInterval set!");
                continue;
            }

            Date startTime = ti.getStartTime();
            Date stopTime = ti.getStopTime();

            if (startTime != null) {
                discharge.setAttribute(
                    "start", String.valueOf(startTime.getTime()));
            }
            else {
                continue;
            }

            if (stopTime != null) {
                discharge.setAttribute(
                    "end", String.valueOf(stopTime.getTime()));
            }
            else {
                long now = System.currentTimeMillis();
                discharge.setAttribute("end", String.valueOf(now));
            }

            String bfgId = dt.getBfgId();
            if (bfgId != null) {
                discharge.setAttribute("bfg-id", bfgId);
            }

            all.appendChild(discharge);
        }
        result.appendChild(all);
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
