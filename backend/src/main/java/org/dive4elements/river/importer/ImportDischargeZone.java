/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.DischargeZone;
import org.dive4elements.river.model.River;


public class ImportDischargeZone {

    private static final Logger log =
        LogManager.getLogger(ImportDischargeZone.class);


    private String gaugeName;

    private BigDecimal value;

    private String lowerDischarge;
    private String upperDischarge;

    private DischargeZone peer;


    public ImportDischargeZone(
        String     gaugeName,
        BigDecimal value,
        String     lowerDischarge,
        String     upperDischarge
    ) {
        this.gaugeName      = gaugeName;
        this.value          = value;
        this.lowerDischarge = lowerDischarge;
        this.upperDischarge = upperDischarge;
    }


    public void storeDependencies(River river) {
        log.debug("store dependencies");

        getPeer(river);
    }


    public DischargeZone getPeer(River river) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from DischargeZone where " +
                "   river=:river and " +
                "   gaugeName=:gaugeName and " +
                "   value=:value"
            );

            query.setParameter("river", river);
            query.setParameter("gaugeName", gaugeName);
            query.setParameter("value", value);

            List<DischargeZone> zone = query.list();

            if (zone.isEmpty()) {
                peer = new DischargeZone(
                    river,
                    gaugeName,
                    value,
                    lowerDischarge,
                    upperDischarge);

                session.save(peer);
            }
            else {
                peer = zone.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
