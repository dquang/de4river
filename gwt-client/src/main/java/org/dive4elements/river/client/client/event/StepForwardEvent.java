/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;

import org.dive4elements.river.client.shared.model.Data;

import java.io.Serializable;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StepForwardEvent implements Serializable {

    private static final long serialVersionUID = -5527511690213770954L;

    /** The selected data.*/
    protected Data[] data;

    /**
     * Creates a new StepForwardEvent with the Data that has been selected in
     * the UI.
     *
     * @param data The selected data.
     */
    public StepForwardEvent(Data[] data) {
        this.data = data;
    }


    /**
     * A method to retrieve the data stored in the event.
     *
     * @return the data.
     */
    public Data[] getData() {
        return data;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
