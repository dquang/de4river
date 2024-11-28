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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

import org.hibernate.Query;
import org.hibernate.Session;

@Entity
@Table(name = "named_main_values")
public class NamedMainValue
implements   Serializable
{
    private Integer       id;
    private String        name;
    private MainValueType type;

    private List<OfficialLine> officialLines;

    public NamedMainValue() {
    }

    public NamedMainValue(String name, MainValueType type) {
        this.name = name;
        this.type = type;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_NAMED_MAIN_VALUES_ID_SEQ",
        sequenceName   = "NAMED_MAIN_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_NAMED_MAIN_VALUES_ID_SEQ")
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
    @JoinColumn(name = "type_id" )
    public MainValueType getType() {
        return type;
    }

    public void setType(MainValueType type) {
        this.type = type;
    }

    @OneToMany
    @JoinColumn(name = "named_main_value_id")
    public List<OfficialLine> getOfficialLines() {
        return officialLines;
    }

    public void setOfficialLines(List<OfficialLine> officialLines) {
        this.officialLines = officialLines;
    }

    public static NamedMainValue fetchByNameAndType(
        String name, String type, Session session) {
        Query query = session.createQuery(
            "from NamedMainValue where name=:name and type.name = :type");
        query.setString("name", name);
        query.setString("type", type);
        List<NamedMainValue> named = query.list();
        return named.isEmpty() ? null : named.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
