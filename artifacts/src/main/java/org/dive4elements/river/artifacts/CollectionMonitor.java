/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.Hook;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.state.Output;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.artifacts.datacage.Recommendations;

/** Monitors collection changes. */
public class CollectionMonitor implements Hook {

    public static final String CACHE_NAME = "recommendations";

    @Override
    public void setup(Node cfg) {
    }


    @Override
    public void execute(Artifact artifact, CallContext context, Document doc) {
        D4EArtifact flys = (D4EArtifact) artifact;

        // Do not generate recommendations for a loaded artifact.
        String out = flys.getBoundToOut();
        if (out != null && !out.isEmpty()) {
            return;
        }

        NodeList results = doc.getElementsByTagNameNS(
            ArtifactNamespaceContext.NAMESPACE_URI, "result");

        if (results.getLength() < 1) {
            return;
        }

        Element result = (Element)results.item(0);

        result.appendChild(getRecommendedElement(flys, context, doc));
    }

    protected Element getRecommendedElement(
        D4EArtifact artifact,
        CallContext context,
        Document    doc
    ) {
        String [] outs = extractOutputNames(artifact, context);

        Element recommendations = null;

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache != null) {
            String key = generateCacheKey(artifact, outs);

            net.sf.ehcache.Element ce = cache.get(key);
            if (ce != null) { // Found in cache.
                Element e = (Element)ce.getValue();
                // Sync to avoid thread issues with XML DOM docs.
                synchronized (e.getOwnerDocument()) {
                    recommendations = (Element)doc.importNode(e, true);
                }
            } else { // Not found in cache -> generate it.
                Element r = createElement(XMLUtils.newDocument());

                Recommendations.getInstance().recommend(
                    artifact, null, outs,
                    getNoneUserSpecificParameters(artifact, context), r);

                recommendations = (Element)doc.importNode(r, true);

                cache.put(new net.sf.ehcache.Element(key, r));
            }
        } else { // No cache configured -> append directly.

            recommendations = createElement(doc);

            Recommendations.getInstance().recommend(
                artifact, null, outs,
                getNoneUserSpecificParameters(artifact, context),
                recommendations);
        }

        return recommendations;
    }

    private static final Element createElement(Document doc) {
        ElementCreator creator = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        return creator.create("recommended-artifacts");
    }

    private static final String generateCacheKey(
        D4EArtifact artifact,
        String [] outs
    ) {
        StringBuilder sb = new StringBuilder(artifact.hash());
        // XXX: The hash really should be unique enough.
        for (String out: outs) {
            sb.append(';').append(out);
        }
        return sb.toString();
    }


    /**
     * Get outputnames from current state (only the ones for which
     * facets exist).
     */
    private static final String [] extractOutputNames(
        D4EArtifact flys,
        CallContext context
    ) {
        if (flys instanceof ChartArtifact) {
            return new String[0];
        }

        List<Output> outs = flys.getCurrentOutputs(context);

        int num = outs == null ? 0 : outs.size();

        String[] names = new String[num];

        for (int i = 0; i < num; i++) {
            names[i] = outs.get(i).getName();
        }

        // Sort them to make cache key generation consistent.
        Arrays.sort(names);

        return names;
    }


    /**
     * Creates Map from Strings "recommended" to "true".
     */
    private static final Map<String, Object> getNoneUserSpecificParameters(
        D4EArtifact flys,
        CallContext context)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recommended", "true");

        return params;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
