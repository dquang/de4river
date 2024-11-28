/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

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

/** Mapped Point of a cross section line. */
@Entity
@Table(name = "cross_section_points")
public class CrossSectionPoint
implements   Serializable
{
    private Integer          id;
    private CrossSectionLine crossSectionLine;
    private Integer          colPos;
    private Double       x;
    private Double       y;

    public CrossSectionPoint() {
    }

    public CrossSectionPoint(
        CrossSectionLine crossSectionLine,
        Integer          colPos,
        Double       x,
        Double       y
    ) {
        this.crossSectionLine = crossSectionLine;
        this.colPos           = colPos;
        this.x                = x;
        this.y                = y;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_CROSS_SECTION_POINTS_ID_SEQ",
        sequenceName   = "CROSS_SECTION_POINTS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_CROSS_SECTION_POINTS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "cross_section_line_id")
    public CrossSectionLine getCrossSectionLine() {
        return crossSectionLine;
    }

    public void setCrossSectionLine(CrossSectionLine crossSectionLine) {
        this.crossSectionLine = crossSectionLine;
    }

    @Column(name = "col_pos")
    public Integer getColPos() {
        return colPos;
    }

    public void setColPos(Integer colPos) {
        this.colPos = colPos;
    }

    @Column(name = "x")
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    @Column(name = "y")
    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
