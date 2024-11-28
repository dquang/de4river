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


@Entity
@Table(name = "depths")
public class Depth implements Serializable {

    private Integer id;

    private BigDecimal lower;
    private BigDecimal upper;


    public Depth() {
    }


    public Depth(BigDecimal lower, BigDecimal upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_DEPTHS_ID_SEQ",
        sequenceName   = "DEPTHS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_DEPTHS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "lower")
    public BigDecimal getLower() {
        return lower;
    }

    public void setLower(BigDecimal lower) {
        this.lower = lower;
    }

    @Column(name = "upper")
    public BigDecimal getUpper() {
        return upper;
    }

    public void setUpper(BigDecimal upper) {
        this.upper = upper;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
