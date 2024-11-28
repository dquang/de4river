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
@Table(name = "location_system")
public class LocationSystem implements Serializable {

    protected Integer id;

    protected String name;
    protected String description;


    public LocationSystem() {
    }


    public LocationSystem(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_LOCATION_SYSTEM_ID_SEQ",
        sequenceName   = "LOCATION_SYSTEM_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_LOCATION_SYSTEM_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
