/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DIPSGauge
{
    private static Logger log = LogManager.getLogger(DIPSGauge.class);

    public static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{4})-(\\d{2})-(\\d{2})(?:\\s+|T)(\\d{2}):(\\d{2}):(\\d{2})");

    public static final Comparator<Datum> DATE_CMP = new Comparator<Datum>() {
        public int compare(Datum a, Datum b) {
            return a.date.compareTo(b.date);
        }
    };

    public static class Datum {

        protected double value;
        protected Date   date;

        public Datum() {
        }

        public Datum(Element element) {
            value = Double.parseDouble(element.getAttribute("WERT"));
            String dateString = element.getAttribute("GUELTIGAB");
            if (dateString.length() == 0) {
                throw
                    new IllegalArgumentException("missing GUELTIGAB attribute");
            }
            Matcher m = DATE_PATTERN.matcher(dateString);
            if (!m.matches()) {
                throw
                    new IllegalArgumentException("GUELTIGAB does not match");
            }

            int year  = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int day   = Integer.parseInt(m.group(3));
            int hours = Integer.parseInt(m.group(4));
            int mins  = Integer.parseInt(m.group(5));
            int secs  = Integer.parseInt(m.group(6));

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day, hours, mins, secs);

            date = cal.getTime();
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    } // class datum

    protected double aeo;

    protected double station;

    protected String name;

    protected String riverName;

    protected List<Datum> datums;

    protected int flysId;

    protected String aftName;

    protected Long   officialNumber;

    public DIPSGauge() {
    }

    public DIPSGauge(Element element) {

        name          = element.getAttribute("NAME");
        riverName     = element.getAttribute("GEWAESSER");

        String aeoString = element.getAttribute("EINZUGSGEBIET_AEO");
        if (aeoString.length() == 0) {
            log.warn("DIPS: Setting AEO of gauge '" + name + "' to zero.");
            aeoString = "0";
        }
        aeo = Double.parseDouble(aeoString);

        String stationString = element.getAttribute("STATIONIERUNG");
        if (stationString.length() == 0) {
            log.warn("DIPS: Setting station of gauge '" + name + "' to zero.");
            stationString = "-99999";
        }
        station = Double.parseDouble(stationString);
        if (station == 0d) {
            log.warn("DIPS: Station of gauge '" + name + "' is zero.");
        }

        datums = new ArrayList<Datum>();
        NodeList nodes = element.getElementsByTagName("PNP");
        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Element e = (Element)nodes.item(i);
            Datum datum = new Datum(e);
            datums.add(datum);
        }
        Collections.sort(datums, DATE_CMP);
    }

    public List<Datum> getDatums() {
        return datums;
    }

    public String getName() {
        return name;
    }

    public String getRiverName() {
        return riverName;
    }

    public int getFlysId() {
        return flysId;
    }

    public void setFlysId(int flysId) {
        this.flysId = flysId;
    }

    public String getAftName() {
        return aftName != null ? aftName : name;
    }

    public void setAftName(String aftName) {
        this.aftName = aftName;
    }

    public double getStation() {
        return station;
    }

    public double getAeo() {
        return aeo;
    }

    public void setAeo(double aeo) {
        this.aeo = aeo;
    }

    public void setStation(double station) {
        this.station = station;
    }

    public boolean hasDatums() {
        return !datums.isEmpty();
    }

    public Datum getLatestDatum() {
        return datums.get(datums.size()-1);
    }

    public Long getOfficialNumber() {
        return officialNumber;
    }

    public void setOfficialNumber(Long officialNumber) {
        this.officialNumber = officialNumber;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
