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
public class SedimentLoadInfoObjectImpl implements SedimentLoadInfoObject {

    protected String description;
    protected String dateString;
    protected String tiIdString;
    protected String tiDateString;

    public SedimentLoadInfoObjectImpl() {
    }

    public SedimentLoadInfoObjectImpl(
        String description,
        String dateString,
        String tiDateString,
        String tiIdString
    ) {
        this.description = description;
        this.dateString = dateString;
        this.tiDateString = tiDateString;
        this.tiIdString = tiIdString;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return dateString;
    }

    public String getSQTiDate() {
        return tiDateString;
    }

    public String getSQTiId() {
        return tiIdString;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
