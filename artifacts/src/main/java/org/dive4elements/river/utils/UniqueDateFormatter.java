/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class UniqueDateFormatter {

    private static Logger log = LogManager.getLogger(UniqueDateFormatter.class);

    private DateFormat df;
    private DateFormat lf;
    private Map<String, int[]> collisions;

    public UniqueDateFormatter(
        DateFormat df,
        DateFormat lf,
        Collection<Date> dates
    ) {
        this.df = df;
        this.lf = lf;
        collisions = build(dates);
    }

    private Map<String, int []> build(Collection<Date> dates) {
        Map<String, int []> collisions = new HashMap<String, int[]>();
        for (Date d: dates) {
            String s = df.format(d);
            int [] count = collisions.get(s);
            if (count == null) {
                collisions.put(s, count = new int[1]);
            }
            if (++count[0] > 1) {
                log.debug("date collsion found: " + d);
            }
        }
        return collisions;
    }

    public String format(Date date) {
        String s = df.format(date);
        int [] count = collisions.get(s);
        return count == null || count[0] < 2
            ? s
            : lf.format(date);
    }
}
