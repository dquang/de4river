/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SQLQuery;

import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.backend.SessionHolder;

public class FastAnnotations
implements   Serializable
{
    public static final String SQL_BY_RIVER_NAME =
        "SELECT r.a AS a, r.b AS b, p.value AS position, " +
                "at.value AS attribute, ant.name AS name, " +
                "e.top AS top, e.bottom AS bottom " +
        "FROM annotations an " +
            "JOIN ranges r " +
                "ON an.range_id = r.id " +
            "JOIN attributes at " +
                "ON an.attribute_id = at.id " +
            "JOIN positions p " +
                "ON an.position_id = p.id " +
            "JOIN rivers riv " +
                "ON r.river_id = riv.id " +
            "LEFT JOIN annotation_types ant " +
                "ON an.type_id = ant.id " +
            "LEFT JOIN edges e " +
                "ON an.edge_id = e.id " +
            "WHERE riv.name = :river_name " +
                "ORDER BY r.a";

    public static final String SQL_BY_RIVER_ID =
        "SELECT r.a AS a, r.b AS b, p.value AS position, " +
                "at.value AS attribute, ant.name AS name, " +
                "e.top AS top, e.bottom AS bottom " +
        "FROM annotations an " +
            "JOIN ranges r " +
                "ON an.range_id = r.id " +
            "JOIN attributes at " +
                "ON an.attribute_id = at.id " +
            "JOIN positions p " +
                "ON an.position_id = p.id " +
            "LEFT JOIN annotation_types ant " +
                "ON an.type_id = ant.id " +
            "LEFT JOIN edges e " +
                "ON an.edge_id = e.id " +
            "WHERE r.id = :river_id " +
                "ORDER BY r.a";

    public static final double EPSILON = 1e-5;

    public static final Comparator<Annotation> KM_CMP =
        new Comparator<Annotation>() {
            @Override
            public int compare(Annotation a, Annotation b) {
                double diff = a.a - b.a;
                if (diff < -EPSILON) return -1;
                if (diff > +EPSILON) return +1;
                return 0;
            }
        };

    public static final class Annotation
    implements                Serializable
    {
        private double a;
        private double b;
        private String position;
        private String attribute;
        private String name;
        private double top;
        private double bottom;

        public Annotation() {
        }

        public Annotation(double a) {
            this.a = a;
        }

        public Annotation(
            double a,
            double b,
            String position,
            String attribute,
            String name,
            double top,
            double bottom
        ) {
            this.a         = a;
            this.b         = b;
            this.position  = position;
            this.attribute = attribute;
            this.name      = name;
            this.top       = top;
            this.bottom    = bottom;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public String getPosition() {
            return position;
        }

        public String getAttribute() {
            return attribute;
        }

        public String getName() {
            return name;
        }

        public double getTop() {
            return top;
        }

        public double getBottom() {
            return bottom;
        }

        @Override
        public String toString() {
            return "[a=" + a + ";b=" + b +
                ";pos=" + position + ";attr=" + attribute +
                ";name=" + name + ";top=" + top +
                ";bot=" + bottom + "]";
        }
    } // class Annotation

    public interface Filter {

        boolean accept(Annotation annotation);

    } // interface Filter

    public static class NameFilter implements Filter {

        private Pattern namePattern;

        public NameFilter(String name) {
            this.namePattern = Pattern.compile(name);
        }

        @Override
        public boolean accept(Annotation annotation) {
            return namePattern.matcher(annotation.getName()).matches();
        }
    } // class NameFilter

    public static final Filter ALL = new Filter() {
        @Override
        public boolean accept(Annotation annotation) {
            return true;
        }
    };

    public static final Filter IS_POINT = new Filter() {
        @Override
        public boolean accept(Annotation annotation) {
            return Double.isNaN(annotation.getB());
        }
    };

    public static final Filter IS_RANGE = new Filter() {
        @Override
        public boolean accept(Annotation annotation) {
            return !Double.isNaN(annotation.getB());
        }
    };

    private Annotation [] annotations;

    public FastAnnotations() {
    }

    public FastAnnotations(Annotation [] annotations) {
        this.annotations = annotations;
    }

    public FastAnnotations(String riverName) {
        this(loadByRiverName(riverName));
    }

    public FastAnnotations(int riverId) {
        this(loadByRiverId(riverId));
    }

    public FastAnnotations(Iterator<Annotation> iter) {
        this(toArray(iter));
    }

    public int size() {
        return annotations.length;
    }

    public Iterator<Annotation> filter(final Filter filter) {
        return new Iterator<Annotation>() {

            private int idx;
            private Annotation current = findNext();

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Annotation next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                Annotation result = current;
                current = findNext();
                return result;
            }

            private Annotation findNext() {

                while (idx < annotations.length) {
                    Annotation annotation = annotations[idx++];
                    if (filter.accept(annotation)) {
                        return annotation;
                    }
                }

                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static Annotation [] toArray(Iterator<Annotation> iter) {

        ArrayList<Annotation> list = new ArrayList<Annotation>();

        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return list.toArray(new Annotation[list.size()]);
    }

    public Annotation findByKm(double km) {
        Annotation key = new Annotation(km);
        int idx = Arrays.binarySearch(annotations, key, KM_CMP);
        return idx < 0 ? null : annotations[idx];
    }

    private static SQLQuery createQuery(String query) {
        Session session = SessionHolder.HOLDER.get();

        return session.createSQLQuery(query)
            .addScalar("a",         StandardBasicTypes.DOUBLE)
            .addScalar("b",         StandardBasicTypes.DOUBLE)
            .addScalar("position",  StandardBasicTypes.STRING)
            .addScalar("attribute", StandardBasicTypes.STRING)
            .addScalar("name",      StandardBasicTypes.STRING)
            .addScalar("top",       StandardBasicTypes.DOUBLE)
            .addScalar("bottom",    StandardBasicTypes.DOUBLE);
    }

    private static Annotation [] buildAnnotations(List<Object []> list) {
        Annotation [] anns = new Annotation[list.size()];

        // Names are likely the same because they are a type
        // like 'Pegel' or 'Hafen'.
        HashMap<String, String> names = new HashMap<String, String>();

        for (int i = 0; i < anns.length; ++i) {
            Object [] data   = list.get(i);
            double a         = ((Double)data[0]);
            double b         = data[1] != null ? (Double)data[1] : Double.NaN;
            String position  = (String)data[2];
            String attribute = (String)data[3];
            String name      = (String)data[4];
            double top       = data[5] != null ? (Double)data[5] : Double.NaN;
            double bottom    = data[6] != null ? (Double)data[6] : Double.NaN;

            if (name != null) {
                String old = names.get(name);
                if (old != null) {
                    name = old;
                }
                else {
                    names.put(name, name);
                }
            }

            anns[i] = new Annotation(
                a, b, position, attribute, name, top, bottom);
        }

        return anns;
    }

    public static Annotation [] loadByRiverName(String riverName) {

        SQLQuery query = createQuery(SQL_BY_RIVER_NAME);

        query.setString("river_name", riverName);

        return buildAnnotations(query.list());
    }

    public static Annotation [] loadByRiverId(int riverId) {

        SQLQuery query = createQuery(SQL_BY_RIVER_ID);

        query.setInteger("river_id", riverId);

        return buildAnnotations(query.list());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
