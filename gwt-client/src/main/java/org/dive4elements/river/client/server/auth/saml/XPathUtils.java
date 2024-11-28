/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.saml;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.common.utils.XMLUtils;


/**
 * Convenience methods to evaluate XPath queries on SAML documents. The
 * methods are just front-ends for the {@link XMLUtils.xpath} method.
 */
public class XPathUtils
{
    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type, using SamlNamespaceContext as the namespace
     * context.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnType The type of the result.
     * @return The result of type 'returnType' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(Object root, String query,
                                     QName returnType) {
        return XMLUtils.xpath(root, query, returnType,
                              SamlNamespaceContext.INSTANCE);
    }


    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a String, using SamlNamespaceContext as the namespace context.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @return The result as a String or null if something went wrong
     * during XPath evaluation.
     */
    public static final String xpathString(Object root, String query) {
        return (String)xpath(root, query, XPathConstants.STRING);
    }


    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a Node, using SamlNamespaceContext as the namespace context.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @return The result as a Node or null if something went wrong
     * during XPath evaluation.
     */
    public static final Node xpathNode(Object root, String query) {
        return (Node)xpath(root, query, XPathConstants.NODE);
    }


    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a NodeList, using SamlNamespaceContext as the namespace
     * context.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @return The result as a NodeList or null if something
     * went wrong during XPath evaluation.
     */
    public static final NodeList xpathNodeList(Object root, String query) {
        return (NodeList)xpath(root, query, XPathConstants.NODESET);
    }
}
