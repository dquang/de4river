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

import org.dive4elements.river.model.SedimentDensity;
import org.dive4elements.river.model.SedimentDensityValue;


public class ImportSedimentDensityValue {

    private static final Logger log =
        LogManager.getLogger(ImportSedimentDensityValue.class);


    protected SedimentDensityValue peer;

    protected BigDecimal station;

    protected BigDecimal shoreOffset;

    protected BigDecimal density;

    private BigDecimal year;

    protected String description;


    public ImportSedimentDensityValue(
        BigDecimal station,
        BigDecimal shoreOffset,
        BigDecimal density,
        BigDecimal year,
        String     description
    ) {
        this.station     = station;
        this.shoreOffset = shoreOffset;
        this.density     = density;
        this.year        = year;
        this.description = description;
    }


    public void storeDependencies(SedimentDensity sedimentDensity) {
        log.info("store dependencies");

        getPeer(sedimentDensity);
    }


    public SedimentDensityValue getPeer(SedimentDensity sedimentDensity) {
        log.info("get peer");

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from SedimentDensityValue where " +
                "   sedimentDensity=:sedimentDensity and " +
                "   station=:station and " +
                "   shoreOffset=:shoreOffset and " +
                "   density=:density and " +
                "   year=:year and " +
                "   description=:description");

            query.setParameter("sedimentDensity", sedimentDensity);
            query.setParameter("station", station);
            query.setParameter("shoreOffset", shoreOffset);
            query.setParameter("density", density);
            query.setParameter("year", year);
            query.setParameter("description", description);

            List<SedimentDensityValue> values = query.list();
            if (values.isEmpty()) {
                log.debug("Create new SedimentDensityValue DB instance.");

                peer = new SedimentDensityValue(
                    sedimentDensity,
                    station,
                    shoreOffset,
                    density,
                    year,
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
