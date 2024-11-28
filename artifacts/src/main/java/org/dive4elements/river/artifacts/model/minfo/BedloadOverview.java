/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

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
import org.dive4elements.river.artifacts.model.RiverFactory;

public class BedloadOverview implements Serializable {

    private static Logger log = LogManager.getLogger(BedloadOverview.class);

    /**
     * Serial version UId.
     */
    private static final long serialVersionUID = -7607668985959407096L;

    public interface Filter {
        boolean accept(KMIndex<List<Date>> entry);

    } // interface Filter


    public static final Filter ACCEPT = new Filter() {
        public boolean accept(KMIndex<List<Date>> entry) {
            return true;
        }
    };

    public static class KmFilter implements Filter {

        protected double km;

        public KmFilter (double km) {
            this.km = km;
        }
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
        "    m.km    AS km," +
        "    m.datum AS datum " +
        "FROM messung m " +
        "    JOIN station s" +
        "       ON m.stationid = s.stationid " +
        "    JOIN gewaesser g " +
        "       ON s.gewaesserid = g.gewaesserid " +
        "WHERE" +
        "    g.name = :name" +
        "    AND m.km IS NOT NULL" +
        "    AND m.km BETWEEN :from AND :to " +
        "    AND m.d10 IS NOT NULL " +
        "ORDER BY m.km, m.datum";

    protected String riverName;
    protected String SeddbRiverName;

    protected KMIndex<List<Date>> entries;

    public BedloadOverview() {
        entries = new KMIndex<List<Date>>();
    }

    public BedloadOverview(String riverName) {
        this();
        this.riverName = riverName;
        this.SeddbRiverName = RiverFactory.getRiver(riverName).nameForSeddb();
    }

    private static final boolean epsilonEquals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    protected void loadData(Session session) {
        double [] fromTo = RiverFactory.getRiver(riverName)
            .determineMinMaxDistance();

        SQLQuery query = session.createSQLQuery(SQL_SQ)
            .addScalar("km",    StandardBasicTypes.DOUBLE)
            .addScalar("datum", StandardBasicTypes.DATE);

        query.setString("name", SeddbRiverName);
        query.setDouble("from", fromTo[0]);
        query.setDouble("to", fromTo[1]);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("No river '" + SeddbRiverName + "' found.");
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

        Element sqElement = document.createElement("bedload");

        Element riverElement = document.createElement("river");

        riverElement.setAttribute("name", SeddbRiverName);

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
