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

import org.dive4elements.river.model.Porosity;
import org.dive4elements.river.model.PorosityValue;


public class ImportPorosityValue {

    private static final Logger log =
        LogManager.getLogger(ImportPorosityValue.class);


    protected PorosityValue peer;

    protected BigDecimal station;

    protected BigDecimal shoreOffset;

    protected BigDecimal porosity;

    protected String description;


    public ImportPorosityValue(
        BigDecimal station,
        BigDecimal shoreOffset,
        BigDecimal porosity,
        String     description
    ) {
        this.station     = station;
        this.shoreOffset = shoreOffset;
        this.porosity    = porosity;
        this.description = description;
    }


    public void storeDependencies(Porosity porosity) {
        log.info("store dependencies");

        getPeer(porosity);
    }


    public PorosityValue getPeer(Porosity porosity) {
        log.info("get peer");

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from PorosityValue "
                + "where porosity=:porosity "
                + "and station between :station - 0.0001f "
                + "    and :station + 0.0001f "
                + "and porosityValue between :poros -0.0001f "
                + "    and :poros + 0.0001f "
                + "and description=:description");

            query.setParameter("porosity", porosity);
            query.setParameter("station", station.floatValue());
            query.setParameter("poros", this.porosity.floatValue());
            query.setParameter("description", description);

            List<PorosityValue> values = query.list();
            if (values.isEmpty()) {
                log.debug("Create new PorosityValue DB instance.");

                peer = new PorosityValue(
                    porosity,
                    station,
                    shoreOffset,
                    this.porosity,
                    description);

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
