/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The default implementation of a {@link Collection}.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultCollection implements Collection {

    /** The uuid of the collection. */
    protected String uuid;

    /** The name of the collection. */
    protected String name;

    /** The creation time of this collection. */
    protected Date creation;

    /**
     * The time to live of the collection.
     * If this value is 0, it will never die.
     */
    protected long ttl;

    /** The list of artifacts that are managed by this Collection.*/
    protected List<CollectionItem> items;

    protected List<Recommendation> recommendations;

    /**
     * ThemeList by outputmode name.
     */
    protected Map<String, ThemeList> themeLists;

    /**
     * Settings by outputmode name.
     */
    protected Map<String, Settings> settings;

    /**
     * Constructor without arguments is necessary for GWT.
     */
    public DefaultCollection() {
    }


    public DefaultCollection(String uuid, long ttl, String name) {
        this.uuid            = uuid;
        this.ttl             = ttl;
        this.name            = name;
        this.items           = new ArrayList<CollectionItem>();
        this.themeLists      = new HashMap<String, ThemeList>();
        this.recommendations = new ArrayList<Recommendation>();
        this.settings        = new HashMap<String, Settings>();
    }


    /**
     * Creates a new DefaultCollection with a UUID.
     *
     * @param uuid The UUID.
     */
    public DefaultCollection(
        String uuid,
        long   ttl,
        String name,
        List<Recommendation> recs
    ) {
        this(uuid, ttl, name);

        this.recommendations = recs;
    }


    public DefaultCollection(
        String uuid,
        long   ttl,
        String name,
        List<Recommendation> recommendations,
        Map<String, ThemeList> themeLists)
    {
        this(uuid, ttl, name, recommendations);
        this.themeLists = themeLists;
    }


    public DefaultCollection(
        String uuid,
        long   ttl,
        String name,
        List<Recommendation> recommendations,
        Map<String, ThemeList> themeLists,
        Map<String, Settings> settings)
    {
        this(uuid, ttl, name, recommendations);
        this.themeLists = themeLists;
        this.settings = settings;
    }


    /**
     * Creates a new DefaultCollection with uuid and name.
     *
     * @param uuid The identifier of this collection.
     * @param name The name of this collection.
     * @param creation The creation time.
     */
    public DefaultCollection(String uuid, long ttl, String name, Date creation){
        this(uuid, ttl, name);

        this.creation = creation;
    }


    public String identifier() {
        return uuid;
    }


    public Date getCreationTime() {
        return creation;
    }


    /**
     * Returns now.
     * TODO candidate for removal?
     */
    public Date getLastAccess() {
        return new Date();
    }


    public long getTTL() {
        return ttl;
    }


    public void setTTL(long ttl) {
        this.ttl = ttl;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void addItem(CollectionItem item) {
        if (item != null) {
            items.add(item);
        }
    }


    public int getItemLength() {
        return items.size();
    }


    /** Returns item at index (0-based), or null if out of range. */
    public CollectionItem getItem(int idx) {
        if (idx >= getItemLength()) {
            return null;
        }

        return items.get(idx);
    }


    /**
     * Get item whose identifier is the given string.
     * @param uuid identifier of collection item (artifacts uuid).
     * @return CollectionItem whose identifier is given String,
     * null if not found.
     */
    public CollectionItem getItem(String uuid) {
        int size = getItemLength();
        for (int i = 0; i < size; i++) {
            CollectionItem item = getItem(i);
            if (item.identifier().equals(uuid)) {
                return item;
            }
        }
        return null;
    }


    public Map<String, OutputMode> getOutputModes() {
        Map<String, OutputMode> modes = new HashMap<String, OutputMode>();

        for (CollectionItem item: items) {
            List<OutputMode> itemModes = item.getOutputModes();

            if (itemModes != null) {
                for (OutputMode itemMode: itemModes) {
                    String name = itemMode.getName();
                    if (!modes.containsKey(name)) {
                        // we dont want duplicated OutputModes in our result.
                        modes.put(name, itemMode);
                    }
                }
            }
        }

        return modes;
    }


    /**
     * Returns ThemeList for given output name.
     */
    public ThemeList getThemeList(String outName) {
        if (themeLists != null) {
            return themeLists.get(outName);
        }

        return null;
    }


    /**
     * Returns Settings for given output name.
     */
    public Settings getSettings(String outName) {
        if (settings != null) {
            return settings.get(outName);
        }

        return null;
    }


    public void setSettings(Map<String, Settings> settings) {
        this.settings = settings;
    }


    public void addSettings(String outname, Settings settings) {
        if (this.settings == null) {
            this.settings = new HashMap<String, Settings>();
        }
        this.settings.put(outname, settings);
    }


    /** Set the outputname to themelist map. */
    public void setThemeLists(Map<String, ThemeList> map) {
        this.themeLists = map;
    }


    public List<Recommendation> getRecommendations() {
        return recommendations;
    }


    public void addRecommendation(Recommendation recommendation) {
        recommendations.add(recommendation);
    }


    public void addRecommendations(List<Recommendation> recommendations) {
        this.recommendations.addAll(recommendations);
    }


    /**
     * Returns true if a recommendation with given factory and id
     * is already member of this collection.
     */
    public boolean loadedRecommendation(Recommendation recommendation) {
        String factory = recommendation.getFactory();
        String dbids   = recommendation.getIDs();

        for (Recommendation in: recommendations) {
            String inFactory = in.getFactory();
            String inDbids   = in.getIDs();

            if (factory.equals(inFactory) && dbids.equals(inDbids)) {
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean hasItems() {
        return items.isEmpty();
    }

    /**
     * Returns the name of the collection or uuid if no name is set.
     */
    @Override
    public String getDisplayName() {
        if (this.name != null) {
            return this.name;
        }
        return this.uuid;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
