/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.exceptions;

import java.io.Serializable;


/**
 * This exception class is used if an error occured while user authentication.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AuthenticationException
extends      Exception
implements   Serializable
{
    public AuthenticationException() {
    }


    public AuthenticationException(String msg) {
        super(msg);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
