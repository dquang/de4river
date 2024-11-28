/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.HYKFormation;
import org.dive4elements.river.model.HYKFlowZone;
import org.dive4elements.river.model.HYKFlowZoneType;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;

import java.math.BigDecimal;

public class ImportHYKFlowZone
{
    protected ImportHYKFormation    formation;
    protected ImportHYKFlowZoneType type;
    protected BigDecimal            a;
    protected BigDecimal            b;

    protected HYKFlowZone peer;

    public ImportHYKFlowZone() {
    }

    public ImportHYKFlowZone(
        ImportHYKFormation    formation,
        ImportHYKFlowZoneType type,
        BigDecimal            a,
        BigDecimal            b
    ) {
        this.formation = formation;
        this.type      = type;
        this.a         = a;
        this.b         = b;
    }

    public ImportHYKFormation getFormation() {
        return formation;
    }

    public void setFormation(ImportHYKFormation formation) {
        this.formation = formation;
    }

    public void storeDependencies() {
        getPeer();
    }

    public HYKFlowZone getPeer() {
        if (peer == null) {
            HYKFormation    f = formation.getPeer();
            HYKFlowZoneType t = type.getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from HYKFlowZone where formation=:formation " +
                "and type=:type and a=:a and b=:b");
            query.setParameter("formation", f);
            query.setParameter("type", t);
            query.setParameter("a", a);
            query.setParameter("b", b);
            List<HYKFlowZone> zones = query.list();
            if (zones.isEmpty()) {
                peer = new HYKFlowZone(f, t, a, b);
                session.save(peer);
            }
            else {
                peer = zones.get(0);
            }

        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
