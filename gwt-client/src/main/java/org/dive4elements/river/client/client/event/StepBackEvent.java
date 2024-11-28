/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;

import java.io.Serializable;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StepBackEvent implements Serializable {

    private static final long serialVersionUID = 7895180143662002198L;

    /** The identifier of the target state.*/
    protected String target;


    /**
     * Creates a new StepBackEvent with the identifier of the target state.
     *
     * @param target The identifier of the target state.
     */
    public StepBackEvent(String target) {
        this.target = target;
    }


    /**
     * A method to retrieve the target identifier.
     *
     * @return the target identifier.
     */
    public String getTarget() {
        return target;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
