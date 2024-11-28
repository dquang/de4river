/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.GrainFraction;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.SedimentLoadLS;
import org.dive4elements.river.model.TimeInterval;
import org.dive4elements.river.model.Unit;


public class ImportSedimentLoadLS {

    private static Logger log = LogManager.getLogger(ImportSedimentLoadLS.class);

    private ImportGrainFraction grainFraction;

    private ImportUnit unit;

    private ImportTimeInterval timeInterval;

    private ImportTimeInterval sqTimeInterval;

    private String description;

    private Integer kind;

    private List<ImportSedimentLoadLSValue> values;

    private SedimentLoadLS peer;

    public ImportSedimentLoadLS(String description) {
        this.values = new ArrayList<ImportSedimentLoadLSValue>();
        this.description = description;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void setSQTimeInterval(ImportTimeInterval sqTimeInterval) {
        this.sqTimeInterval = sqTimeInterval;
    }

    public void setUnit(ImportUnit unit) {
        this.unit = unit;
    }

    public void setGrainFraction(ImportGrainFraction grainFraction) {
        this.grainFraction = grainFraction;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public void addValue(ImportSedimentLoadLSValue value) {
        this.values.add(value);
    }

    public void storeDependencies(River river) {
        log.debug("store dependencies");

        SedimentLoadLS peer = getPeer(river);

        if (peer != null) {
            int i = 0;

            for (ImportSedimentLoadLSValue value : values) {
                value.storeDependencies(peer);
                i++;
            }

            log.info("stored " + i + " sediment load values.");
        }
    }

    public SedimentLoadLS getPeer(River river) {
        log.debug("get peer");

        GrainFraction gf = grainFraction != null ? grainFraction.getPeer()
            : null;

        Unit u = unit != null ? unit.getPeer() : null;

        TimeInterval ti = timeInterval != null ? timeInterval.getPeer() : null;
        TimeInterval sqti = sqTimeInterval != null
                ? sqTimeInterval.getPeer()
                : null;

        if (ti == null || u == null) {
            log.warn(
                "Skip invalid SedimentLoadLS: time interval or unit null!");
            return null;
        }

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            String sqtquery = sqTimeInterval == null ?
                "sq_time_interval_id is null" :
                "sqTimeInterval = :sqTimeInterval";
            Query query = session.createQuery("from SedimentLoadLS where "
                + "   river=:river and "
                + "   grainFraction=:grainFraction and "
                + "   unit=:unit and "
                + "   timeInterval=:timeInterval and "
                + "   description=:description and "
                + "   kind = :kind and " +
                      sqtquery);

            query.setParameter("river", river);
            query.setParameter("grainFraction", gf);
            query.setParameter("unit", u);
            query.setParameter("timeInterval", ti);
            if (sqti != null) {
                query.setParameter("sqTimeInterval", sqti);
            }
            query.setParameter("description", description);
            query.setParameter("kind", kind);

            List<SedimentLoadLS> loads = query.list();
            if (loads.isEmpty()) {
                log.debug("create new SedimentLoadLS");

                peer = new SedimentLoadLS(river, u, ti, sqti, gf, description);
                peer.setKind(this.kind);
                session.save(peer);
            }
            else {
                peer = loads.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
