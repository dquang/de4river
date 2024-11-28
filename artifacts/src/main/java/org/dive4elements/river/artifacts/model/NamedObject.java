/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;


/**
 * This class represents an object that has a name. The default case would be to
 * inherit from this class.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface NamedObject
extends          Serializable
{
    void setName(String name);

    String getName();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
