/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.themes;

import java.util.Map;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class ThemeGroup {

    protected String name;

    protected Map<String, Theme> themes;


    public ThemeGroup(String name, Map<String, Theme> themes) {
        this.name = name;
        this.themes = themes;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Theme> getThemes() {
        return this.themes;
    }

    public Theme getThemeByName(String name) {
        return themes.get(name);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
