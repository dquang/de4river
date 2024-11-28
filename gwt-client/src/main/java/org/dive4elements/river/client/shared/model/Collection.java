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
import java.util.List;
import java.util.Map;


/**
 * The artifact collection.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Collection extends Serializable {

    public String identifier();

    public String getName();

    public void setName(String name);

    public Date getCreationTime();

    /** TODO never called, trivial implementation. Can be removed? */
    public Date getLastAccess();

    public long getTTL();

    public void setTTL(long ttl);

    public void addItem(CollectionItem item);

    public int getItemLength();

    public CollectionItem getItem(int idx);

    public CollectionItem getItem(String uuid);

    public Map<String, OutputMode> getOutputModes();

    public ThemeList getThemeList(String outName);

    public Settings getSettings(String outName);

    public void setSettings(Map<String, Settings> settings);

    public void addSettings(String name, Settings settings);

    /** Sets mapping outputname to ThemeList. */
    public void setThemeLists(Map<String, ThemeList> map);

    public List<Recommendation> getRecommendations();

    public void addRecommendation(Recommendation recommendation);

    public void addRecommendations(List<Recommendation> recommendations);

    public boolean loadedRecommendation(Recommendation recommendation);

    public boolean hasItems();

    /**
     * Returns the name which should be displayed in the client gui
     * @return String display name
     */
    public String getDisplayName();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
