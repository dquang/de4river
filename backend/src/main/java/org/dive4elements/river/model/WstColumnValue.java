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
@Table(name = "wst_column_values")
public class WstColumnValue
implements   Serializable
{
    private Integer    id;
    private WstColumn  wstColumn;
    private BigDecimal position;
    private BigDecimal w;

    public WstColumnValue() {
    }

    public WstColumnValue(
        WstColumn  wstColumn,
        BigDecimal position,
        BigDecimal w
    ) {
        this.wstColumn = wstColumn;
        this.position  = position;
        this.w         = w;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_WST_COLUMN_VALUES_ID_SEQ",
        sequenceName   = "WST_COLUMN_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_WST_COLUMN_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "wst_column_id")
    public WstColumn getWstColumn() {
        return wstColumn;
    }

    public void setWstColumn(WstColumn wstColumn) {
        this.wstColumn = wstColumn;
    }

    @Column(name = "position") // FIXME: type mapping needed?
    public BigDecimal getPosition() {
        return position;
    }

    public void setPosition(BigDecimal position) {
        this.position = position;
    }

    @Column(name = "w") // FIXME: type mapping needed?
    public BigDecimal getW() {
        return w;
    }

    public void setW(BigDecimal w) {
        this.w = w;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
