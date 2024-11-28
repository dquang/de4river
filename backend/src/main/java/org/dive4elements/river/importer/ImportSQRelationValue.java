/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.SQRelation;
import org.dive4elements.river.model.SQRelationValue;


public class ImportSQRelationValue {

    private static Logger log = LogManager.getLogger(ImportSQRelationValue.class);


    private SQRelationValue peer;

    private String parameter;
    private MeasurementStation station;
    private Double a;
    private Double b;
    private Double qMax;
    private Double rSQ;
    private Integer nTot;
    private Integer nOutlier;
    private Double cFerguson;
    private Double cDuan;


    public ImportSQRelationValue(
        String parameter,
        MeasurementStation station,
        Double a,
        Double b,
        Double qMax,
        Double rSQ,
        Integer nTot,
        Integer nOutlier,
        Double cFerguson,
        Double cDuan
    ) {
        this.parameter = parameter;
        this.station   = station;
        this.a         = a;
        this.b         = b;
        this.qMax      = qMax;
        this.rSQ       = rSQ;
        this.nTot      = nTot;
        this.nOutlier  = nOutlier;
        this.cFerguson = cFerguson;
        this.cDuan     = cDuan;
    }


    public void storeDependencies(SQRelation owner)
    throws SQLException, ConstraintViolationException
    {
        getPeer(owner);
    }


    public SQRelationValue getPeer(SQRelation owner) {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery(
                "from SQRelationValue " +
                "   where sqRelation=:owner " +
                "   and measurementStation=:measurementStation" +
                "   and parameter=:parameter");

            query.setParameter("owner", owner);
            query.setParameter("measurementStation", station);
            query.setString("parameter", parameter);

            List<SQRelationValue> values = query.list();

            if (values.isEmpty()) {
                peer = new SQRelationValue(
                    owner,
                    parameter,
                    station,
                    a,
                    b,
                    qMax,
                    rSQ,
                    nTot,
                    nOutlier,
                    cFerguson,
                    cDuan
                );

                session.save(peer);
            }
            else {
                peer = values.get(0);
            }
        }
        return peer;
    }

    private static final BigDecimal toBigDecimal(Double x) {
        if (x == null) return null;
        return new BigDecimal(x);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
