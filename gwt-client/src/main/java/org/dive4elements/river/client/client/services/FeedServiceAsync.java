/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Data;


/**
 * This interface provides artifact specific operation FEED.
 */
public interface FeedServiceAsync {

    /**
     * Inserts new data into an existing artifact.
     *
     * @param locale The locale used for the request.
     * @param artifact The artifact.
     * @param data The data that should be inserted.
     */
    public void feed(
        String                  locale,
        Artifact                artifact,
        Data[]                  data,
        AsyncCallback<Artifact> callback);

    /**
     * Inserts (the same) new data into existing artifacts.
     *
     * @param locale The locale used for the request.
     * @param artifact The artifact.
     * @param data The data that should be inserted.
     */
    public void feedMany(
        String                  locale,
        List<Artifact>          artifacts,
        Data[]                  data,
        AsyncCallback<List<Artifact>> callback);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
