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
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@Entity
@Table(name = "elevation_model")
public class ElevationModel
implements   Serializable
{
    private static Logger log = LogManager.getLogger(ElevationModel.class);

    protected Integer id;

    protected String name;

    protected Unit unit;


    public ElevationModel() {
    }


    public ElevationModel(String name, Unit unit) {
        this.name = name;
        this.unit = unit;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_ELEVATION_MODE_ID_SEQ",
        sequenceName   = "ELEVATION_MODEL_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_ELEVATION_MODE_ID_SEQ")
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

    @OneToOne
    @JoinColumn(name = "unit_id")
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
