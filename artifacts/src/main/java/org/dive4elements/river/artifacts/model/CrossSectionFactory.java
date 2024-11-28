/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.hibernate.Session;
import org.hibernate.Query;


/**
 * Get Cross Sections.
 */
public class CrossSectionFactory {

    protected final static String CACHE_NAME = "cross_sections";

    // TODO use caching consistently, streamline access.
    /**
     * Get CrossSections for an instantiated River.
     *
     * @param river river object.
     *
     * @return List of Cross Sections of river.
     */
    public static List<CrossSection> getCrossSections(River river) {
        return getCrossSections(river.getName());
    }


    /**
     * Get Cross Sections for a river by name.
     *
     * @param riverName name of the river of interest.
     *
     * @return List of Cross Sections of river.
     */
    public static List<CrossSection> getCrossSections(String riverName) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from CrossSection where river.name = :rivername");
        query.setParameter("rivername", riverName);
        return query.list();
    }



    /**
     *  Get a specific CrossSection from db.
     *  @param id The dbid of the cross-section to load.
     */
    public static CrossSection getCrossSection(int id) {
        Cache cache = CacheFactory.getCache(CACHE_NAME);
        if (cache != null) {
            Element element = cache.get(Integer.valueOf(id));
            if (element != null) {
                return (CrossSection) element.getValue();
            }
        }

        CrossSection section = getCrossSectionUncached(id);
        if (cache != null) {
            Element element = new Element(Integer.valueOf(id), section);
            cache.put(element);
        }

        return section;
    }


    /** Get specific CrossSection from database. */
    protected static CrossSection getCrossSectionUncached(int id) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
                "from CrossSection where id=:id");
        query.setParameter("id", id);
        List<CrossSection> list = query.list();
        return list.isEmpty() ? null : list.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
