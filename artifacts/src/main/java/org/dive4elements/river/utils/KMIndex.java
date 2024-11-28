/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import java.io.Serializable;

/** km to value, searchable. Tolerance is at 10cm. */
public class KMIndex<A>
implements   Serializable, Iterable<KMIndex.Entry<A>>
{
    public static final double EPSILON = 1e-4;

    public static class Entry<A>
    implements          Serializable, Comparable<Entry<A>>
    {
        protected double km;
        protected A      value;

        public Entry(double km) {
            this.km = km;
        }

        public Entry(double km, A value) {
            this.km    = km;
            this.value = value;
        }

        public double getKm() {
            return km;
        }

        public A getValue() {
            return value;
        }

        public void setValue(A value) {
            this.value = value;
        }

        @Override
        public int compareTo(Entry<A> other) {
            double diff = km - other.km;
            if (diff < -EPSILON) return -1;
            if (diff > +EPSILON) return +1;
            return 0;
        }

        public boolean epsilonEquals(double km) {
            return Math.abs(this.km - km) < EPSILON;
        }
    } // class Entry


    protected List<Entry<A>> entries;

    public KMIndex() {
        this(10);
    }

    public KMIndex(int capacity) {
        entries = new ArrayList<Entry<A>>(capacity);
    }

    public void add(double km, A value) {
        entries.add(new Entry<A>(km, value));
    }

    public void sort() {
        Collections.sort(entries);
    }

    public int size() {
        return entries.size();
    }

    public Entry<A> get(int idx) {
        return entries.get(idx);
    }

    /** Return the first entry at km. */
    public Entry<A> search(double km) {
        for (Entry<A> entry: entries) {
            if (entry.epsilonEquals(km)) {
                return entry;
            }
        }
        return null;
    }

    public Entry<A> binarySearch(double km) {
        int index = Collections.binarySearch(entries, new Entry<A>(km));
        return index >= 0 ? entries.get(index) : null;
    }

    public Iterator<Entry<A>> iterator() {
        return entries.iterator();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
