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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "hyk_flow_zones")
public class HYKFlowZone
implements   Serializable
{
    private Integer         id;
    private HYKFormation    formation;
    private HYKFlowZoneType type;
    private BigDecimal      a;
    private BigDecimal      b;

    public HYKFlowZone() {
    }

    public HYKFlowZone(
        HYKFormation    formation,
        HYKFlowZoneType type,
        BigDecimal      a,
        BigDecimal      b
    ) {
        this.formation = formation;
        this.type      = type;
        this.a         = a;
        this.b         = b;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_HYK_FLOW_ZONES_ID_SEQ",
        sequenceName   = "HYK_FLOW_ZONES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_HYK_FLOW_ZONES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "formation_id")
    public HYKFormation getFormation() {
        return formation;
    }

    public void setFormation(HYKFormation formation) {
        this.formation = formation;
    }

    @OneToOne
    @JoinColumn(name = "type_id")
    public HYKFlowZoneType getType() {
        return type;
    }

    public void setType(HYKFlowZoneType type) {
        this.type = type;
    }

    @Column(name = "a")
    public BigDecimal getA() {
        return a;
    }

    public void setA(BigDecimal a) {
        this.a = a;
    }

    @Column(name = "b")
    public BigDecimal getB() {
        return b;
    }

    public void setB(BigDecimal b) {
        this.b = b;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
