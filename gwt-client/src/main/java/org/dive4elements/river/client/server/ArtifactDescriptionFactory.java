/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultArtifactDescription;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.DefaultOutputMode;
import org.dive4elements.river.client.shared.model.DoubleArrayData;
import org.dive4elements.river.client.shared.model.DoubleRangeData;
import org.dive4elements.river.client.shared.model.IntegerArrayData;
import org.dive4elements.river.client.shared.model.IntegerRangeData;
import org.dive4elements.river.client.shared.model.IntegerOptionsData;
import org.dive4elements.river.client.shared.model.LongRangeData;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.WQDataItem;


/**
 * This factory class helps creating an {@link ArtifactDescription} based on the
 * DESCRIBE document of an artifact returned by the artifact server. Use the
 * {@link createArtifactDescription(org.w3c.dom.Document)} method with the
 * DESCRIBE document to create such an {@link ArtifactDescription}.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ArtifactDescriptionFactory {

    private static final Logger log =
        LogManager.getLogger(ArtifactDescriptionFactory.class);


    public static final String XPATH_STATE_NAME = "@art:name";

    public static final String XPATH_UIPROVIDER = "@art:uiprovider";

    public static final String XPATH_HELP_TEXT = "@art:helpText";

    public static final String XPATH_REACHABLE_STATE = "art:state";

    public static final String XPATH_STATIC_STATE_NODE = "art:state";

    public static final String XPATH_STATIC_DATA_NODE = "art:data";

    public static final String XPATH_STATIC_ITEM_NODE = "art:item";

    public static final String XPATH_RECOMMENDED_ARTIFACTS =
        "/art:result/art:recommended-artifacts//*[@factory]";

    /**
     * This method creates the {@link ArtifactDescription} of the DESCRIBE
     * document <i>doc</i>.
     *
     * @param doc A DESCRIBE document.
     *
     * @return the {@link ArtifactDescription}.
     */
    public static ArtifactDescription createArtifactDescription(Document doc) {
        log.debug("ArtifactDescriptionFactory.createArtifactDescription");

        Node currentState = ClientProtocolUtils.getCurrentState(doc);
        Node staticNode   = ClientProtocolUtils.getStaticUI(doc);
        Node dynamicNode  = ClientProtocolUtils.getDynamicUI(doc);
        Node reachable    = ClientProtocolUtils.getReachableStates(doc);
        NodeList outputs  = ClientProtocolUtils.getOutputModes(doc);

        String state = (String) XMLUtils.xpath(
            currentState,
            XPATH_STATE_NAME,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);

        log.debug("Current state name: " + state);

        DataList currentData = extractCurrentData(dynamicNode, state);
        DataList[] old       = extractOldData(staticNode);
        String[] states      = extractReachableStates(reachable);
        OutputMode[] outs    = extractOutputModes(outputs);
        Recommendation[] rec = extractRecommendedArtifacts(doc);

        return new DefaultArtifactDescription(
            old,
            currentData,
            state,
            states,
            outs,
            rec);
    }


    /**
     * This method extracts the data that the user is able to enter in the
     * current state of the artifact.
     *
     * @param dynamicNode The dynamic node of the DESCRIBE document.
     * @param state The name of the current state.
     *
     * @return A {@link Data} object that represents the data which might be
     * entered by the user in the current state or null, if no data might be
     * entered.
     */
    protected static DataList extractCurrentData(
        Node dynamicNode,
        String state
    ) {
        log.debug("ArtifactDescriptionFactory.extractCurrentData");

        NodeList data     = ClientProtocolUtils.getSelectNode(dynamicNode);
        String help       = extractHelpText(dynamicNode);
        String uiProvider = extractUIProvider(dynamicNode);

        if (data == null || data.getLength() == 0) {
            return null;
        }

        int      dataNum = data.getLength();
        DataList list    = new DataList(state, dataNum, uiProvider, null, help);

        for (int i = 0; i < dataNum; i++) {
            Element   d  = (Element) data.item(i);
            String label = ClientProtocolUtils.getLabel(d);
            String name  = XMLUtils.xpathString(
                d, "@art:name", ArtifactNamespaceContext.INSTANCE);
            String type  = XMLUtils.xpathString(
                d, "@art:type", ArtifactNamespaceContext.INSTANCE);

            log.debug("Create new IntegerRangeData object for: " + name);
            log.debug("New Data is from type: " + type);

            // TODO replace with DataFactory.

            if (type == null || type.length() == 0) {
                NodeList   choices   = ClientProtocolUtils.getItemNodes(d);
                DataItem[] dataItems = extractCurrentDataItems(choices);
                DataItem   def       = extractDefaultDataItem(d);

                list.add(new DefaultData(name, label, null, dataItems, def));
            }
            else if (type.equals("intrange")) {
                String min = ClientProtocolUtils.getMinNode(d);
                String max = ClientProtocolUtils.getMaxNode(d);

                String defMin = ClientProtocolUtils.getDefMin(d);
                String defMax = ClientProtocolUtils.getDefMax(d);

                try {
                    int lower = Integer.parseInt(min);
                    int upper = Integer.parseInt(max);

                    if (defMin != null && defMax != null) {
                        list.add(new IntegerRangeData(
                                name, label,
                                lower, upper,
                                Integer.parseInt(defMin),
                                Integer.parseInt(defMax)));
                    }
                    else {
                        list.add(
                            new IntegerRangeData(name, label, lower, upper));
                    }
                }
                catch (NumberFormatException nfe) {
                    log.warn("NumberFormatException: ", nfe);
                }
            }
            else if (type.equals("longrange")) {
                String min = ClientProtocolUtils.getMinNode(d);
                String max = ClientProtocolUtils.getMaxNode(d);

                String defMin = ClientProtocolUtils.getDefMin(d);
                String defMax = ClientProtocolUtils.getDefMax(d);

                try {
                    long lower = Long.valueOf(min);
                    long upper = Long.valueOf(max);

                    if (defMin != null && defMax != null) {
                        list.add(new LongRangeData(
                                name, label,
                                lower, upper,
                                Long.valueOf(defMin),
                                Long.valueOf(defMax)));
                    }
                }
                catch (NumberFormatException nfe) {
                    log.warn("NumberFormatException: ", nfe);
                }
            }
            else if (type.equals("intarray")) {
                list.add(new IntegerArrayData(name, label, null));
            }
            else if (type.equals("intoptions")
                && uiProvider.equals("parameter-matrix")
            ) {
                list.add(DataFactory.createIntegerOptionsData(d, name, label));
            }
            else if (type.equals("options")) {
                list.add(DataFactory.createStringOptionsData(d, name, label));
            }
            else if (type.equals("intoptions")) {
                NodeList   choices = ClientProtocolUtils.getItemNodes(d);
                DataItem[] opts    = extractCurrentDataItems(choices);

                list.add(new IntegerOptionsData(name, label, opts));
            }
            else if (type.equals("doublearray")) {
                list.add(new DoubleArrayData(name, label, null));
            }
            else if (type.equals("multiattribute")) {
                list.add(DataFactory.createMultiAttributeData(d, name, label));
            }
            else {
                log.warn("Unrecognized Dynamic data type.");
                NodeList   choices   = ClientProtocolUtils.getItemNodes(d);
                DataItem[] dataItems = extractCurrentDataItems(choices);
                DataItem   def       = extractDefaultDataItem(d);

                String min = ClientProtocolUtils.getMinNode(d);
                String max = ClientProtocolUtils.getMaxNode(d);
                if (min != null && max != null) {
                    list.add(new DoubleRangeData(
                        name, label,
                        Double.valueOf(min), Double.valueOf(max),
                        Double.valueOf(min), Double.valueOf(max)));
                }

                list.add(new DefaultData(name, label, null, dataItems, def));
            }

        }

        return list;
    }


    /**
     * This method extracts the default value of a Data object.
     *
     * @param data The data object node.
     *
     * @return the default DataItem.
     */
    protected static DataItem extractDefaultDataItem(Node data) {
        log.debug("ArtifactDescriptionFactory.extractDefaultDataItem");

        String value = XMLUtils.xpathString(
            data, "@art:defaultValue", ArtifactNamespaceContext.INSTANCE);

        String label = XMLUtils.xpathString(
            data, "@art:defaultLabel", ArtifactNamespaceContext.INSTANCE);

        if (value != null && label != null) {
            return new DefaultDataItem(label, null, value);
        }

        return null;
    }


    /**
     * This method extract the {@link DataItem}s of the DESCRIBE document.
     *
     * @param items The items in the DESCRIBE document.
     *
     * @return the {@link DataItem}s.
     */
    protected static DataItem[] extractCurrentDataItems(NodeList items) {
        log.debug("ArtifactDescriptionFactory.extractCurrentDataItems");

        if (items == null || items.getLength() == 0) {
            log.debug("No data items found.");
            return null;
        }

        int count = items.getLength();

        List<DataItem> dataItems = new ArrayList<DataItem>(count);

        for (int i = 0; i < count; i++) {
            Node item    = items.item(i);
            String label = ClientProtocolUtils.getLabel(item);
            String value = ClientProtocolUtils.getValue(item);

            double[] mmQ = extractMinMaxQValues(item);
            double[] mmW = extractMinMaxWValues(item);

            if (mmQ != null || mmW != null) {
                dataItems.add(new WQDataItem(label, null, value, mmQ, mmW));
            }
            else {
                dataItems.add(new DefaultDataItem(label, null, value));
            }
        }

        return dataItems.toArray(new DataItem[count]);
    }


    protected static double[] extractMinMaxQValues(Node item) {
        log.debug("ArtifactDescriptionFactory.extractMinMaxQValues");

        if (item == null) {
            log.debug("This node is empty - no min/max Q values.");
            return null;
        }

        Node node = (Node) XMLUtils.xpath(
            item,
            "art:range[@art:type='Q']",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (node == null) {
            log.debug("No min/max Q values found.");
            return null;
        }

        return extractMinMaxValues(node);
    }


    protected static double[] extractMinMaxWValues(Node item) {
        log.debug("ArtifactDescriptionFactory.extractMinMaxWValues");

        if (item == null) {
            log.debug("This node is empty - no min/max W values.");
            return null;
        }

        Node node = (Node) XMLUtils.xpath(
            item,
            "art:range[@art:type='W']",
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (node == null) {
            log.debug("No min/max W values found.");
            return null;
        }

        return extractMinMaxValues(node);
    }


    protected static double[] extractMinMaxValues(Node node) {
        log.debug("ArtifactDescriptionFactory.extractMinMaxValues");

        String minStr = XMLUtils.xpathString(
            node, "art:min/text()", ArtifactNamespaceContext.INSTANCE);

        String maxStr = XMLUtils.xpathString(
            node, "art:max/text()", ArtifactNamespaceContext.INSTANCE);

        if (maxStr == null || minStr == null) {
            log.debug("No min/max values found.");
            return null;
        }

        try {
            double min = Double.valueOf(minStr);
            double max = Double.valueOf(maxStr);

            return new double[] { min, max };
        }
        catch (NumberFormatException nfe) {
            log.debug("Error while parsing min/max values.");
        }

        return null;
    }


    /**
     * This method extracts the data objects from the data node of the static ui
     * part of the DESCRIBE document.
     *
     * @param staticNode The static ui node of the DESCRIBE.
     *
     * @return the DataList objects.
     */
    protected static DataList[] extractOldData(Node staticNode) {
        log.debug("ArtifactDescriptionFactory.extractOldData()");

        NodeList stateNodes = (NodeList) XMLUtils.xpath(
            staticNode,
            XPATH_STATIC_STATE_NODE,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (stateNodes == null || stateNodes.getLength() == 0) {
            log.debug("No old items found.");
            return null;
        }

        int count       = stateNodes.getLength();
        DataList[] data = new DataList[count];

        for (int i = 0; i < count; i++) {
            Node tmp = stateNodes.item(i);

            String name = XMLUtils.xpathString(
                tmp, "@art:name", ArtifactNamespaceContext.INSTANCE);
            String uiprovider = XMLUtils.xpathString(
                tmp, "@art:uiprovider", ArtifactNamespaceContext.INSTANCE);
            String label = XMLUtils.xpathString(
                tmp, "@art:label", ArtifactNamespaceContext.INSTANCE);
            String help = XMLUtils.xpathString(
                tmp, "@art:helpText", ArtifactNamespaceContext.INSTANCE);

            NodeList dataNodes = (NodeList) XMLUtils.xpath(
                tmp,
                XPATH_STATIC_DATA_NODE,
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

            if (dataNodes == null || dataNodes.getLength() == 0) {
                continue;
            }

            int size      = dataNodes.getLength();
            DataList list = new DataList(name, size, uiprovider, label, help);

            for (int j = 0; j < size; j++) {
                Node dataNode = dataNodes.item(j);

                list.add(DataFactory.createDataFromElement((Element) dataNode));

                data[i] = list;
            }
        }

        return data;
    }


    /**
     * This method extracts the UIProvider specified by the data node.
     *
     * @param data The data node.
     *
     * @return the UIProvider that is specified in the data node.
     */
    protected static String extractUIProvider(Node ui) {
        return (String) XMLUtils.xpath(
            ui,
            XPATH_UIPROVIDER,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method extracts the help text specified by the data node.
     *
     * @param ui The data node.
     *
     * @return the help text.
     */
    protected static String extractHelpText(Node ui) {
        return (String) XMLUtils.xpath(
            ui,
            XPATH_HELP_TEXT,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method extracts the reachable states of the current artifact.
     *
     * @param reachable The reachable states node.
     *
     * @return an array with identifiers of reachable states.
     */
    protected static String[] extractReachableStates(Node reachable) {
        log.debug("ArtifactDescriptionFactory.extractReachableStates()");

        NodeList list = (NodeList) XMLUtils.xpath(
            reachable,
            XPATH_REACHABLE_STATE,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (list == null || list.getLength() == 0) {
            return null;
        }

        int count = list.getLength();

        String[] states = new String[count];

        for (int i = 0; i < count; i++) {
            Node state = list.item(i);

            String name = XMLUtils.xpathString(
                state, "@art:name", ArtifactNamespaceContext.INSTANCE);

            states[i] = name;
        }

        return states;
    }


    /**
     * This method extract available output modes of the the current artifact.
     *
     * @param outputs A list of nodes that contain information about output
     * modes.
     *
     * @return an array of Output modes.
     */
    protected static OutputMode[] extractOutputModes(NodeList outputs) {
        log.debug("ArtifactDescriptionFactory.extractOutputModes");

        if (outputs == null || outputs.getLength() == 0) {
            return null;
        }

        int size = outputs.getLength();

        List<OutputMode> outs = new ArrayList<OutputMode>(size);

        for (int i = 0; i < size; i++) {
            Node out = outputs.item(i);

            String name = XMLUtils.xpathString(
                out, "@art:name", ArtifactNamespaceContext.INSTANCE);
            String desc = XMLUtils.xpathString(
                out, "@art:description", ArtifactNamespaceContext.INSTANCE);
            String mimeType = XMLUtils.xpathString(
                out, "@art:mime-type", ArtifactNamespaceContext.INSTANCE);

            if (name != null) {
                outs.add(new DefaultOutputMode(name, desc, mimeType));
            }
            else {
                log.debug("Found an invalid output mode.");
            }
        }

        return (OutputMode[]) outs.toArray(new OutputMode[size]);
    }


    protected static Recommendation[] extractRecommendedArtifacts(Document doc){
        log.debug("ArtifactDescriptionFactory.extractRecommendedArtifacts.");

        NodeList list = (NodeList) XMLUtils.xpath(
            doc,
            XPATH_RECOMMENDED_ARTIFACTS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = list != null ? list.getLength() : 0;

        Recommendation[] rec = new Recommendation[num];

        for (int i = 0; i < num; i++) {
            Element e           = (Element) list.item(i);
            String  factory     = e.getAttribute("factory");
            String  index       = e.getAttribute("ids");
            String  targetOut   = e.getAttribute("target_out");

            if (factory != null && factory.length() > 0) {
                log.debug("Adding Recommendation. Factory: " + factory +
                        " IDs: " + index + " target out " + targetOut);
                rec[i] = new Recommendation(
                    factory, index, null, null, targetOut);
            }
        }

        return rec;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
