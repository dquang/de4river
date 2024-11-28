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

import org.dive4elements.river.model.MorphologicalWidth;
import org.dive4elements.river.model.MorphologicalWidthValue;


public class ImportMorphWidthValue {

    private static Logger log = LogManager.getLogger(ImportMorphWidthValue.class);


    protected MorphologicalWidthValue peer;

    protected BigDecimal station;
    protected BigDecimal width;

    protected String description;


    public ImportMorphWidthValue(
        BigDecimal station,
        BigDecimal width,
        String     description
    ) {
        this.station     = station;
        this.width       = width;
        this.description = description;
    }


    public void storeDependencies(MorphologicalWidth parent) {
        getPeer(parent);
    }


    public MorphologicalWidthValue getPeer(MorphologicalWidth parent) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from MorphologicalWidthValue where " +
                "   morphologicalWidth=:morphologicalWidth and " +
                "   station=:station and " +
                "   width=:width and " +
                "   description=:description");

            query.setParameter("morphologicalWidth", parent);
            query.setParameter("station", station);
            query.setParameter("width", width);
            query.setParameter("description", description);

            List<MorphologicalWidthValue> values = query.list();

            if (values.isEmpty()) {
                peer = new MorphologicalWidthValue(
                    parent,
                    station,
                    width,
                    description
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
