/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.SedimentLoad;
import org.dive4elements.river.model.SedimentLoadValue;
import org.hibernate.Query;
import org.hibernate.Session;

public class ImportSedimentLoadValue {

    private SedimentLoadValue peer;

    private MeasurementStation station;
    private Double             value;

    public ImportSedimentLoadValue() {
    }

    public ImportSedimentLoadValue(
        MeasurementStation station,
        Double             value
    ) {
        this.station      = station;
        this.value        = value;
    }

    protected SedimentLoadValue getPeer(SedimentLoad sedimentLoad) {

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from SedimentLoadValue where " +
                "   measurementStation = :station and " +
                "   sedimentLoad = :sedimentLoad and " +
                "   value = :value");

            query.setParameter("station", station);
            query.setParameter("sedimentLoad", sedimentLoad);
            query.setParameter("value", value);

            List<SedimentLoadValue> values = query.list();
            if (values.isEmpty()) {
                peer = new SedimentLoadValue(sedimentLoad, station, value);
                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }

        return peer;
    }

    public void storeDependencies(SedimentLoad sedimentLoad) {
        getPeer(sedimentLoad);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
