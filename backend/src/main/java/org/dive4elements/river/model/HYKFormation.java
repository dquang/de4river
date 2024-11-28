/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.List;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "hyk_formations")
public class HYKFormation
implements   Serializable
{
    private Integer    id;
    private Integer    formationNum;
    private HYKEntry   entry;
    private BigDecimal top;
    private BigDecimal bottom;
    private BigDecimal distanceVL;
    private BigDecimal distanceHF;
    private BigDecimal distanceVR;

    private List<HYKFlowZone> zones;

    public HYKFormation() {
    }

    public HYKFormation(
        Integer    formationNum,
        HYKEntry   entry,
        BigDecimal top,
        BigDecimal bottom,
        BigDecimal distanceVL,
        BigDecimal distanceHF,
        BigDecimal distanceVR
    ) {
        this.formationNum = formationNum;
        this.entry        = entry;
        this.top          = top;
        this.bottom       = bottom;
        this.distanceVL   = distanceVL;
        this.distanceHF   = distanceHF;
        this.distanceVR   = distanceVR;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_HYK_FORMATIONS_ID_SEQ",
        sequenceName   = "HYK_FORMATIONS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_HYK_FORMATIONS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "formation_num")
    public Integer getFormationNum() {
        return formationNum;
    }

    public void setFormationNum(Integer formationNum) {
        this.formationNum = formationNum;
    }

    @OneToOne
    @JoinColumn(name = "hyk_entry_id")
    public HYKEntry getEntry() {
        return entry;
    }

    public void setEntry(HYKEntry entry) {
        this.entry = entry;
    }

    @Column(name = "top")
    public BigDecimal getTop() {
        return top;
    }

    public void setTop(BigDecimal top) {
        this.top = top;
    }

    @Column(name = "bottom")
    public BigDecimal getBottom() {
        return bottom;
    }

    public void setBottom(BigDecimal bottom) {
        this.bottom = bottom;
    }

    @Column(name = "distance_vl")
    public BigDecimal getDistanceVL() {
        return distanceVL;
    }

    public void setDistanceVL(BigDecimal distanceVL) {
        this.distanceVL = distanceVL;
    }

    @Column(name = "distance_hf")
    public BigDecimal getDistanceHF() {
        return distanceHF;
    }

    public void setDistanceHF(BigDecimal distanceHF) {
        this.distanceHF = distanceHF;
    }

    @Column(name = "distance_vr")
    public BigDecimal getDistanceVR() {
        return distanceVR;
    }

    public void setDistanceVR(BigDecimal distanceVR) {
        this.distanceVR = distanceVR;
    }


    @OneToMany
    @OrderBy("a")
    @JoinColumn(name="formation_id")
    public List<HYKFlowZone> getZones() {
        return zones;
    }

    public void setZones(List<HYKFlowZone> zones) {
        this.zones = zones;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
