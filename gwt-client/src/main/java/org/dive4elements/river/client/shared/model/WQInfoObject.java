/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public interface WQInfoObject extends Serializable {

    String getName();

    String getType();

    Double getValue();

    boolean isOfficial();

    Date getStartTime();

    Date getStopTime();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
