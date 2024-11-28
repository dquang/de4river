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

/**
 * Talk-to interface for crosssectionkm service.
 */
public interface CrossSectionKMServiceAsync {

    void getCrossSectionKMs(
        String                locale,
        Map<Integer, Double>  data,
        int                   nNeighbours,
        AsyncCallback<Map<Integer, Double[]>> cb);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
