/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.common.utils.XMLUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StateFactory {

    /** The log used in this class */
    private static Logger log = LogManager.getLogger(StateFactory.class);

    /** The XPath to the classname of the state */
    public static final String XPATH_STATE = "@state";

    /** The XPath to the data items of the state relative to the state node. */
    public static final String XPATH_DATA = "data";

    /** The XPath to the data name relative to the data node.*/
    public static final String XPATH_DATA_NAME = "@name";

    /** The XPath to the data type relative to the data node.*/
    public static final String XPATH_DATA_TYPE = "@type";

    /** The XPath to the data description relative to the data node.*/
    public static final String XPATH_DATA_DESCRIPTION = "@description";


    /**
     * Creates a new State based on the configured class provided by
     * <code>stateConf</code>.
     *
     * @param stateConf The configuration of the state.
     *
     * @return a State.
     */
    public static State createState(Node stateConf) {
        String clazz = (String) XMLUtils.xpath(
            stateConf, XPATH_STATE, XPathConstants.STRING);

        State state = null;

        try {
            log.debug("Create a new State for class: " + clazz);
            state = (State) Class.forName(clazz).newInstance();
            state.setup(stateConf);

            initializeStateData(state, stateConf);
        }
        catch (InstantiationException ie) {
            log.error(ie, ie);
        }
        catch (IllegalAccessException iae) {
            log.error(iae, iae);
        }
        catch (ClassNotFoundException cnfe) {
            log.error(cnfe, cnfe);
        }

        return state;
    }


    /**
     * This method extracts the configured input data of a state and adds new
     * StateData objects to the State.
     *
     * @param state The state.
     * @param stateConf The state configuration node.
     */
    protected static void initializeStateData(State state, Node stateConf) {
        NodeList dataList = (NodeList) XMLUtils.xpath(
            stateConf, XPATH_DATA, XPathConstants.NODESET);

        if (dataList == null || dataList.getLength() == 0) {
            log.debug("The state has no input data configured.");

            return;
        }

        int items = dataList.getLength();

        log.debug("The state has " + items + " data items configured.");

        for (int i = 0; i < items; i++) {
            Node data = dataList.item(i);

            String name = (String) XMLUtils.xpath(
                data, XPATH_DATA_NAME, XPathConstants.STRING);
            String type = (String) XMLUtils.xpath(
                data, XPATH_DATA_TYPE, XPathConstants.STRING);
            String desc = (String) XMLUtils.xpath(
                data, XPATH_DATA_DESCRIPTION, XPathConstants.STRING);

            if (name == null || name.length() == 0) {
                log.warn("No name for data item at pos " + i + " found.");
                continue;
            }

            if (type == null || type.length() == 0) {
                log.warn("No type for data item at pos " + i + " found.");
                log.warn("Default type 'string' used.");
                type = "string";
            }

            log.debug("add StateData '" + name + "' (type '" + type + "')");
            state.addData(name, new DefaultStateData(name, desc, type));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
