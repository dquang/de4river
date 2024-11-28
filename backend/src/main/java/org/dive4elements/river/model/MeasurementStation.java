/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.backend.SessionHolder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.dive4elements.river.backend.utils.EpsilonComparator.CMP;

@Entity
@Table(name = "measurement_station")
public class MeasurementStation {
    private static final Logger log = LogManager.getLogger(
        MeasurementStation.class);

    private Integer id;

    private String name;
    private String measurementType;
    private String riverside;
    private String operator;
    private String comment;

    private Range range;

    private Gauge gauge;
    private String gaugeName;

    private TimeInterval observationTimerange;

    public static final String MEASUREMENT_TYPE_BEDLOAD = "Geschiebe";
    public static final String MEASUREMENT_TYPE_SUSP = "Schwebstoff";

    public static final class MeasurementStationComparator
        implements Comparator<MeasurementStation> {

        public MeasurementStationComparator() {
        }

        /* Compare MeasurementStations by km and consider MeasurementStations
           of type "Geschiebe" as smaller if at same km. */
        @Override
        public int compare(MeasurementStation m1, MeasurementStation m2)
            throws IllegalArgumentException {

            if (m1.getRange().getRiver() != m2.getRange().getRiver()) {
                throw new IllegalArgumentException(
                    "Stations not at same river");
            }

            int cmpStations = CMP.compare(m1.retrieveKm(), m2.retrieveKm());
            if (cmpStations == 0) {
                if (m1.getMeasurementType().equals(m2.getMeasurementType())) {
                    throw new IllegalArgumentException(
                        "Two stations of same type at same km");
                }

                return m1.getMeasurementType().equals(MEASUREMENT_TYPE_BEDLOAD)
                    ? -1
                    : +1;
            }
            return cmpStations;
        }
    }

    public MeasurementStation() {
    }

    public MeasurementStation(String name, String measurementType,
        String riverside, Range range, Gauge gauge,
        String gaugeName, TimeInterval observationTimerange, String operator,
        String comment
    ) {
        this.name = name;
        this.measurementType = measurementType;
        this.riverside = riverside;
        this.range = range;
        this.gauge = gauge;
        this.gaugeName = gaugeName;
        this.observationTimerange = observationTimerange;
        this.operator = operator;
        this.comment = comment;
    }

    @Id
    @SequenceGenerator(
        name = "SEQ_MEASUREMENT_STATION_ID_SEQ",
        sequenceName = "MEASUREMENT_STATION_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_MEASUREMENT_STATION_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "measurement_type")
    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    @Column(name = "riverside")
    public String getRiverside() {
        return riverside;
    }

    public void setRiverside(String riverside) {
        this.riverside = riverside;
    }

    @OneToOne
    @JoinColumn(name = "reference_gauge_id")
    public Gauge getGauge() {
        return gauge;
    }

    public void setGauge(Gauge gauge) {
        this.gauge = gauge;
    }

    @Column(name = "reference_gauge_name")
    public String getGaugeName() {
        return gaugeName;
    }

    public void setGaugeName(String gaugeName) {
        this.gaugeName = gaugeName;
    }

    @OneToOne
    @JoinColumn(name = "range_id")
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getObservationTimerange() {
        return observationTimerange;
    }

    public void setObservationTimerange(TimeInterval observationTimerange) {
        this.observationTimerange = observationTimerange;
    }

    @Column(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Column(name = "commentary")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /* Get the actual location of the measurement station at the river */
    public double retrieveKm() {
        // In case river is km_up, station is at larger value of range
        return getRange().getRiver().getKmUp() && getRange().getB() != null
            ? getRange().getB().doubleValue()
            : getRange().getA().doubleValue();
    }

    /* Get measurement station of other type at the same location */
    public MeasurementStation findCompanionStation() {
        River  river = getRange().getRiver();
        double km    = retrieveKm();
        List<MeasurementStation> stations = getStationsAtKM(river, km);

        for (MeasurementStation station: stations) {
            if (!station.getMeasurementType().equals(getMeasurementType())) {
                return station;
            }
        }
        log.debug("No additional stations found at km " + km +
            " at river " + river);
        return null;
    }

    public static List<MeasurementStation> getStationsAtRiver(River river) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from MeasurementStation " +
            "where range.river = :river");
        query.setParameter("river", river);

        List<MeasurementStation> result = query.list();
        Collections.sort(result, new MeasurementStationComparator());
        return result;
    }

    public static List<MeasurementStation> getStationsAtKM(
        River river,
        Double river_km
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from MeasurementStation where range.river = :river");
        query.setParameter("river", river);

        List<MeasurementStation> result = new ArrayList<MeasurementStation>();
        for (Iterator iter = query.iterate(); iter.hasNext();) {
            MeasurementStation st = (MeasurementStation)iter.next();
            if (CMP.compare(st.retrieveKm(), river_km) == 0) {
                result.add(st);
            }
        }

        if (result.size() > 2) {
            // TODO: database schema should prevent this
            log.warn("More than two measurement stations at km " + river_km +
                " at river " + river.getName());
        }

        return result;
    }
}
