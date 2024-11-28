/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.MorphologicalWidth;
import org.dive4elements.river.model.River;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;


public class ImportMorphWidth {

    private static Logger log = LogManager.getLogger(ImportMorphWidth.class);

    protected MorphologicalWidth peer;

    protected ImportUnit unit;

    protected List<ImportMorphWidthValue> values;

    public ImportMorphWidth() {
        this.values = new ArrayList<ImportMorphWidthValue>();
    }

    public void addValue(ImportMorphWidthValue value) {
        this.values.add(value);
    }

    public void setUnit(ImportUnit unit) {
        this.unit = unit;
    }

    public void storeDependencies(River river) {
        log.info("store dependencies");

        MorphologicalWidth peer = getPeer(river);

        if (peer != null) {
            log.info("store morphological width values");

            for (ImportMorphWidthValue value : values) {
                value.storeDependencies(peer);
            }
        }
    }

    public MorphologicalWidth getPeer(River river) {
        log.info("get peer");

        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery("from MorphologicalWidth where "
                + "   river=:river and " + "   unit=:unit");

            query.setParameter("river", river);
            query.setParameter("unit", unit.getPeer());

            List<MorphologicalWidth> widths = query.list();

            if (widths.isEmpty()) {
                log.debug("Create new MorphologicalWidth DB instance.");

                peer = new MorphologicalWidth(river, unit.getPeer());

                session.save(peer);
            }
            else {
                peer = widths.get(0);
            }
        }

        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
