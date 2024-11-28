/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import java.math.BigDecimal;

import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.NamedMainValue;
import org.dive4elements.river.model.TimeInterval;

import org.hibernate.Session;
import org.hibernate.Query;

public class ImportMainValue
{
    protected ImportGauge          gauge;
    protected ImportNamedMainValue mainValue;
    protected BigDecimal           value;
    protected ImportTimeInterval   timeInterval;

    protected MainValue peer;

    public ImportMainValue() {
    }

    public ImportMainValue(
        ImportGauge          gauge,
        ImportNamedMainValue mainValue,
        BigDecimal           value,
        ImportTimeInterval   timeInterval
    ) {
        this.gauge        = gauge;
        this.mainValue    = mainValue;
        this.value        = value;
        this.timeInterval = timeInterval;
    }

    public ImportGauge getGauge() {
        return gauge;
    }

    public void setGauge(ImportGauge gauge) {
        this.gauge = gauge;
    }

    public ImportNamedMainValue getMainValue() {
        return mainValue;
    }

    public void setMainValue(ImportNamedMainValue mainValue) {
        this.mainValue = mainValue;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public MainValue getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query;

            TimeInterval t = timeInterval != null
                ? timeInterval.getPeer()
                : null;

            if (t != null) {
                query = session.createQuery("from MainValue where "
                    + "gauge.id=:gauge_id and mainValue.id=:name_id "
                    + "and timeInterval = :time "
                    + "and value=:value");
                query.setParameter("time", t);
            }
            else {
                query = session.createQuery("from MainValue where "
                    + "gauge.id=:gauge_id and mainValue.id=:name_id "
                    + "and timeInterval is null "
                    + "and value=:value");
            }

            Gauge          g = gauge.getPeer(river);
            NamedMainValue n = mainValue.getPeer();
            query.setParameter("gauge_id", g.getId());
            query.setParameter("name_id",  n.getId());
            query.setParameter("value",    value);

            List<MainValue> values = query.list();
            if (values.isEmpty()) {
                peer = new MainValue(g, n, value, t);
                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
