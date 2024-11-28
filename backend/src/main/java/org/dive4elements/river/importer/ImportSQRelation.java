/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import org.dive4elements.river.model.SQRelation;
import org.dive4elements.river.model.TimeInterval;


public class ImportSQRelation {

    private static Logger log = LogManager.getLogger(ImportSQRelation.class);

    private ImportTimeInterval timeInterval;

    private String description;

    private List<ImportSQRelationValue> values;

    private SQRelation peer;

    public ImportSQRelation() {
        this.values = new ArrayList<ImportSQRelationValue>();
    }

    public void storeDependencies() {
        SQRelation peer = getPeer();

        if (peer != null) {
            int count = 0;

            for (ImportSQRelationValue value : values) {
                try {
                    value.storeDependencies(peer);
                    count++;
                }
                catch (SQLException sqle) {
                    log.warn("ISQ: Unable to store sq relation value.", sqle);
                }
                catch (ConstraintViolationException cve) {
                    log.warn("ISQ: Unable to store sq relation value.", cve);
                }
            }

            log.info("stored " + count + " sq relation values.");
        }
    }

    public SQRelation getPeer() {
        if (peer == null) {
            TimeInterval timeInter = timeInterval.getPeer();

            if (timeInter == null) {
                log.warn(
                    "ISQ: Cannot determine sq relation without time interval");
                return null;
            }

            Session session = ImporterSession.getInstance()
                .getDatabaseSession();

            Query query = session.createQuery(
                "from SQRelation where " +
                "    description = :description and " +
                "    timeInterval=:timeInter");

            query.setParameter("description", description);
            query.setParameter("timeInter", timeInter);

            List<SQRelation> sq = query.list();

            if (sq.isEmpty()) {
                log.info("create new SQ relation '" + description + "'");

                peer = new SQRelation(timeInter, description);
                session.save(peer);
            }
            else {
                peer = sq.get(0);
            }
        }

        return peer;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimeInterval(ImportTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void addValue(ImportSQRelationValue value) {
        if (value != null) {
            this.values.add(value);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
