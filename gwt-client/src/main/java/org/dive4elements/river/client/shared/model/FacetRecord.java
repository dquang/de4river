/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * ListGridRecord for Facets.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FacetRecord extends ListGridRecord {

    /** Underlying theme. */
    protected Theme theme;


    public FacetRecord(Theme theme) {
        this.theme = theme;

        setActive(theme.getActive() == 1);
        setName(theme.getDescription());
    }


    public Theme getTheme() {
        return theme;
    }


    public void setName(String description) {
        // TODO Add a setter method setName() to Facet
        // facet.setName(name);
        setAttribute("name", description);
    }


    public String getName() {
        return getAttribute("name");
    }


    public boolean getActive() {
        return getAttributeAsBoolean("active");
    }


    public void setActive(boolean active) {
        setAttribute("active", active);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
