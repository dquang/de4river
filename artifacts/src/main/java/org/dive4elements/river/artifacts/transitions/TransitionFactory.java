/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.transitions;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Node;

import org.dive4elements.artifactdatabase.transition.Transition;

import org.dive4elements.artifacts.common.utils.XMLUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TransitionFactory {

    /** The log used in this class */
    private static Logger log = LogManager.getLogger(TransitionFactory.class);

    /** The XPath to the classname of the transition */
    public static final String XPATH_TRANSITION = "@transition";

    /** The XPath to the current state ID */
    public static final String XPATH_CURRENT_STATE = "from/@state";

    /** The XPath to the target state ID */
    public static final String XPATH_TARGET_STATE = "to/@state";


    /**
     * Creates a new Transition based on the configured class provided by
     * <code>transitionConf</code>.
     *
     * @param transitionConf The configuration of the transition.
     *
     * @return a Transition.
     */
    public static Transition createTransition(Node transitionConf) {
        String clazz = (String) XMLUtils.xpath(
            transitionConf, XPATH_TRANSITION, XPathConstants.STRING);

        Transition transition = null;

        try {
            transition = (Transition) Class.forName(clazz).newInstance();

            String from = (String) XMLUtils.xpath(
                transitionConf, XPATH_CURRENT_STATE, XPathConstants.STRING);
            String to = (String) XMLUtils.xpath(
                transitionConf, XPATH_TARGET_STATE, XPathConstants.STRING);

            transition.init(transitionConf);
            transition.setFrom(from);
            transition.setTo(to);
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

        return transition;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
