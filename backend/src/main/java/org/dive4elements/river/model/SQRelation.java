/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.GenerationType;


@Entity
@Table(name = "sq_relation")
public class SQRelation implements Serializable {

    private Integer id;

    private TimeInterval timeInterval;

    private String description;

    private List<SQRelationValue> values;


    protected SQRelation() {
    }


    public SQRelation(TimeInterval timeInterval, String desc) {
        this.timeInterval = timeInterval;
        this.description  = desc;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SQ_ID_SEQ",
        sequenceName   = "SQ_RELATION_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SQ_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }


    @OneToMany
    @JoinColumn(name = "sq_relation_id")
    public List<SQRelationValue> getValues() {
        return values;
    }

    public void setValues(List<SQRelationValue> values) {
        this.values = values;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
