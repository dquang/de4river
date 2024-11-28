/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend.utils;

import java.util.Date;
import java.util.Calendar;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class DateGuesser {
    public static final String [] MONTH = {
        "jan", "feb", "mrz", "apr", "mai", "jun",
        "jul", "aug", "sep", "okt", "nov", "dez"
    };

    public static final int guessMonth(String s) {
        s = s.toLowerCase();
        for (int i = 0; i < MONTH.length; ++i)
            if (MONTH[i].equals(s)) {
                return i;
            }
        return -1;
    }

    public static final Pattern YYYY_MM_DD =
        Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");

    public static final Pattern DD_MM_YYYY =
        Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})$");

    public static final Pattern MMM_YYYY =
        Pattern.compile("^(\\d{0,2})\\.?(\\w{3})\\.?(\\d{2,4})$");

    public static final Pattern GARBAGE_YYYY =
        Pattern.compile("^\\D*(\\d{2,4})$");

    public static final Pattern YYYY_MM_DDThh_mm =
        Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})$");

    public static final Pattern YYYY_MM_DDThh_mm_ss =
        Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})$");

    public static final Pattern DD_MM_YYYYThh_mm =
        Pattern.compile(
            "^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})T(\\d{1,2}):(\\d{2})$");

    public static final Pattern DD_MM_YYYYThh_mm_ss =
        Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})"
            + "T(\\d{1,2}):(\\d{2}):(\\d{2})$");

    public static final Pattern DDMMYY =
        Pattern.compile("^(\\d{2})(\\d{2})(\\d{2})$");

    private DateGuesser() {
    }

    public static final int calendarMonth(String month) {
        return calendarMonth(Integer.parseInt(month));
    }

    public static final int calendarMonth(int month) {
        return Math.max(Math.min(month-1, 11), 0);
    }

    /**
     * Guess date by trying all different patterns.
     * Throws IllegalArgumentException if not able to guess.
     * @param s The date to be guessed (e.g. 11.02.2001).
     * @return the parsed Date.
     */
    public static Date guessDate(String s) {
        if (s == null || (s = s.trim()).length() == 0) {
            throw new IllegalArgumentException();
        }

        Matcher m;

        m = YYYY_MM_DD.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year  = m.group(1);
            String month = m.group(2);
            String day   = m.group(3);
            cal.clear();
            cal.set(
                Integer.parseInt(year),
                calendarMonth(month),
                Integer.parseInt(day),
                12, 0, 0);
            return cal.getTime();
        }

        m = DD_MM_YYYY.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year  = m.group(3);
            String month = m.group(2);
            String day   = m.group(1);
            cal.clear();
            cal.set(
                Integer.parseInt(year) + (year.length() == 2 ? 1900 : 0),
                calendarMonth(month),
                Integer.parseInt(day),
                12, 0, 0);
            return cal.getTime();
        }

        m = MMM_YYYY.matcher(s);

        if (m.matches()) {
            int month = guessMonth(m.group(2));
            if (month >= 0) {
                Calendar cal = Calendar.getInstance();
                String year = m.group(3);
                String day  = m.group(1);
                cal.clear();
                cal.set(
                    Integer.parseInt(year) + (year.length() == 2 ? 1900 : 0),
                    month,
                    day.length() == 0 ? 15 : Integer.parseInt(day),
                    12, 0, 0);
                return cal.getTime();
            }
        }

        m = YYYY_MM_DDThh_mm.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year = m.group(1);
            String month = m.group(2);
            String day = m.group(3);
            String hour = m.group(4);
            String minute = m.group(5);
            cal.clear();
            cal.set(
                Integer.parseInt(year),
                calendarMonth(month),
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                0
            );
            return cal.getTime();
        }

        m = YYYY_MM_DDThh_mm_ss.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year = m.group(1);
            String month = m.group(2);
            String day = m.group(3);
            String hour = m.group(4);
            String minute = m.group(5);
            String second = m.group(6);
            cal.clear();
            cal.set(
                Integer.parseInt(year),
                calendarMonth(month),
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                Integer.parseInt(second)
            );
            return cal.getTime();
        }

        m = DD_MM_YYYYThh_mm.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year = m.group(3);
            String month = m.group(2);
            String day = m.group(1);
            String hour = m.group(4);
            String minute = m.group(5);
            cal.clear();
            cal.set(
                Integer.parseInt(year) + (year.length() == 2 ? 1900 : 0),
                calendarMonth(month),
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                0
            );
            return cal.getTime();
        }

        m = DD_MM_YYYYThh_mm_ss.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year = m.group(3);
            String month = m.group(2);
            String day = m.group(1);
            String hour = m.group(4);
            String minute = m.group(5);
            String second = m.group(6);
            cal.clear();
            cal.set(
                Integer.parseInt(year) + (year.length() == 2 ? 1900 : 0),
                calendarMonth(month),
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                Integer.parseInt(second)
            );
            return cal.getTime();
        }

        m = DDMMYY.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String day   = m.group(1);
            String month = m.group(2);
            String yearS = m.group(3);
            int year = Integer.parseInt(yearS);

            if (year <= cal.get(Calendar.YEAR) % 100) {
                year += 2000;
            }
            else {
                year += 1900;
            }
            cal.clear();
            cal.set(
                year,
                Integer.parseInt(month),  // month
                Integer.parseInt(day), // day
                12, 0, 0);
            return cal.getTime();
        }

        m = GARBAGE_YYYY.matcher(s);

        if (m.matches()) {
            Calendar cal = Calendar.getInstance();
            String year = m.group(1);
            cal.clear();
            cal.set(
                Integer.parseInt(year) + (year.length() == 2 ? 1900 : 0),
                5,  // month
                15, // day
                12, 0, 0);
            return cal.getTime();
        }

        throw new IllegalArgumentException();
    }

    public static void main(String [] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.println(args[i] + ": " + guessDate(args[i]));
        }
    }
}
// end of file
