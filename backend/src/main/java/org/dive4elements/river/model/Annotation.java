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

@Entity
@Table(name = "annotations")
public class Annotation
implements   Serializable
{
    private Integer        id;
    private Range          range;
    private Attribute      attribute;
    private Position       position;
    private Edge           edge;
    private AnnotationType type;

    public Annotation() {
    }

    public Annotation(
        Range          range,
        Attribute      attribute,
        Position       position,
        Edge           edge,
        AnnotationType type
    ) {
        this.range     = range;
        this.attribute = attribute;
        this.position  = position;
        this.edge      = edge;
        this.type      = type;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_ANNOTATIONS_ID_SEQ",
        sequenceName   = "ANNOTATIONS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_ANNOTATIONS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "range_id")
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @OneToOne
    @JoinColumn(name = "attribute_id")
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @OneToOne
    @JoinColumn(name = "position_id")
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @OneToOne
    @JoinColumn(name = "edge_id")
    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    @OneToOne
    @JoinColumn(name = "type_id")
    public AnnotationType getType() {
        return type;
    }

    public void setType(AnnotationType type) {
        this.type = type;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
