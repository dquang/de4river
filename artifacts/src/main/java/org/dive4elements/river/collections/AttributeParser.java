/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.Settings;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.ManagedDomFacet;
import org.dive4elements.river.exports.ChartSettings;

/**
 * Access parts of the Attribute parts of a FLYSCollections description
 * document.
 */
public class AttributeParser {

    /** Constant XPath that points to the outputmodes of an artifact. */
    public static final String XPATH_ARTIFACT_OUTPUTMODES =
        "/art:attribute/art:outputs/art:output";


    private static Logger log = LogManager.getLogger(AttributeParser.class);


    protected Document attributeDocument;

    protected CollectionAttribute attribute;


    /** Just store reference to document. */
    public AttributeParser(Document attributeDocument) {
        this.attributeDocument = attributeDocument;
    }


    public void parse() {
        log.debug("AttributeParser.parse");

        attribute = new CollectionAttribute();

        NodeList outs = (NodeList) XMLUtils.xpath(
            attributeDocument,
            XPATH_ARTIFACT_OUTPUTMODES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = outs != null ? outs.getLength() : 0;

        log.debug("Attribute has " + num + " outputs.");

        for (int i = 0; i < num; i++) {
            Node out = outs.item(i);

            parseOutput(out);
        }
    }


    public CollectionAttribute getCollectionAttribute() {
        if (attribute == null) {
            parse();
        }

        return attribute;
    }


    public Document getAttributeDocument() {
        return attributeDocument;
    }


    public Map<String, Output> getOuts() {
        return attribute.getOutputs();
    }


    /**
     * Access all facets.
     * @return list of all facets.
     */
    public List<Facet> getFacets() {
        return attribute.getFacets();
    }


    protected void parseOutput(Node out) {
        String name = ((Element)out).getAttribute("name");

        if (name.length() == 0) {
            log.warn("No Output name specified. Cancel parsing!");
            return;
        }

        Output o = attribute.getOutput(name);

        if (o == null) {
            log.debug("Create new output: " + name);

            o = new DefaultOutput(name, null, null);
            attribute.addOutput(name, o);
        }

        parseSettings(out, name);
        parseItems(out, name);
    }

    private static final Node getChild(Element element, String name) {
        NodeList children = element.getChildNodes();
        for (int i = 0, N = children.getLength(); i < N; ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
            && child.getLocalName().equals(name)) {
                return child;
            }
        }
        return null;
    }


    protected void parseSettings(Node out, String outname) {
        Node settingsNode = getChild((Element)out, "settings");

        if (settingsNode == null) {
            log.debug("No Settings found for Output '" + outname + "'");
            return;
        }

        Settings settings = ChartSettings.parse(settingsNode);
        attribute.setSettings(outname, settings);
    }


    protected void parseItems(Node out, String outname) {
        String uri = ArtifactNamespaceContext.NAMESPACE_URI;
        Element element = (Element)out;

        NodeList themes = element.getElementsByTagNameNS(uri, "facet");

        int num = themes.getLength();

        log.debug("Output has " + num + " themes.");

        for (int i = 0; i < num; i++) {
            Element theme = (Element) themes.item(i);
            if (theme.getParentNode() == out) {
                attribute.addFacet(outname, new ManagedDomFacet(theme));
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
