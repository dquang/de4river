/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Query;
import org.dive4elements.river.backend.SessionHolder;

@Entity
@Table(name = "discharge_tables")
public class DischargeTable
implements   Serializable, Comparable<DischargeTable>
{
    private Integer      id;
    private Gauge        gauge;
    private String       description;
    private String       bfgId;
    private Integer      kind;
    private TimeInterval timeInterval;

    private List<DischargeTableValue> dischargeTableValues;

    public DischargeTable() {
        kind = 0;
    }

    public DischargeTable(Gauge gauge) {
        this(gauge, null, null, 0, null);
    }

    public DischargeTable(
        Gauge        gauge,
        String       description,
        String       bfgId,
        Integer      kind,
        TimeInterval timeInterval
    ) {
        this.gauge        = gauge;
        this.description  = description;
        this.bfgId        = bfgId;
        this.kind         = kind;
        this.timeInterval = timeInterval;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_DISCHARGE_TABLES_ID_SEQ",
        sequenceName   = "DISCHARGE_TABLES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_DISCHARGE_TABLES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "gauge_id")
    public Gauge getGauge() {
        return gauge;
    }

    public void setGauge(Gauge gauge) {
        this.gauge = gauge;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "bfg_id")
    public String getBfgId() {
        return bfgId;
    }

    public void setBfgId(String bfgId) {
        this.bfgId = bfgId;
    }

    @Column(name = "kind")
    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @OneToMany
    @JoinColumn(name = "table_id")
    @OrderBy("q")
    public List<DischargeTableValue> getDischargeTableValues() {
        return dischargeTableValues;
    }

    public void setDischargeTableValues(
        List<DischargeTableValue> dischargeTableValues
    ) {
        this.dischargeTableValues = dischargeTableValues;
    }

    @Override
    public int compareTo(DischargeTable o) {
        if (getKind() == 0 && o.getKind() != 0) {
            return 1;
        }

        TimeInterval other = o.getTimeInterval();
        if (other == null && timeInterval == null) {
            return 1;
        }
        else if (other == null) {
            return -1;
        }
        else if (timeInterval == null) {
            return 1;
        }

        Date otherStartTime = other.getStartTime();
        Date thisStartTime  = timeInterval.getStartTime();

        if (otherStartTime == null) {
            return -1;
        }
        else if (thisStartTime == null) {
            return 1;
        }

        long otherStart = otherStartTime.getTime();
        long thisStart  = thisStartTime.getTime();

        if (otherStart < thisStart) {
            return 1;
        }
        else if (otherStart > thisStart) {
            return -1;
        }

        Date otherStopTime  = other.getStopTime();
        Date thisStopTime  = timeInterval.getStopTime();

        if (otherStopTime == null) {
            return -1;
        }
        else if (thisStopTime == null) {
            return 1;
        }

        long otherEnd   = otherStopTime.getTime();
        long thisEnd    = thisStopTime.getTime();

        if (otherEnd < thisEnd) {
            return 1;
        }
        else if (otherEnd > thisEnd) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public static DischargeTable getDischargeTableById(int dtId)
    {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from DischargeTable where id =:dtId");
        query.setParameter("dtId", dtId);

        List<DischargeTable> list = query.list();
        return list.isEmpty() ? null : list.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
