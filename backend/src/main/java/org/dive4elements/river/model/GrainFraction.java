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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@Entity
@Table(name = "grain_fraction")
public class GrainFraction
implements   Serializable
{
    public static final String TOTAL              = "total";
    public static final String COARSE             = "coarse";
    public static final String FINE_MIDDLE        = "fine_middle";
    public static final String SAND               = "sand";
    public static final String SUSP_SAND          = "susp_sand";
    public static final String SUSP_SAND_BED      = "susp_sand_bed";
    public static final String SUSPENDED_SEDIMENT = "suspended_sediment";
    public static final String UNKNOWN            = "unknown";


    private static Logger log = LogManager.getLogger(GrainFraction.class);

    private Integer id;

    private String name;

    private Double lower;
    private Double upper;


    public GrainFraction() {
    }

    public GrainFraction(String name, Double lower, Double upper) {
        this.name  = name;
        this.lower = lower;
        this.upper = upper;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_GRAIN_FRACTION_ID_SEQ",
        sequenceName   = "GRAIN_FRACTION_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_GRAIN_FRACTION_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name" )
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "lower")
    public Double getLower() {
        return lower;
    }

    public void setLower(Double lower) {
        this.lower = lower;
    }

    @Column(name = "upper")
    public Double getUpper() {
        return upper;
    }

    public void setUpper(Double upper) {
        this.upper = upper;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
