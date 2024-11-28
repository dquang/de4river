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
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.GenerationType;

import java.util.List;

@Entity
@Table(name = "positions")
public class Position
implements   Serializable
{
    private Integer id;

    private String  value;

    private List<Annotation> annotations;

    public Position() {
    }

    public Position(String value) {
        this.value = value;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_POSITIONS_ID_SEQ",
        sequenceName   = "POSITIONS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_POSITIONS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @OneToMany
    @JoinColumn(name="position_id")
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
