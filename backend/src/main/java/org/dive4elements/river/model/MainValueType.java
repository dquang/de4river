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
@Table(name = "main_value_types")
public class MainValueType
implements   Serializable
{
    private Integer id;
    private String  name;

    public MainValueType() {
    }

    public MainValueType(String name) {
        this.name = name;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_MAIN_VALUE_TYPES_ID_SEQ",
        sequenceName   = "MAIN_VALUE_TYPES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_MAIN_VALUE_TYPES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name") // FIXME: Type conversion needed?
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
