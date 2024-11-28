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

import org.dive4elements.river.client.shared.model.Recommendation;

/**
 * This interface provides artifact specific services as CREATE, DESCRIBE, FEED,
 * ADVANCE and OUT.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface ArtifactServiceAsync {

    public void create(
        String         locale,
        String         factory,
        Recommendation recommendation,
        AsyncCallback<Artifact> callback);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
