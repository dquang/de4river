/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.type.StandardBasicTypes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.river.utils.KMIndex;

public class SQOverview
implements   Serializable
{
    private static Logger log = LogManager.getLogger(SQOverview.class);

    /**
     * Serial version UId.
     */
    private static final long serialVersionUID = -8934372438968398508L;

    public interface Filter {

        boolean accept(KMIndex<List<Date>> entry);

    } // interface Filter


    public static final Filter ACCEPT = new Filter() {
        @Override
        public boolean accept(KMIndex<List<Date>> entry) {
            return true;
        }
    };

    public static class KmFilter implements Filter {

        protected double km;

        public KmFilter (double km) {
            this.km = km;
        }
        @Override
        public boolean accept(KMIndex<List<Date>> list) {
            for (KMIndex.Entry<List<Date>> e: list){
                if (e.getKm() == km) {
                    return true;
                }
            }
            return false;
        }
    };

    public static class DateFilter implements Filter {

        protected Date date;

        public DateFilter (Date date) {
            this.date = date;
        }
        @Override
        public boolean accept(KMIndex<List<Date>> list) {
            for (KMIndex.Entry<List<Date>> e: list){
                if (e.getValue().equals(this.date)) {
                    return true;
                }
            }
            return false;
        }
    };

    public static final double EPSILON = 1e-4;

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static final String SQL_SQ =
        "SELECT" +
        "    s.km    AS km," +
        "    m.datum AS datum " +
        "FROM messung m " +
        "    JOIN station s" +
        "       ON m.stationid = s.stationid " +
        "    JOIN gewaesser g " +
        "       ON s.gewaesserid = g.gewaesserid " +
        "WHERE" +
        "    m.q_bpegel IS NOT NULL AND" +
        "    g.name = :name " +
        "ORDER by" +
        "    s.km, m.datum";

    protected String       riverName;

    protected KMIndex<List<Date>> entries;

    public SQOverview() {
        entries = new KMIndex<List<Date>>();
    }

    public SQOverview(String riverName) {
        this();
        this.riverName = RiverFactory.getRiver(riverName).nameForSeddb();
    }

    private static final boolean epsilonEquals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    protected void loadData(Session session) {
        SQLQuery query = session.createSQLQuery(SQL_SQ)
            .addScalar("km",    StandardBasicTypes.DOUBLE)
            .addScalar("datum", StandardBasicTypes.DATE);

        query.setString("name", riverName);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("No river '" + riverName + "' found.");
        }

        Double prevKm = -Double.MAX_VALUE;
        List<Date> dates = new ArrayList<Date>();

        for (Object [] row: list) {
            Double km = (Double)row[0];
            if (!epsilonEquals(km, prevKm) && !dates.isEmpty()) {
                entries.add(prevKm, dates);
                dates = new ArrayList<Date>();
            }
            dates.add((Date)row[1]);
            prevKm = km;
        }

        if (!dates.isEmpty()) {
            entries.add(prevKm, dates);
        }
    }

    public boolean load(Session session) {

        loadData(session);

        return true;
    }


    public void generateOverview(Document document) {
        generateOverview(document, ACCEPT);
    }

    public KMIndex<List<Date>> filter(Filter f) {
        // TODO: Apply filter
        return entries;
    }

    public void generateOverview(
        Document document,
        Filter   filter
    ) {
        KMIndex<List<Date>> filtered = filter(ACCEPT);

        Element sqElement = document.createElement("sq");

        Element riverElement = document.createElement("river");

        riverElement.setAttribute("name", riverName);

        sqElement.appendChild(riverElement);

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

        Element kmE = document.createElement("km");

        for (KMIndex.Entry<List<Date>> e: filtered) {

            List<Date> dates = e.getValue();

            if (!dates.isEmpty()) {
                Element dEs = document.createElement("dates");

                for (Date d: dates) {
                    Element dE = document.createElement("date");

                    dE.setAttribute("value", df.format(d));

                    dEs.appendChild(dE);
                }

                kmE.appendChild(dEs);
            }
        }

        sqElement.appendChild(kmE);

        document.appendChild(sqElement);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
