/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.sq;

import org.dive4elements.river.artifacts.states.DefaultState;


public class SQInitState
extends DefaultState
{

    @Override
    protected String getUIProvider() {
        return "static_data";
    }
}