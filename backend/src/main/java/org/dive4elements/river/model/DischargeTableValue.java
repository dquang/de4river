/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.math.BigDecimal;

import java.io.Serializable;

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
@Table(name = "discharge_table_values")
public class DischargeTableValue
implements   Serializable
{
    private Integer        id;
    private DischargeTable dischargeTable;
    private BigDecimal     q;
    private BigDecimal     w;

    public DischargeTableValue() {
    }

    public DischargeTableValue(
        DischargeTable dischargeTable, BigDecimal q, BigDecimal w)
    {
        this.dischargeTable = dischargeTable;
        this.q              = q;
        this.w              = w;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_DISCHARGE_TABLE_VALUES_ID_SEQ",
        sequenceName   = "DISCHARGE_TABLE_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_DISCHARGE_TABLE_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "table_id")
    public DischargeTable getDischargeTable() {
        return dischargeTable;
    }

    public void setDischargeTable(DischargeTable dischargeTable) {
        this.dischargeTable = dischargeTable;
    }


    @Column(name = "q")
    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    @Column(name = "w")
    public BigDecimal getW() {
        return w;
    }

    public void setW(BigDecimal w) {
        this.w = w;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
