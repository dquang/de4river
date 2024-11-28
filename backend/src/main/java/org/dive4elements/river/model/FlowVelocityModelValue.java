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
@Table(name = "flow_velocity_model_values")
public class FlowVelocityModelValue
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(FlowVelocityModelValue.class);


    private Integer id;

    private FlowVelocityModel flowVelocity;

    private BigDecimal station;
    private BigDecimal q;
    private BigDecimal totalChannel;
    private BigDecimal mainChannel;
    private BigDecimal shearStress;


    public FlowVelocityModelValue() {
    }


    public FlowVelocityModelValue(
        FlowVelocityModel flowVelocity,
        BigDecimal        station,
        BigDecimal        q,
        BigDecimal        totalChannel,
        BigDecimal        mainChannel,
        BigDecimal        shearStress
    ) {
        this.flowVelocity = flowVelocity;
        this.station      = station;
        this.q            = q;
        this.totalChannel = totalChannel;
        this.mainChannel  = mainChannel;
        this.shearStress  = shearStress;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_FLOW_VELOCITY_M_VALUES_ID_SEQ",
        sequenceName   = "FLOW_VELOCITY_M_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_FLOW_VELOCITY_M_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "flow_velocity_model_id")
    public FlowVelocityModel getFlowVelocity() {
        return flowVelocity;
    }

    public void setFlowVelocity(FlowVelocityModel flowVelocity) {
        this.flowVelocity = flowVelocity;
    }

    @Column(name = "station")
    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    @Column(name = "q")
    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    @Column(name = "total_channel")
    public BigDecimal getTotalChannel() {
        return totalChannel;
    }

    public void setTotalChannel(BigDecimal totalChannel) {
        this.totalChannel = totalChannel;
    }

    @Column(name = "main_channel")
    public BigDecimal getMainChannel() {
        return mainChannel;
    }

    public void setMainChannel(BigDecimal mainChannel) {
        this.mainChannel = mainChannel;
    }

    @Column(name = "shear_stress")
    public BigDecimal getShearStress() {
        return shearStress;
    }

    public void setShearStress(BigDecimal shearStress) {
        this.shearStress = shearStress;
    }


    public static List<FlowVelocityModelValue> getValues(
        FlowVelocityModel model,
        double kmLo,
        double kmHi
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from FlowVelocityModelValue where " +
            "   flowVelocity=:model and" +
            "   station >= :kmLo and " +
            "   station <= :kmHi" +
            "   order by station");

        query.setParameter("model", model);
        query.setParameter("kmLo", new BigDecimal(kmLo));
        query.setParameter("kmHi", new BigDecimal(kmHi));

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
