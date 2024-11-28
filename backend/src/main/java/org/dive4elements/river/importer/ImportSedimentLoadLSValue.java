/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.SedimentLoadLS;
import org.dive4elements.river.model.SedimentLoadLSValue;


public class ImportSedimentLoadLSValue {

    private Double station;
    private Double value;

    private SedimentLoadLSValue peer;


    public ImportSedimentLoadLSValue(Double station, Double value) {
        this.station = station;
        this.value   = value;
    }


    public void storeDependencies(SedimentLoadLS sedimentLoadLS) {
        getPeer(sedimentLoadLS);
    }


    public SedimentLoadLSValue getPeer(SedimentLoadLS sedimentLoadLS) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from SedimentLoadLSValue where " +
                "   sedimentLoadLS=:sedimentLoadLS and " +
                "   station=:station and " +
                "   value=:value"
            );

            query.setParameter("sedimentLoadLS", sedimentLoadLS);
            query.setParameter("station", station);
            query.setParameter("value", value);

            List<SedimentLoadLSValue> values = query.list();
            if (values.isEmpty()) {
                peer = new SedimentLoadLSValue(sedimentLoadLS, station, value);
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
