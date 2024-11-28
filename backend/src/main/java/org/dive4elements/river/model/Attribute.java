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

@Entity
@Table(name = "attributes")
public class Attribute
implements   Serializable
{
    private Integer id;

    private String  value;

    public Attribute() {
    }

    public Attribute(String value) {
        this.value = value;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_ATTRIBUTES_ID_SEQ",
        sequenceName   = "ATTRIBUTES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_ATTRIBUTES_ID_SEQ")
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
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
