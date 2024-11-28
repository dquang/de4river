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

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * This transition compares data objects with a <i>equal</i> or <i>notequal</i>
 * operator.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ValueCompareTransition extends DefaultTransition {

    /** The log that is used in this transition.*/
    private static Logger log = LogManager.getLogger(ValueCompareTransition.class);


    /** XPath that points to the condition's operator.*/
    public static final String XPATH_OPERATOR = "condition/@operator";

    /** XPath that points to the condition's value.*/
    public static final String XPATH_VALUE    = "condition/@value";

    /** XPath that points  to the condition's dataname.*/
    public static final String XPATH_DATANAME = "condition/@data";

    /** The value of the 'equal' operator.*/
    public static final String OPERATOR_EQUAL = "equal";

    /** The value of the 'not equal' operator.*/
    public static final String OPERATOR_NOTEQUAL = "notequal";


    /** The operator.*/
    protected String operator;

    /** The value used for comparison.*/
    protected String value;

    /** The name of the data used for the comparison.*/
    protected String dataname;



    public ValueCompareTransition() {
    }


    public ValueCompareTransition(String from, String to) {
        super(from, to);
    }


    /** Setup member values from the document. */
    @Override
    public void init(Node config) {
        log.debug("ValueCompareTransition.init");

        String tmp = (String) XMLUtils.xpath(
            config,
            XPATH_OPERATOR,
            XPathConstants.STRING);
        operator = tmp.trim().toLowerCase();

        value = (String) XMLUtils.xpath(
            config,
            XPATH_VALUE,
            XPathConstants.STRING);

        dataname = (String) XMLUtils.xpath(
            config,
            XPATH_DATANAME,
            XPathConstants.STRING);
    }


    @Override
    public boolean isValid(Artifact artifact, State a, State b) {
        log.debug("ValueCompareTransition.isValid");

        D4EArtifact flysArtifact = (D4EArtifact) artifact;

        StateData dataObj = flysArtifact.getData(dataname);
        String    dataVal = dataObj != null ? (String) dataObj.getValue() : "";

        if (log.isDebugEnabled()) {
            log.debug("Compare settings:");
            log.debug("-- dataname: " + dataname);
            log.debug("-- operator: " + operator);
            log.debug("-- compare value: " + value);
            log.debug("-- state value: " + dataVal);
        }

        if (operator.equals(OPERATOR_EQUAL)) {
            return value.equals(dataVal);
        }
        else if (operator.equals(OPERATOR_NOTEQUAL)) {
            return !(value.equals(dataVal));
        }

        log.error("Wrong operator set! No comparison takes place.");

        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
