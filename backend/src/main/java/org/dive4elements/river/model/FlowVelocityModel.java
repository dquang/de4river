/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import org.dive4elements.river.backend.SessionHolder;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;


@Entity
@Table(name = "flow_velocity_model")
public class FlowVelocityModel
implements   Serializable
{
    private static Logger log = LogManager.getLogger(FlowVelocityModel.class);

    private Integer id;

    private DischargeZone dischargeZone;

    private String description;


    public FlowVelocityModel() {
    }


    public FlowVelocityModel(DischargeZone dischargeZone) {
        this(dischargeZone, null);
    }


    public FlowVelocityModel(
        DischargeZone dischargeZone,
        String        description
    ) {
        this.dischargeZone = dischargeZone;
        this.description   = description;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_FLOW_VELOCITY_MODEL_ID_SEQ",
        sequenceName   = "FLOW_VELOCITY_MODEL_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_FLOW_VELOCITY_MODEL_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "discharge_zone_id")
    public DischargeZone getDischargeZone() {
        return dischargeZone;
    }

    public void setDischargeZone(DischargeZone dischargeZone) {
        this.dischargeZone = dischargeZone;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public static List<FlowVelocityModel> getModels(DischargeZone zone) {

        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from FlowVelocityModel where dischargeZone=:zone");

        query.setParameter("zone", zone);

        return query.list();
    }


    /** Get a Model by id. */
    public static FlowVelocityModel getModel(int id) {

        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from FlowVelocityModel where id=:id");

        query.setParameter("id", id);

        return (FlowVelocityModel) query.list().get(0);
    }


    /** Get description of a Model by id. */
    public static String getModelDescription(int id) {

        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from FlowVelocityModel where id=:id");

        query.setParameter("id", id);

        FlowVelocityModel model = (FlowVelocityModel) query.list().get(0);

        return (model == null) ? null : model.getDescription();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
