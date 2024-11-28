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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.dive4elements.river.backend.SessionHolder;
import org.hibernate.Session;

@Entity
@Table(name = "official_lines")
public class OfficialLine
implements   Serializable
{

    private Integer id;
    private WstColumn wstColumn;
    private NamedMainValue namedMainValue;

    public OfficialLine() {
    }

    public OfficialLine(WstColumn wstColumn, NamedMainValue namedMainValue) {
        this.wstColumn = wstColumn;
        this.namedMainValue = namedMainValue;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_OFFICIAL_LINES_ID_SEQ",
        sequenceName   = "OFFICIAL_LINES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_OFFICIAL_LINES_ID_SEQ")
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
    @JoinColumn(name = "named_main_value_id" )
    public NamedMainValue getNamedMainValue() {
        return namedMainValue;
    }

    public void setNamedMainValue(NamedMainValue namedMainValue) {
        this.namedMainValue = namedMainValue;
    }

    public static List<OfficialLine> fetchAllOfficalLines() {
        Session session = SessionHolder.HOLDER.get();
        return session.createQuery("from OfficialLine").list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
