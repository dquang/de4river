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

import org.dive4elements.river.model.Depth;


public class ImportDepth {

    private static Logger log = LogManager.getLogger(ImportDepth.class);


    protected Depth peer;

    protected BigDecimal lower;
    protected BigDecimal upper;


    public ImportDepth(BigDecimal lower, BigDecimal upper) {
        this.lower = lower;
        this.upper = upper;
    }


    public void storeDependencies() {
        log.info("store dependencies");

        getPeer();
    }


    public Depth getPeer() {
        log.info("get peer");

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from Depth where "
                + "lower between :lower - 0.0001f and :lower + 0.00001f and "
                + "upper between :upper - 0.0001f and :upper + 0.00001f");

            query.setParameter("lower", lower.floatValue());
            query.setParameter("upper", upper.floatValue());

            List<Depth> depths = query.list();

            if (depths.isEmpty()) {
                log.debug("Create new Depth DB instance.");

                peer = new Depth(lower, upper);

                session.save(peer);
            }
            else {
                peer = depths.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
