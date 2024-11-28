/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SessionHolder;

import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.CrossSectionLine;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import java.util.concurrent.ConcurrentSkipListMap;

import net.sf.ehcache.Cache;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Service to find the next/previous km (measurement) of cross sections.
 * Looking at the query for a single cross-section id at a single km, the
 * service does the following:
 *
 * It returns the km itself if a measurement at that km was found and
 * the N nearest other measurement points in both directions.
 *
 * That means, you can pass N=0 to find out whether a measurement at given km
 * exists.
 *
 * If less than N neighbours exist in one direction, less are delivered
 * (e.g. given measurements at [0,2,3,4,5,7,8,9] a query for km=8, N=3 will
 * result in [4,5,7,8,9]).
 */
public class CrossSectionKMService
extends      D4EService
{
    private static Logger log =
        LogManager.getLogger(CrossSectionKMService.class);

    public static final String CACHE_NAME = "cross-section-kms";


    /** Trivial constructor. */
    public CrossSectionKMService() {
    }


    /**
     * @param data
     */
    @Override
    public Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("CrossSectionKMService.doProcess");

        NodeList crossSectionNodes =
            data.getElementsByTagName("art:cross-section");

        Document document = XMLUtils.newDocument();

        Element all = document.createElement("cross-sections");

        for (int i = 0, CS = crossSectionNodes.getLength(); i < CS; ++i) {
            Element crossSectionElement = (Element)crossSectionNodes.item(i);

            String idString = crossSectionElement.getAttribute("id");
            String kmString = crossSectionElement.getAttribute("km");
            String neighborsString = crossSectionElement.getAttribute("n");

            if (idString.length() == 0 || kmString.length() == 0) {
                log.debug("missing attributes in cross-section element");
                continue;
            }

            double  km;
            Integer crossSectionId;
            int     N = 2;

            try {
                km             = Double.parseDouble(kmString);
                crossSectionId = Integer.valueOf(idString);

                if (neighborsString.length() > 0) {
                    N = Integer.parseInt(neighborsString);
                }
            }
            catch (NumberFormatException nfe) {
                log.debug("converting number failed", nfe);
                continue;
            }

            NavigableMap<Double, Integer> map = getKms(crossSectionId);

            if (map == null) {
                log.debug("cannot find cross section " + crossSectionId);
                continue;
            }

            Deque<Map.Entry<Double, Integer>> result =
                nearestNeighbors(map, km, N);

            if (!result.isEmpty()) {
                Element csE = document.createElement("cross-section");
                csE.setAttribute("id", idString);
                for (Map.Entry<Double, Integer> entry: result) {
                    Element lineE = document.createElement("line");
                    lineE.setAttribute(
                        "line-id", String.valueOf(entry.getValue()));
                    lineE.setAttribute(
                        "km", String.valueOf(entry.getKey()));
                    csE.appendChild(lineE);
                }
                all.appendChild(csE);
            }
        }

        document.appendChild(all);

        return document;
    }

    public static NavigableMap<Double, Integer> getKms(int crossSectionId) {

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            return getUncached(crossSectionId);
        }

        NavigableMap<Double, Integer> map;

        net.sf.ehcache.Element element = cache.get(crossSectionId);
        if (element == null) {
            map = getUncached(crossSectionId);
            if (map != null) {
                element = new net.sf.ehcache.Element(
                    crossSectionId, map);
                cache.put(element);
            }
        }
        else {
            map = (NavigableMap<Double, Integer>)element.getValue();
        }

        return map;
    }


    /**
     * @param km  the kilometer from which to start searching for other
     *            measurements
     * @param N   number of neighboring measurements to find.
     */
    public static Deque<Map.Entry<Double, Integer>> nearestNeighbors(
        NavigableMap<Double, Integer> map,
        double                        km,
        int                           N
    ) {
        Deque<Map.Entry<Double, Integer>> result =
            new ArrayDeque<Map.Entry<Double, Integer>>(2*N);

        Integer v = map.get(km);

        if (v != null) {
            result.add(new AbstractMap.SimpleEntry<Double, Integer>(km, v));
        }

        int i = 0;
        for (Map.Entry<Double, Integer> entry:
             map.headMap(km, false).descendingMap().entrySet()) {
            if (i++ >= N) {
                break;
            }
            result.addFirst(entry);
        }

        i = 0;
        for (Map.Entry<Double, Integer> entry:
             map.tailMap(km, false).entrySet()) {
            if (i++ >= N) {
                break;
            }
            result.addLast(entry);
        }

        return result;
    }


    /**
     * @param crossSectionId id of queried cross-section (in db).
     * @return Mapping from kilometer to db-id.
     */
    public static NavigableMap<Double, Integer> getUncached(
        Integer crossSectionId
    ) {
        NavigableMap<Double, Integer> result =
            new ConcurrentSkipListMap<Double, Integer>();

        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from CrossSection where id=:id");
        query.setParameter("id", crossSectionId);

        List<CrossSection> crossSections = query.list();
        if (crossSections.isEmpty()) {
            return null;
        }

        CrossSection crossSection = crossSections.get(0);
        List<CrossSectionLine> lines = crossSection.getLines();

        for (CrossSectionLine line: lines) {
            Double  km = line.getKm().doubleValue();
            Integer id = line.getId();
            result.put(km, id);
        }

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
