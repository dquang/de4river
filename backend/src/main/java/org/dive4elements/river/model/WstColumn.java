/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.util.List;

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
import javax.persistence.OneToMany;

@Entity
@Table(name = "wst_columns")
public class WstColumn
implements   Serializable
{
    private Integer               id;
    private Wst                   wst;
    private String                name;
    private String                description;
    private String                source;
    private Integer               position;
    private TimeInterval          timeInterval;

    private List<WstColumnQRange> columnQRanges;
    private List<WstColumnValue>  columnValues;

    public WstColumn() {
    }

    public WstColumn(
        Wst          wst,
        String       name,
        String       description,
        String       source,
        Integer      position,
        TimeInterval timeInterval
    ) {
        this.wst          = wst;
        this.name         = name;
        this.description  = description;
        this.source       = source;
        this.position     = position;
        this.timeInterval = timeInterval;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_WST_COLUMNS_ID_SEQ",
        sequenceName   = "WST_COLUMNS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_WST_COLUMNS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "wst_id" )
    public Wst getWst() {
        return wst;
    }

    public void setWst(Wst wst) {
        this.wst = wst;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id" )
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @OneToMany
    @JoinColumn(name="wst_column_id")
    public List<WstColumnQRange> getColumnQRanges() {
        return columnQRanges;
    }

    public void setColumnQRanges(List<WstColumnQRange> columnQRanges) {
        this.columnQRanges = columnQRanges;
    }

    @OneToMany
    @JoinColumn(name="wst_column_id")
    public List<WstColumnValue> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(List<WstColumnValue> columnValues) {
        this.columnValues = columnValues;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
