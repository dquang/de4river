/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataFactory;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Load;

/** Service delivering info about sediment loads. */
public class SedimentLoadInfoService
extends D4EService
{
    /** The log used in this service. */
    private static Logger log = LogManager.getLogger(
        SedimentLoadInfoService.class);

    public static final String RIVER_XPATH = "/art:river/text()";
    public static final String TYPE_XPATH = "/art:river/art:type/text()";
    public static final String SQ_TI_XPATH = "/art:river/art:sq_ti_id/text()";
    public static final String FROM_XPATH =
        "/art:river/art:location/art:from/text()";
    public static final String TO_XPATH =
        "/art:river/art:location/art:to/text()";

    /**
     * Create document with sedimentload infos,
     * constrained by contents in data.
     */
    @Override
    protected Document doProcess(
        Document data,
        GlobalContext globalContext,
        CallMeta callMeta) {
        String river = XMLUtils.xpathString(
            data,
            RIVER_XPATH,
            ArtifactNamespaceContext.INSTANCE);
        String type = XMLUtils.xpathString(
            data,
            TYPE_XPATH,
            ArtifactNamespaceContext.INSTANCE);
        String from = XMLUtils.xpathString(
            data,
            FROM_XPATH,
            ArtifactNamespaceContext.INSTANCE);
        String to = XMLUtils.xpathString(
            data,
            TO_XPATH,
            ArtifactNamespaceContext.INSTANCE);
        String sq_ti_id = XMLUtils.xpathString(
            data,
            SQ_TI_XPATH,
            ArtifactNamespaceContext.INSTANCE);
        double fromD, toD;
        try {
            fromD = Double.parseDouble(from);
            toD = Double.parseDouble(to);
        }
        catch (NumberFormatException nfe) {
            log.warn("Invalid locations. Cannot return sediment loads.");
            return XMLUtils.newDocument();
        }

        /* This call initializes the sedimentloaddata for the river. Might be
         * expensive but has to be done anyway for the calculation later on. */
        SedimentLoadData allLoadData =
            SedimentLoadDataFactory.INSTANCE.getSedimentLoadData(river);

        log.debug("Requested type: " + type + " with sq_ti_id: " + sq_ti_id);
        Collection <Load> loads;
        if (type.equals("sq_time_intervals")) {
            loads = allLoadData
                .findDistinctSQTimeIntervalNonEpochLoadsWithValue(fromD, toD);

            for (Iterator<Load> it = loads.iterator(); it.hasNext();) {
                /* Skip loads without time interval for this info type. */
                Load cur = it.next();
                if (cur.getSQRelationTimeIntervalId() == null) {
                    it.remove();
                }
            }
        } else {
            if (!sq_ti_id.isEmpty()) {
                Integer id = Integer.parseInt(sq_ti_id);
                loads = allLoadData.findLoadsWithValue(fromD, toD, id);
            } else {
                loads = allLoadData.findLoadsWithValue(fromD, toD);
            }
            for (Iterator<Load> it = loads.iterator(); it.hasNext();) {
                /* Skip epochs . */
                if (it.next().isEpoch()) {
                    it.remove();
                }
            }
        }

        return buildDocument(loads);
    }

    protected Document buildDocument(Collection<Load> loads) {
        Document result = XMLUtils.newDocument();
        Element all = result.createElement("sedimentloads");
        for (Load load : loads) {
            Element ele = result.createElement("sedimentload");
            ele.setAttribute("description", load.getDescription());
            if (load.isEpoch()) {
                Calendar calendarS = Calendar.getInstance();
                calendarS.setTime(load.getStartTime());
                Calendar calendarE = Calendar.getInstance();
                calendarE.setTime(load.getStopTime());
                ele.setAttribute(
                    "date",
                    calendarS.get(Calendar.YEAR) +
                        " - " +
                        calendarE.get(Calendar.YEAR));
            }
            else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(load.getStartTime());
                ele.setAttribute(
                    "date",
                    String.valueOf(calendar.get(Calendar.YEAR)));
            }
            /* SQ Time interval */
            if (load.getSQRelationTimeIntervalId() != null) {
                ele.setAttribute(
                    "sq_ti_id",
                    String.valueOf(load.getSQRelationTimeIntervalId()));
                Date start = load.getSQStartTime();
                Date stop = load.getSQStopTime();
                if (start != null && stop != null) {
                    Calendar calendarS = Calendar.getInstance();
                    calendarS.setTime(start);
                    Calendar calendarE = Calendar.getInstance();
                    calendarE.setTime(stop);
                    ele.setAttribute(
                        "sq_date",
                        calendarS.get(Calendar.YEAR) +
                            " - " +
                            calendarE.get(Calendar.YEAR));
                } else if (start != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(start);
                    ele.setAttribute(
                        "sq_date",
                        String.valueOf(calendar.get(Calendar.YEAR)));
                } else {
                    log.warn("Load: " + load.getSQRelationTimeIntervalId() +
                            " has no beginning.");
                }
            }

            all.appendChild(ele);
        }
        result.appendChild(all);
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
