/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;

public class ToLoad implements Serializable
{

    /** Two strings. */
    public static class StringTriple {
        public String first;
        public String second;
        public String third;
        public StringTriple(String first, String second, String third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
        @Override
        public int hashCode() {
            return first.hashCode() + second.hashCode() + third.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StringTriple)) {
                return false;
            }
            StringTriple other = (StringTriple) o;
            return second.equals(other.second)
                && first.equals(other.first)
                && third.equals(other.third);
        }
    }
    public static final String SYNTHETIC_KEY = "key-";

    protected Map<String, Map<StringTriple, ArtifactFilter>> artifacts;

    public ToLoad() {
        artifacts = new HashMap<String, Map<StringTriple, ArtifactFilter>>();
    }

    public static final String uniqueKey(Map<?, ?> map) {
        int idx = map.size();

        String key = SYNTHETIC_KEY + idx;
        while (map.containsKey(key)) {
            key = SYNTHETIC_KEY + ++idx;
        }
        return key;
    }

    public void add(
         String artifactName,
         String factory,
         String out,
         String name,
         String ids,
         String displayName
     ) {
        add(artifactName, factory, out, name, ids, displayName, null);
     }

    public void add(
        String artifactName,
        String factory,
        String out,
        String name,
        String ids,
        String displayName,
        String targetOut
    ) {
        GWT.log("Adding artifact: " + artifactName + " Factory: " + factory +
                " Out: " + out + " Name: " + name + " Ids: " + ids +
                " Display Name: " + displayName + " Target Out: " + targetOut);

        if (artifactName == null) {
            artifactName = uniqueKey(artifacts);
        }

        Map<StringTriple, ArtifactFilter> artifact = artifacts.get(
            artifactName);

        if (artifact == null) {
            artifact = new HashMap<StringTriple, ArtifactFilter>();
            artifacts.put(artifactName, artifact);
        }

        ArtifactFilter filter = artifact.get(factory);
        if (filter == null) {
            filter = new ArtifactFilter(factory);
            artifact.put(new StringTriple(
                    factory, displayName, targetOut), filter);
        }

        filter.add(out, name, ids);
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

    public List<Recommendation> toRecommendations() {
        List<Recommendation> recommendations = new ArrayList<Recommendation>();

        for (Map.Entry<String, Map<StringTriple, ArtifactFilter>> all:
            artifacts.entrySet()
        ) {
            String masterArtifact = all.getKey();

            if (masterArtifact.startsWith(SYNTHETIC_KEY)) { // system data
                masterArtifact = null;
            }

            for (Map.Entry<StringTriple, ArtifactFilter> entry:
                all.getValue().entrySet()
            ) {
                StringTriple triple = entry.getKey();
                String factory = triple.first;
                String targetOut = triple.third;
                ArtifactFilter artifactFilter = entry.getValue();

                String                ids;
                Recommendation.Filter filter;

                if (masterArtifact == null) { // system data
                    ids    = artifactFilter.collectIds();
                    filter = null;
                }
                else { // user specific
                    ids    = null;
                    filter = artifactFilter.toFilter();
                }

                Recommendation recommendation = new Recommendation(
                    factory, ids, masterArtifact, filter, targetOut);
                recommendation.setDisplayName(triple.second);

                recommendations.add(recommendation);
            }
        }

        return recommendations;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
