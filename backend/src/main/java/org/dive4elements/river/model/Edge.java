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
@Table(name = "edges")
public class Edge
implements   Serializable
{
    private Integer    id;
    private BigDecimal top;
    private BigDecimal bottom;

    public Edge() {
    }

    public Edge(BigDecimal top, BigDecimal bottom) {
        this.top    = top;
        this.bottom = bottom;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_EDGES_ID_SEQ",
        sequenceName   = "EDGES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_EDGES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
