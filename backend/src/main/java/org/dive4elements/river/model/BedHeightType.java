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
import javax.persistence.GenerationType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;

import org.dive4elements.river.backend.SessionHolder;

@Entity
@Table(name = "bed_height_type")
public class BedHeightType
implements   Serializable
{
    private static Logger log = LogManager.getLogger(BedHeightType.class);

    private Integer id;
    private String  name;


    public BedHeightType() {
    }

    public BedHeightType(String name) {
        this.name = name;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_BED_HEIGHT_TYPE_ID_SEQ",
        sequenceName   = "BED_HEIGHT_TYPE_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_BED_HEIGHT_TYPE_ID_SEQ")
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

    public static BedHeightType fetchBedHeightTypeForType(String type) {
        return fetchBedHeightTypeForType(type, null);
    }

    public static BedHeightType fetchBedHeightTypeForType(
        String name,
        Session session
    ) {
        if (session == null) {
            session = SessionHolder.HOLDER.get();
        }

        Query query = session.createQuery(
            "from BedHeightType where name=:name");

        query.setParameter("name", name);

        List<Object> results = query.list();

        return results.isEmpty() ? null : (BedHeightType)results.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
