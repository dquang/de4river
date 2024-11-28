/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.exceptions;

import java.io.Serializable;


public class ServerException
extends      Exception
implements   Serializable
{
    public ServerException() {
    }


    public ServerException(String msg) {
        super(msg);
    }
}
