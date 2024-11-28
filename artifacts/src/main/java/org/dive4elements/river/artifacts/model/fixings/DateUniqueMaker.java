/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import java.util.Date;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongHashSet;

public class DateUniqueMaker {

    private TLongHashSet      times;
    private TIntObjectHashMap already;

    public DateUniqueMaker() {
        times   = new TLongHashSet();
        already = new TIntObjectHashMap();
    }

    public <T extends QWI> void makeUnique(T t) {

        // Map same index to same new value
        if (already.containsKey(t.index)) {
            t.date = (Date)already.get(t.index);
            return;
        }
        long time = t.date.getTime();
        if (!times.add(time)) { // same found before
            do {
                time += 30L*1000L; // Add 30secs
            }
            while (!times.add(time));
            Date newDate = new Date(time);
            already.put(t.index, newDate);
            // Write back modified time.
            t.date = newDate;
        }
        else {
            // register as seen.
            already.put(t.index, t.date);
        }
    }
}
