/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StringData extends DefaultData {

    public static final String TYPE = "string";


    public StringData() {
        super();
    }


    public StringData(String name, String description, DataItem[] items) {
        super(name, description, TYPE, items);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
