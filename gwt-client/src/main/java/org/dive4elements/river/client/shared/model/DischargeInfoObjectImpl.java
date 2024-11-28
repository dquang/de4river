/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DischargeInfoObjectImpl implements DischargeInfoObject {

    protected String description;

    protected Integer startYear;

    protected Integer endYear;

    protected String bfgId;


    public DischargeInfoObjectImpl() {
    }


    public DischargeInfoObjectImpl(
        String description,
        Integer startYear,
        Integer endYear,
        String bfgId
    ) {
        this.description = description;
        this.startYear   = startYear;
        this.endYear     = endYear;
        this.bfgId       = bfgId;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public Integer getStartYear() {
        return startYear;
    }


    @Override
    public Integer getEndYear() {
        return endYear;
    }

    @Override
    public String getBfGId() {
        return bfgId;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
