/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

/** Interface to represent user authentications
 */
public interface Authentication {

    /** Returns true if the authentication was successfull
     */
    public boolean isSuccess();

    /** Returns a new User object
     */
    public User getUser() throws AuthenticationException;

}
