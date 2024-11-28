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
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "wst_column_q_ranges")
public class WstColumnQRange
implements   Serializable
{
    private Integer   id;
    private WstColumn wstColumn;
    private WstQRange wstQRange;

    public WstColumnQRange() {
    }

    public WstColumnQRange(
        WstColumn wstColumn,
        WstQRange wstQRange
    ) {
        this.wstColumn = wstColumn;
        this.wstQRange = wstQRange;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_WST_Q_RANGES_ID_SEQ",
        sequenceName   = "WST_Q_RANGES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_WST_Q_RANGES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "wst_column_id" )
    public WstColumn getWstColumn() {
        return wstColumn;
    }

    public void setWstColumn(WstColumn wstColumn) {
        this.wstColumn = wstColumn;
    }

    @OneToOne
    @JoinColumn(name = "wst_q_range_id" )
    public WstQRange getWstQRange() {
        return wstQRange;
    }

    public void setWstQRange(WstQRange wstQRange) {
        this.wstQRange = wstQRange;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

