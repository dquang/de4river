/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.Session;
import org.hibernate.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "discharge_zone")
public class DischargeZone
implements   Serializable
{
    private static Logger log = LogManager.getLogger(DischargeZone.class);

    private Integer id;

    private River river;

    private String gaugeName;

    private BigDecimal value;

    private String lowerDischarge;
    private String upperDischarge;

    private String type;

    public DischargeZone() {
    }


    public DischargeZone(
        River       river,
        String      gaugeName,
        BigDecimal  value,
        String      lowerDischarge,
        String      upperDischarge
    ) {
        this.river          = river;
        this.gaugeName      = gaugeName;
        this.value          = value;
        this.lowerDischarge = lowerDischarge;
        this.upperDischarge = upperDischarge;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_DISCHARGE_ZONE_ID_SEQ",
        sequenceName   = "DISCHARGE_ZONE_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_DISCHARGE_ZONE_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "river_id")
    public River getRiver() {
        return river;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    @Column(name = "value")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Column(name = "gauge_name")
    public String getGaugeName() {
        return gaugeName;
    }

    public void setGaugeName(String gaugeName) {
        this.gaugeName = gaugeName;
    }

    @Column(name = "lower_discharge")
    public String getLowerDischarge() {
        return lowerDischarge;
    }

    public void setLowerDischarge(String lowerDischarge) {
        this.lowerDischarge = lowerDischarge;
    }

    @Column(name = "upper_discharge")
    public String getUpperDischarge() {
        return upperDischarge;
    }

    public void setUpperDischarge(String upperDischarge) {
        this.upperDischarge = upperDischarge;
    }

    public void putType(String type) {
        this.type = type;
    }

    public String fetchType() {
        return this.type;
    }

    public static List<DischargeZone> getDischargeZones(River river) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from DischargeZone where river=:river");

        query.setParameter("river", river);

        return query.list();
    }


    public static DischargeZone getDischargeZoneById(int id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from DischargeZone where id=:id");

        query.setParameter("id", id);

        List<DischargeZone> zones = query.list();

        return zones.isEmpty() ? null : zones.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
