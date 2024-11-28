/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

public class PeriodSelect extends DefaultState {

    public static final String UI_PROVIDER = "period_select";

    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that initializes an empty State object.
     */
    public PeriodSelect() {
    }

    @Override
    protected String getUIProvider() {
        return UI_PROVIDER;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
