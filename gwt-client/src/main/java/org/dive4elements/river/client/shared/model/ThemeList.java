/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Data Model for list of themes (shown facets).
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ThemeList implements Serializable {

    public List<Theme> themes;


    public ThemeList() {
    }


    public ThemeList(List<Theme> themes) {
        this.themes = themes;
    }


    public List<Theme> getThemes() {
        return themes;
    }


    public List<Theme> getActiveThemes() {
        List<Theme> active = new ArrayList<Theme>();
        List<Theme> all    = getThemes();

        if (all == null || all.isEmpty()) {
            return active;
        }

        for (Theme theme: all) {
            if (theme.getActive() == 1) {
                active.add(theme);
            }
        }

        return active;
    }


    public int getThemeCount() {
        return themes.size();
    }


    /**
     * Returns (first) theme of which the artifact has given uuid, null if none
     * found.
     * @param uuid Artifacts identifier for which to search theme.
     * @return theme of which getArtifact() equals given uuid.
    */
    public Theme getTheme(String uuid) {
        for (Theme theme: themes) {
            if (theme.getArtifact().equals(uuid)) {
                return theme;
            }
        }
        return null;
    }


    /**
     * Returns a theme at a specific position. <b>NOTE: Themes start at position
     * 1. So, take care in loops, that might start at index 0!</b>
     *
     * @param pos The position of the desired theme.
     *
     * @return a theme.
     */
    public Theme getThemeAt(int pos) {
        for (Theme theme: themes) {
            if (theme.getPosition() == pos) {
                return theme;
            }
        }

        return null;
    }


    public void removeTheme(Theme theme) {
        if (theme != null) {
            themes.remove(theme);
        }
    }


    public void addTheme(Theme theme) {
        if (theme != null) {
            themes.add(theme);
        }
    }


    /**
     * Modifies the order of themes in this list and the position of the
     * <i>theme</i> itself.
     *
     * @param theme The theme which position has to be modified.
     * @param newPos The new position.
     */
    public void setThemePosition(Theme theme, int newPos) {
        int count  = getThemeCount();
        int oldPos = theme.getPosition();

        if (newPos == oldPos || newPos > count || newPos < 1) {
            return;
        }

        boolean moveUp = newPos < oldPos;

        for (Theme aTheme: themes) {
            int tmpPos = aTheme.getPosition();

            if (theme.equals(aTheme)) {
                theme.setPosition(newPos);
            }
            else if (tmpPos >= newPos && tmpPos < oldPos && moveUp) {
                aTheme.setPosition(tmpPos+1);
            }
            else if (tmpPos <= newPos && tmpPos > oldPos && !moveUp) {
                aTheme.setPosition(tmpPos-1);
            }
        }
    }


    /**
     * Create a map from index to description of facets that have a given name.
     * Only visible facets are taken into account.
     * @param facetName name to match against facets whose info to put in map.
     * @return mapping of index to description
     */
    public LinkedHashMap<String, String> toMapIndexDescription(
        String facetName
    ) {
        int count = getThemeCount();
        LinkedHashMap<String, String> valueMap =
            new LinkedHashMap<String, String>();
        for (int i = 0; i <= count; i++) {
            Theme theme = getThemeAt(i + 1);

            if (theme == null || theme.getVisible() == 0) {
                continue;
            }

            if (theme.getFacet().equals(facetName)) {
                valueMap.put(String.valueOf(theme.getIndex()),
                    theme.getDescription());
            }
        }
        return valueMap;
    }


    public LinkedHashMap<String, String>
        toMapArtifactUUIDDescription(String facetName
    ) {
        int count = getThemeCount();
        LinkedHashMap<String, String> valueMap =
            new LinkedHashMap<String, String>();
        for (int i = 0; i <= count; i++) {
            Theme theme = getThemeAt(i + 1);

            if (theme == null || theme.getVisible() == 0) {
                continue;
            }

            if (theme.getFacet().equals(facetName)) {
                valueMap.put(theme.getArtifact(),
                    theme.getDescription());
            }
        }
        return valueMap;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
