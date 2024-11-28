/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.math.BigDecimal;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;

import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.Annotation;
import org.dive4elements.river.model.River;

import org.hibernate.Session;
import org.hibernate.Query;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AnnotationsFactory {

    /**
     * Get Annotations which do not have a "b" ("to")-value set.
     *
     * @param river name of the river of interest.
     *
     * @return List of Annotations for river which have only "a" ("from")
     *          value set.
     */
    public static List<Annotation> getPointAnnotations(String river) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Annotation as an " +
            "where an.range.b = null and an.range.river.name=:name " +
            "order by range.a");
        query.setParameter("name", river);
        return query.list();
    }


    public static List<Annotation> getAnnotations(River river) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Annotation as an where an.range.river = :river" +
            " order by an.range.a");
        query.setParameter("river", river);
        return query.list();
    }


    public static Annotation getAnnotation(String river, double km) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Annotation as a " +
            "where a.range.river.name = :river AND a.range.a = :km");

        query.setParameter("river", river);
        query.setParameter("km", BigDecimal.valueOf(km));

        List<Annotation> result = query.list();

        return result != null && result.size() > 0 ? result.get(0) : null;
    }


    /**
     * Get minimal "a" ("from") and maximal "b" ("to") value of annotations'
     * ranges of a river.
     *
     * @param river name of the river of interest.
     *
     * @return Array containing minimal "a" and max "b" value of any
     *         annotation stored for the given river.
     */
    public static double[] getAnnotationsBreadth(String river) {
        Session session = SessionHolder.HOLDER.get();

        Query minAQuery = session.createQuery(
            "select min(a), max(b) from Range where river.name=:name");
        minAQuery.setParameter("name", river);

        double[] minAmaxB = {0.0f, 0.0f};
        Object[] row = (Object[]) minAQuery.list().iterator().next();
        minAmaxB[0] = ((BigDecimal) row[0]).doubleValue();
        minAmaxB[1] = ((BigDecimal) row[1]).doubleValue();
        return minAmaxB;
    }


    public static Iterator<Annotation> getAnnotationsIterator(
        String riverName
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query riverQuery = session.createQuery(
            "from River where name = :name");
        riverQuery.setParameter("name", riverName);
        List<River> rivers = riverQuery.list();
        if (rivers.isEmpty()) {
            return Collections.<Annotation>emptyList().iterator();
        }

        Query query = session.createQuery(
            "from Annotation as an" +
            " where an.range.river = :river order by an.range.a");
        query.setParameter("river", rivers.get(0));

        return (Iterator<Annotation>)query.iterate();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
