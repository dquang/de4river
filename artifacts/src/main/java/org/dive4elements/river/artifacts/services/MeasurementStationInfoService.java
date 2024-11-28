/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.TimeInterval;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class MeasurementStationInfoService extends RiverInfoService {

    private static final Logger log = LogManager.getLogger(
            MeasurementStationInfoService.class);

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(
        DateFormat.SHORT, Locale.GERMANY);

    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        Document result = super.doProcess(data, globalContext, callMeta);

        Element egs = ec.create("measurement-stations");

        List<MeasurementStation> mstations = MeasurementStation
            .getStationsAtRiver(river);

        if (log.isDebugEnabled()) {
            log.debug("Loaded stations: " + mstations);
        }

        for (MeasurementStation mstation: mstations) {
            Element eg = ec.create("measurement-station");

            String name = mstation.getName();
            if (name != null) {
                ec.addAttr(eg, "name", name, true);
            }

            Integer id = mstation.getId();
            if (id != null) {
                ec.addAttr(eg, "id", Integer.toString(id), true);
            }

            String type = mstation.getMeasurementType();
            if (type != null) {
                ec.addAttr(eg, "type", type, true);
            }

            String riverside = mstation.getRiverside();
            if (riverside != null) {
                ec.addAttr(eg, "riverside", riverside, true);
            }

            Range range = mstation.getRange();
            if (range != null) {
                BigDecimal a = range.getA();
                BigDecimal b = range.getB();

                // In case river is km_up, station is at larger value of range
                if (b != null && river.getKmUp()) {
                    ec.addAttr(eg, "start", getStringValue(b), true);
                    ec.addAttr(eg, "end", getStringValue(a), true);
                }
                else {
                    ec.addAttr(eg, "start", getStringValue(a), true);
                    if (b != null) {
                        ec.addAttr(eg, "end", getStringValue(b), true);
                    }
                }
            }

            String moperator = mstation.getOperator();
            if (moperator != null) {
                ec.addAttr(eg, "operator", moperator, true);
            }

            TimeInterval tinterval = mstation.getObservationTimerange();
            if (tinterval != null) {
                Date tstart = tinterval.getStartTime();
                if (tstart != null) {
                    ec.addAttr(eg, "starttime", DATE_FORMAT.format(tstart),
                            true);
                }
                Date tstop = tinterval.getStopTime();
                if (tstop != null) {
                    ec.addAttr(eg, "stoptime", DATE_FORMAT.format(tstop),
                            true);
                }
            }

            String comment = mstation.getComment();
            if (comment != null) {
                ec.addAttr(eg, "comment", comment, true);
            }

            String gaugename = mstation.getGaugeName();
            if (gaugename != null) {
                Element egauge = ec.create("gauge");
                ec.addAttr(egauge, "name", gaugename, true);
                eg.appendChild(egauge);
            }

            egs.appendChild(eg);
        }

        this.riverele.appendChild(egs);

        return result;
    }

}
