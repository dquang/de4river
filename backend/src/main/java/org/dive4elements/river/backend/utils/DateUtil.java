/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend.utils;

import java.util.Date;
import java.util.Calendar;

public final class DateUtil {

    private DateUtil() {
    }

    /** Create Date on first moment (1st jan) of given year. */
    public static Date getStartDateFromYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, 0, 1, 0, 0, 0);

        return cal.getTime();
    }


    /** Create Date on last moment (31st dec) of given year. */
    public static Date getEndDateFromYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, 11, 31, 23, 59, 59);

        return cal.getTime();
    }
}
