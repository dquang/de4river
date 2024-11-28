/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Data;

/**
 * This interface provides artifact specific operations FEED and ADVANCE.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
@RemoteServiceRelativePath("forward")
public interface StepForwardService extends RemoteService {

    /**
     * This method inserts new data into the an existing artifact and
     * advances its state.
     *
     * @param locale The locale used for the request.
     * @param artifact The artifact.
     * @param data The data that should be inserted.
     *
     * @return the artifact which description might have been changed.
     */
    public Artifact go(
        String   locale,
        Artifact artifact,
        Data[]   data)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
