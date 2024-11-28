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

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "hyk_entries")
public class HYKEntry
implements   Serializable
{
    private Integer    id;
    private HYK        hyk;
    private BigDecimal km;
    private Date       measure;

    private List<HYKFormation> formations;

    public HYKEntry() {
    }

    public HYKEntry(HYK hyk, BigDecimal km, Date measure) {
        this.hyk     = hyk;
        this.km      = km;
        this.measure = measure;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_HYK_ENTRIES_ID_SEQ",
        sequenceName   = "HYK_ENTRIES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_HYK_ENTRIES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "hyk_id")
    public HYK getHYK() {
        return hyk;
    }

    public void setHYK(HYK hyk) {
        this.hyk = hyk;
    }

    @Column(name = "km")
    public BigDecimal getKm() {
        return km;
    }

    public void setKm(BigDecimal km) {
        this.km = km;
    }

    @Column(name = "measure")
    public Date getMeasure() {
        return measure;
    }

    public void setMeasure(Date measure) {
        this.measure = measure;
    }

    @OneToMany
    @OrderBy("formationNum")
    @JoinColumn(name="hyk_entry_id")
    public List<HYKFormation> getFormations() {
        return formations;
    }

    public void setFormations(List<HYKFormation> formations) {
        this.formations = formations;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
