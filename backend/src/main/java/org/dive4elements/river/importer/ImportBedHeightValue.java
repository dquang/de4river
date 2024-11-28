/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightValue;


public class ImportBedHeightValue {

    private static final Logger log =
        LogManager.getLogger(ImportBedHeightValue.class);


    protected ImportBedHeight bedHeight;

    protected Double station;
    protected Double height;
    protected Double uncertainty;
    protected Double dataGap;
    protected Double soundingWidth;

    protected BedHeightValue peer;


    public ImportBedHeightValue(
        ImportBedHeight bedHeight,
        Double station,
        Double height,
        Double uncertainty,
        Double dataGap,
        Double soundingWidth
    ) {
        this.bedHeight     = bedHeight;
        this.station       = station;
        this.height        = height;
        this.uncertainty   = uncertainty;
        this.dataGap       = dataGap;
        this.soundingWidth = soundingWidth;
    }


    public void storeDependencies(BedHeight bedHeight) {
        getPeer(bedHeight);
    }


    /**
     * Add this value to database or return database bound Value, assuring
     * that the BedHeight exists in db already.
     */
    public BedHeightValue getPeer(BedHeight bedHeight) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from BedHeightValue where " +
                "   bedHeight=:bedHeight and " +
                "   station=:station");

            query.setParameter("bedHeight", bedHeight);
            query.setParameter("station", station);

            List<BedHeightValue> values = query.list();
            if (values.isEmpty()) {
                peer = new BedHeightValue(
                    bedHeight,
                    station,
                    height,
                    uncertainty,
                    dataGap,
                    soundingWidth
                );
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
