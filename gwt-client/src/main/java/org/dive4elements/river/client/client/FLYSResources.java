/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;


/**
 * A {@link ClientBundle} that is used to handle resources in this client.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface FLYSResources extends ClientBundle {

    public static final FLYSResources INSTANCE =
        GWT.create(FLYSResources.class);

        @Source("config.xml")
        public TextResource initialConfiguration();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
