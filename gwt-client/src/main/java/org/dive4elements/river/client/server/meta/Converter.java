/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.meta;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.shared.model.DataCageTree;
import org.dive4elements.river.client.shared.model.DataCageNode;
import org.dive4elements.river.client.shared.model.AttrList;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.dive4elements.artifacts.common.utils.XMLUtils;

/**
 * Converts document parts (meta-data xml) to datacagenodes/trees,
 * which are shown in datacage widgets.
 */
public class Converter
{
    private static final Logger log = LogManager.getLogger(Converter.class);


    public interface NodeConverter
    {
        DataCageNode convert(Element node, Converter converter);

    } // interface NodeConverter

    public static class NameConverter implements NodeConverter {
        public DataCageNode convert(Element node, Converter converter) {
            //System.err.println("NameConverter called");
            DataCageNode out = new DataCageNode(
                node.getAttribute("name"),
                toAttrList(node.getAttributes()));
            converter.convertChildren(out, node);
            return out;
        }
    } // class NameConverter

    public static class I18NConverter implements NodeConverter {
        public DataCageNode convert(Element node, Converter converter) {
            //System.err.println("I18NConverter called");

            String name = node.getLocalName();
            String desc = node.hasAttribute("description")
                ? node.getAttribute("description")
                : "${" + name + "}";

            DataCageNode out =
                new DataCageNode(name, desc, toAttrList(node.getAttributes()));

            converter.convertChildren(out, node);
            return out;
        }
    } // I18NConverter

    private static Map<String, NodeConverter> converters =
        new HashMap<String, NodeConverter>();

    public static final NodeConverter NAME_CONVERTER = new NameConverter();
    public static final NodeConverter I18N_CONVERTER = new I18NConverter();

    static {
        converters.put("river",      NAME_CONVERTER);
        converters.put("gauge",      NAME_CONVERTER);
        converters.put("historical", NAME_CONVERTER);
        converters.put("column",     NAME_CONVERTER);
    }


    /** Trivial constructor. */
    public Converter() {
    }


    public DataCageTree convert(Document document) {
        log.debug("convert called");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(document));
        }

        ArrayList<DataCageNode> roots = new ArrayList<DataCageNode>();
        NodeList nodes = document.getChildNodes();
        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element ele = (Element)node;
                roots.add(convertElement(ele));
            }
        }
        return roots.isEmpty()
            ? new DataCageTree()
            : new DataCageTree(roots.get(0));
    }

    protected void convertChildren(DataCageNode parent, Element sub) {
        //System.err.println("convertChildren called");
        NodeList children = sub.getChildNodes();
        for (int i = 0, N = children.getLength(); i < N; ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childele = (Element)child;
                parent.addChild(convertElement(childele));

            if (childele.hasAttribute("description"))
                log.debug("nwd: " + childele.getAttribute("description"));
            }
        } // for all children
    }

    private DataCageNode convertElement(Element element) {
        String name = element.getLocalName();

        log.debug("search for name: " + name);

        NodeConverter converter = converters.get(name);
        if (converter == null) {
            converter = I18N_CONVERTER;
        }
        return converter.convert(element, this);

    }


    /**
     * Creates key/value pairs from Nodes Attributes.
     */
    public static AttrList toAttrList(NamedNodeMap nodeMap) {
        if (nodeMap == null) {
            return null;
        }
        int N = nodeMap.getLength();

        if (N == 0) {
            return null;
        }

        AttrList result = new AttrList(N);

        for (int i = 0; i < N; ++i) {
            Node node = nodeMap.item(i);
            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                Attr   attr  = (Attr)node;
                String key   = attr.getName();
                String value = attr.getValue();
                result.add(key, value);
            }
        }

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
