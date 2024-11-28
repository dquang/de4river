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

import org.dive4elements.artifactdatabase.XMLService;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import net.sf.ehcache.Cache;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CacheInvalidationService
extends      XMLService
{
    @Override
    public Document processXML(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        Document result = XMLUtils.newDocument();

        Element all = result.createElement("caches");

        NodeList caches = data.getElementsByTagName("cache");

        for (int i = 0, C = caches.getLength(); i < C; ++i) {
            Element c = (Element)caches.item(i);
            String name = c.getAttribute("name");
            Element e = result.createElement("cache");
            e.setAttribute("name", name);
            Cache cache = CacheFactory.getCache(name);
            if (cache != null) {
                cache.removeAll();
                e.setTextContent("All elements removed.");
            }
            else {
                e.setTextContent("Error: Cache not found.");
            }
            all.appendChild(e);
        }

        result.appendChild(all);

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
