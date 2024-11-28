/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.dive4elements.river.client.shared.model.Style;


/**
 * This interface provides a method to list themes filterd by name.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface ThemeListingServiceAsync {

    public void list(
        String locale,
        String name,
        AsyncCallback<Map<String, Style>> callback);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
