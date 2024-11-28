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
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.OneToOne;
import javax.persistence.GenerationType;


@Entity
@Table(name = "sq_relation_value")
public class SQRelationValue implements Serializable {

    private Integer id;

    private SQRelation sqRelation;

    private String parameter;

    private MeasurementStation measurementStation;

    private Double a;
    private Double b;
    private Double qMax;
    private Double rSQ;
    private Integer nTot;
    private Integer nOutlier;
    private Double cFerguson;
    private Double cDuan;


    protected SQRelationValue() {
    }


    public SQRelationValue(
        SQRelation         sqRelation,
        String             parameter,
        MeasurementStation measurementStation,
        Double             a,
        Double             b,
        Double             qMax,
        Double             rSQ,
        Integer            nTot,
        Integer            nOutlier,
        Double             cFerguson,
        Double             cDuan
    ) {
        this.sqRelation         = sqRelation;
        this.parameter          = parameter;
        this.measurementStation = measurementStation;
        this.a                  = a;
        this.b                  = b;
        this.qMax               = qMax;
        this.rSQ                = rSQ;
        this.nTot               = nTot;
        this.nOutlier           = nOutlier;
        this.cFerguson          = cFerguson;
        this.cDuan              = cDuan;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SQ_VALUE_ID_SEQ",
        sequenceName   = "SQ_RELATION_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SQ_VALUE_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @OneToOne
    @JoinColumn(name = "sq_relation_id")
    public SQRelation getSqRelation() {
        return sqRelation;
    }

    public void setSqRelation(SQRelation sqRelation) {
        this.sqRelation = sqRelation;
    }


    @Column(name = "parameter")
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @OneToOne
    @JoinColumn(name = "measurement_station_id")
    public MeasurementStation getMeasurementStation() {
        return measurementStation;
    }

    public void setMeasurementStation(MeasurementStation measurementStation) {
        this.measurementStation = measurementStation;
    }


    @Column(name = "a")
    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }


    @Column(name = "b")
    public Double getB() {
        return b;
    }

    public void setB(Double b) {
        this.b = b;
    }

    @Column(name = "qmax")
    public Double getQMax() {
        return qMax;
    }

    public void setQMax(Double qMax) {
        this.qMax = qMax;
    }

    @Column(name = "rsq")
    public Double getRSQ() {
        return rSQ;
    }

    public void setRSQ(Double rSQ) {
        this.rSQ = rSQ;
    }

    @Column(name = "ntot")
    public Integer getNTot() {
        return nTot;
    }

    public void setNTot(Integer nTot) {
        this.nTot = nTot;
    }

    @Column(name = "noutl")
    public Integer getNOutlier() {
        return nOutlier;
    }

    public void setNOutlier(Integer nOutlier) {
        this.nOutlier = nOutlier;
    }

    @Column(name = "cferguson")
    public Double getCFerguson() {
        return cFerguson;
    }

    public void setCFerguson(Double cFerguson) {
        this.cFerguson = cFerguson;
    }

    @Column(name = "cduan")
    public Double getCDuan() {
        return cDuan;
    }

    public void setCDuan(Double cDuan) {
        this.cDuan = cDuan;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
