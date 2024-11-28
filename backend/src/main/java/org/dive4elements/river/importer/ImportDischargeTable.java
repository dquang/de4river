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

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.TimeInterval;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ImportDischargeTable
{
    private static Logger log = LogManager.getLogger(ImportDischargeTable.class);

    protected DischargeTable peer;

    protected String         description;

    protected Integer        kind;

    protected List<ImportDischargeTableValue> dischargeTableValues;

    protected ImportTimeInterval timeInterval;

    public ImportDischargeTable() {
        this(0, null);
    }

    public ImportDischargeTable(int kind, String description) {
        this.kind            = kind;
        this.description     = description;
        dischargeTableValues = new ArrayList<ImportDischargeTableValue>();
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void addDischargeTableValue(ImportDischargeTableValue value) {
        dischargeTableValues.add(value);
    }


    public void setDischargeTableValues(
        List<ImportDischargeTableValue> values
    ) {
        this.dischargeTableValues = values;
    }


    public List<ImportDischargeTableValue> getDischargeTableValues() {
        return dischargeTableValues;
    }

    public ImportTimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }


    public DischargeTable getPeer(Gauge gauge) {
        if (peer == null) {
            TimeInterval ti = timeInterval != null
                ? timeInterval.getPeer()
                : null;
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            String timeIntervalQuery = ti != null
                ? "timeInterval=:interval"
                : "timeInterval is null";

            Query query = session.createQuery(
                    "from DischargeTable where " +
                    "gauge.id=:gauge and kind=:kind and " +
                    "description=:description and " + timeIntervalQuery);
            query.setParameter("gauge",       gauge.getId());
            query.setParameter("description", description);
            query.setParameter("kind",        kind);
            if (ti != null) {
                query.setParameter("interval", ti);
            }

            List<DischargeTable> dischargeTables = query.list();
            if (dischargeTables.isEmpty()) {
                peer = new DischargeTable(gauge, description, null, kind, ti);
                session.save(peer);
            }
            else {
                peer = dischargeTables.get(0);
            }
        }
        return peer;
    }


    public void storeDependencies(Gauge gauge) {
        log.info("store discharge table '" + description + "'");
        storeDischargeTableValues(gauge);
    }


    public void storeDischargeTableValues(Gauge gauge) {
        DischargeTable dischargeTable = getPeer(gauge);

        for (ImportDischargeTableValue value: dischargeTableValues) {
            value.getPeer(dischargeTable);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
