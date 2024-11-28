/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.DoubleArrayData;
import org.dive4elements.river.client.shared.model.IntegerArrayData;
import org.dive4elements.river.client.shared.model.IntegerData;
import org.dive4elements.river.client.shared.model.IntegerOptionsData;
import org.dive4elements.river.client.shared.model.IntegerRangeData;
import org.dive4elements.river.client.shared.model.MultiAttributeData;
import org.dive4elements.river.client.shared.model.MultiDataItem;
import org.dive4elements.river.client.shared.model.StringData;
import org.dive4elements.river.client.shared.model.StringOptionsData;
import org.dive4elements.river.client.shared.model.LongRangeData;
import org.dive4elements.river.client.shared.model.IntDataItem;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DataFactory {

    private static final Logger log = LogManager.getLogger(DataFactory.class);

    public static final String NS_URI = ArtifactNamespaceContext.NAMESPACE_URI;


    /**
     * Creates a new Data instance based on the <i>art:type</i> attribute of
     * <i>element</i>.
     *
     * @param element The Data element.
     *
     * @return a Data instance.
     */
    public static Data createDataFromElement(Element element) {
        String name  = element.getAttributeNS(NS_URI, "name");
        String type  = element.getAttributeNS(NS_URI, "type");
        String label = element.getAttributeNS(NS_URI, "label");

        label = label != null && label.length() > 0 ? label : name;

        try {
            log.debug("Create Data instance for: " + name + " | " + type);

            if (type == null || type.length() == 0) {
                return createDefaultData(element, name, label);
            }

            type = type.toLowerCase();

            if (type.equals(StringData.TYPE)) {
                return createStringData(element, name, label);
            }
            else if (type.equals(IntegerData.TYPE)) {
                return createIntegerData(element, name, label);
            }
            else if (type.equals(StringOptionsData.TYPE)) {
                return createStringOptionsData(element, name, label);
            }
            else if (type.equals(IntegerOptionsData.TYPE)) {
                return createIntegerOptionsData(element, name, label);
            }
            else if (type.equals(IntegerRangeData.TYPE)) {
                return createIntegerRangeData(element, name, label);
            }
            else if (type.equals(IntegerArrayData.TYPE)) {
                return createIntegerArrayData(element, name, label);
            }
            else if (type.equals(DoubleArrayData.TYPE)) {
                return createDoubleArrayData(element, name, label);
            }
            else if (type.equals(LongRangeData.TYPE)) {
                return createLongRangeData(element, name, label);
            }
            else if (type.equals(MultiAttributeData.TYPE)) {
                return createMultiAttributeData(element, name, label);
            }
            else {
                return createDefaultData(element, name, label);
            }
        }
        catch (Exception e) {
            log.error("Error while data creation for: " + name);
        }

        return null;
    }


    public static Data createMultiAttributeData(
        Element element,
        String name,
        String label) {
        return new MultiAttributeData(
            name,
            label,
            extractMultiDataItems(element),
            extractMeta(element));
    }


    private static Map<String, Map<String, String>> extractMeta(
        Element element
    ) {
        NodeList nl = element.getElementsByTagName("meta");
        int N = nl.getLength();
        if (N < 1) {
            log.debug("No meta data found for multi attribute data");
            return Collections.<String, Map<String, String>>emptyMap();
        }
        Map<String, Map<String, String>> map =
            new HashMap<String, Map<String, String>>();

        for (int i = 0; i < N; ++i) {
            Element e = (Element)nl.item(i);
            NamedNodeMap attrs = e.getAttributes();
            Map<String, String> kvs = new HashMap<String, String>();
            for (int j = 0, A = attrs.getLength(); j < A; ++j) {
                Attr attr = (Attr)attrs.item(j);
                kvs.put(attr.getName(), attr.getValue());
            }
            map.put(e.getTagName(), kvs);
        }

        return map;
    }


    protected static DataItem[] extractMultiDataItems(Element element) {
        NodeList itemList = (NodeList) XMLUtils.xpath(
            element,
            "art:item",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (itemList == null || itemList.getLength() == 0) {
            log.debug("No old data items found.");
            return null;
        }

        int count = itemList.getLength();

        MultiDataItem[] items = new MultiDataItem[count];

         for (int i = 0; i < count; i++) {
             Element tmp = (Element) itemList.item(i);

             HashMap<String, String> data = new HashMap<String, String>();
             String label = tmp.getAttributeNS(NS_URI, "label");
             NamedNodeMap attributes = tmp.getAttributes();
             for (int j = 0, L = attributes.getLength(); j < L; j++) {
                 Node n = attributes.item(j);
                 if (n.getNodeName().equals("label")) {
                     continue;
                 }
                 data.put(n.getNodeName(), n.getNodeValue());
             }
             items[i] = new MultiDataItem(label, label, data);
         }
         return items;
    }


    /**
     * This method creates a new instance of DefaultData which has no real type
     * set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of DefaultData.
     */
    protected static Data createDefaultData(
        Element ele,
        String name,
        String label
    ) {
        log.debug("Create new DefaultData");
        return new DefaultData(name, label, "default", extractDataItems(ele));
    }


    /**
     * This method creates a new instance of StringData which has a type
     * "string" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of StringData.
     */
    protected static Data createStringData(
        Element ele,
        String name,
        String label
    ) {
        return new StringData(name, label, extractDataItems(ele));
    }


    /**
     * This method creates a new instance of DefaultData which has a type
     * "integer" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of IntegerData.
     */
    protected static Data createIntegerData(
        Element ele,
        String name,
        String label
    ) {
        return new IntegerData(name, label, extractDataItems(ele));
    }


    /**
     * This method creates a new instance of StringOptionsData which has a type
     * "options" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of StringOptionsData.
     */
    protected static Data createStringOptionsData(
        Element ele,
        String name,
        String label
    ) {
        return new StringOptionsData(name, label, extractDataItems(ele));
    }


    /**
     * This method creates a new instance of DefaultData which has a type
     * "intoptions" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of IntegerOptionsData.
     */
    protected static Data createIntegerOptionsData(
        Element ele,
        String name,
        String label
    ) {
        return new IntegerOptionsData(name, label, extractDataItems(ele));
    }


    /**
     * This method creates a new instance of DefaultData which has a type
     * "intrange" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of IntegerRangeData.
     */
    protected static Data createIntegerRangeData(
        Element ele,
        String name,
        String label
    ) {
        DataItem[] items    = extractDataItems(ele);
        String     rawValue = items[0].getStringValue();

        String[] minmax = rawValue.split(";");

        return new IntegerRangeData(
            name,
            label,
            Integer.valueOf(minmax[0]),
            Integer.valueOf(minmax[1]));
    }


    /**
     * This method creates a new instance of DefaultData which has a type
     * "integerarray" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of IntegerArrayData.
     */
    protected static Data createIntegerArrayData(
        Element ele,
        String name,
        String label
    ) {
        IntDataItem[] items    = extractIntDataItems(ele);
        return new IntegerArrayData(name, label, items);
    }


    /**
     * This method creates a new instance of DefaultData which has a type
     * "doublearray" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of DoubleArrayData.
     */
    protected static Data createDoubleArrayData(
        Element ele,
        String name,
        String label
    ) {
        DataItem[] items    = extractDataItems(ele);
        String     rawValue = items[0].getStringValue();

        String[] values  = rawValue.split(";");
        double[] doubles = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            try {
                doubles[i] = Double.valueOf(values[i]);
            }
            catch (NumberFormatException nfe) {
                log.warn("Error while parsing DoubleArrayData: " + nfe);
            }
        }

        return new DoubleArrayData(name, label, doubles);
    }


    /**
     * This method extracts the art:item elements placed under <i>elements</i>.
     *
     * @param element A data node that contains items.
     *
     * @return a list of DataItems.
     */
    protected static DataItem[] extractDataItems(Element element) {
        NodeList itemList = (NodeList) XMLUtils.xpath(
            element,
            "art:item",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (itemList == null || itemList.getLength() == 0) {
            log.debug("No data items found.");
            return null;
        }

        int count = itemList.getLength();

        DataItem[] items = new DataItem[count];

        log.debug("There are " + count + " data items in element.");

        for (int i = 0; i < count; i++) {
            Element tmp = (Element) itemList.item(i);

            String value = tmp.getAttributeNS(NS_URI, "value");
            String label = tmp.getAttributeNS(NS_URI, "label");

            log.debug("Found data item:");
            log.debug("   label: " + label);
            log.debug("   value: " + value);

            items[i] = new DefaultDataItem(label, label, value);
        }

        return items;
    }


    /**
     * This method extracts the art:item elements placed under <i>elements</i>.
     *
     * @param element A data node that contains items.
     *
     * @return a list of DataItems.
     */
    protected static IntDataItem[] extractIntDataItems(Element element) {
        NodeList itemList = (NodeList) XMLUtils.xpath(
            element,
            "art:item",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (itemList == null || itemList.getLength() == 0) {
            log.debug("No old data items found.");
            return null;
        }

        int count = itemList.getLength();

        IntDataItem[] items = new IntDataItem[count];

         for (int i = 0; i < count; i++) {
             Element tmp = (Element) itemList.item(i);

             String value = tmp.getAttributeNS(NS_URI, "value");
             String label = tmp.getAttributeNS(NS_URI, "label");

             try {
                 int data = Integer.parseInt(value);
                 items[i] = new IntDataItem(label, label, data);
             }
             catch(NumberFormatException nfe) {
                 log.debug(nfe, nfe);
             }
         }
         return items;
    }

    /**
     * This method creates a new instance of LongRangeData which has a type
     * "longrange" set.
     *
     * @param ele The Data element.
     * @param name The name of the Data instance.
     *
     * @return an instance of IntegerRangeData.
     */
    protected static Data createLongRangeData(
        Element ele,
        String name,
        String label
    ) {
        DataItem[] items    = extractDataItems(ele);
        String     rawValue = items[0].getStringValue();

        String[] minmax = rawValue.split(";");

        return new LongRangeData(
            name,
            label,
            Long.valueOf(minmax[0]),
            Long.valueOf(minmax[1]));
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
