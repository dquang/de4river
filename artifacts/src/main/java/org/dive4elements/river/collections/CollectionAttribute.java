/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.Settings;


/**
 * Create attribute part of collection document.
 *
 * Has outputs, settings, facets and list of loaded recommendations.
 */
public class CollectionAttribute {

    /** Privately owned log. */
    private static final Logger log =
        LogManager.getLogger(CollectionAttribute.class);

    protected ElementCreator ec;

    protected Map<String, Output> outputMap;

    protected Node loadedRecommendations;


    public CollectionAttribute() {
    }


    public void addOutput(String key, Output output) {
        if (outputMap == null) {
            outputMap = new HashMap<String, Output>();
        }

        if (key != null && key.length() > 0 && output != null) {
            outputMap.put(
                key,
                new DefaultOutput(
                    output.getName(),
                    output.getDescription(),
                    output.getMimeType(),
                    new ArrayList<Facet>(),
                    output.getType()));
        }
    }


    /** Remove outputs without facets from outputMap. */
    public void cleanEmptyOutputs() {
        if (outputMap == null) {
            return;
        }

        List<String> removeUs = new ArrayList<String>();

        for (Map.Entry<String, Output> entry: outputMap.entrySet()) {
            Output o = entry.getValue();

            List<Facet> facets = o.getFacets();
            if (facets == null || facets.isEmpty()) {
                removeUs.add(entry.getKey());
            }
        }

        for (String key: removeUs) {
            outputMap.remove(key);
        }
    }


    public void setSettings(String outputKey, Settings settings) {
        if (settings == null) {
            log.warn("Tried to set empty Settings for '" + outputKey + "'");
            return;
        }

        if (outputMap == null) {
            log.warn("Tried to add settings but no Outputs are existing yet.");
            return;
        }

        Output output = outputMap.get(outputKey);

        if (output == null) {
            log.warn("Tried to add settings for unknown Output: " + outputKey);
            return;
        }

        output.setSettings(settings);
    }


    public void addFacet(String outputKey, Facet facet) {
        if (facet == null) {
            log.warn("Tried to add null facet.");
            return;
        }

        if (outputMap == null) {
            log.warn("Tried to add facet but no Outputs are existing yet.");
            return;
        }

        Output output = outputMap.get(outputKey);

        if (output == null) {
            log.warn("Tried to add facet for unknown Output: " + outputKey);
            return;
        }

        log.debug("Add facet for '" + outputKey + "': " + facet.getName());
        output.addFacet(facet);
    }


    public void setLoadedRecommendations(Node loadedRecommendations) {
        // TODO Replace this Node with a Java class object.
        this.loadedRecommendations = loadedRecommendations;
    }


    /** Empty facets list for outputKey output. */
    public void clearFacets(String outputKey) {
        if (outputKey == null || outputKey.length() == 0) {
            log.warn("Tried to clear Facets, but no Output key specified!");
            return;
        }

        if (outputMap == null) {
            log.warn("Tried to clear Facets, but no Outputs existing!");
            return;
        }

        Output output = outputMap.get(outputKey);
        if (output == null) {
            log.warn("Tried to clear Facets for unknown Out: " + outputKey);
            return;
        }

        output.setFacets(new ArrayList<Facet>());
    }


    public Document toXML() {
        Document doc = XMLUtils.newDocument();

        ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("attribute");

        appendOutputs(root);
        appendLoadedRecommendations(root);

        doc.appendChild(root);

        return doc;
    }

    /** True if output with outputName is found. */
    public boolean hasOutput(String outputName) {
        return getOutput(outputName) != null;
    }

    public Map<String, Output> getOutputs() {
        return outputMap;
    }


    public Output getOutput(String name) {
        if (name == null || name.length() == 0) {
            log.warn("No Output name specified.");
            return null;
        }

        if (outputMap == null || outputMap.isEmpty()) {
            log.warn("Tried to retrieve Output, but no Outputs existing.");
            return null;
        }

        return outputMap.get(name);
    }


    public List<Facet> getFacets(String output) {
        if (output == null || output.length() == 0) {
            log.warn("No Output name specified.");
            return new ArrayList<Facet>();
        }

        if (outputMap == null) {
            log.warn("Tried to retrieve facets, but no Outputs existing.");
            return new ArrayList<Facet>();
        }

        Output o = outputMap.get(output);

        if (o == null) {
            log.warn("No Output '" + output + "' existing.");
            return new ArrayList<Facet>();
        }

        return o.getFacets();
    }


    public List<Facet> getFacets() {
        List<Facet> allFacets = new ArrayList<Facet>();

        if (outputMap == null || outputMap.isEmpty()) {
            log.warn("No Outputs existing.");
            return allFacets;
        }

        for (String outputName: outputMap.keySet()) {
            allFacets.addAll(getFacets(outputName));
        }

        return allFacets;
    }


    protected void appendOutputs(Element root) {
        if (outputMap == null || outputMap.isEmpty()) {
            log.warn("No outputs to append.");
            return;
        }

        log.debug("Append " + outputMap.size() + " Output Elements.");

        Element outputsEl = ec.create("outputs");

        for (Map.Entry<String, Output> entry: outputMap.entrySet()) {
            appendOutput(outputsEl, entry.getKey(), entry.getValue());
        }

        root.appendChild(outputsEl);
    }


    protected void appendOutput(Element root, String name, Output output) {
        if (name == null || name.length() == 0 || output == null) {
            log.warn("Tried to appendOutput, but Output is invalid.");
            return;
        }

        log.debug("Append Output Element for '" + name + "'");

        Element outputEl = ec.create("output");
        ec.addAttr(outputEl, "name", name);

        appendSettings(outputEl, output.getSettings());
        appendFacets(outputEl, output.getFacets());

        root.appendChild(outputEl);
    }


    protected void appendSettings(Element root, Settings settings) {
        if (settings == null) {
            log.warn("Tried to append Settings, but Settings is empty!");
            return;
        }

        settings.toXML(root);
    }


    protected void appendFacets(Element root, List<Facet> facets) {
        if (facets == null || facets.isEmpty()) {
            log.warn("Tried to append 0 Facets.");
            return;
        }

        Document owner = root.getOwnerDocument();

        log.debug("Append " + facets.size() + " facets.");

        for (Facet facet: facets) {
            Node facetNode = facet.toXML(owner);

            if (facetNode != null) {
                root.appendChild(facetNode);
            }
        }
    }


    protected void appendLoadedRecommendations(Element root) {
        if (loadedRecommendations == null) {
            log.debug("No loaded recommendations existing yet.");
            return;
        }

        Document owner = root.getOwnerDocument();

        root.appendChild(owner.importNode(loadedRecommendations, true));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
