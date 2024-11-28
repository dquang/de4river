/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dive4elements.river.client.shared.model.Artifact;


/**
 * This interface provides artifact specific operation ADVANCE.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface AdvanceServiceAsync {

    public void advance(
        String                  locale,
        Artifact                artifact,
        String                  target,
        AsyncCallback<Artifact> callback);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
