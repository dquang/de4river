/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.features;

import java.util.Iterator;

import javax.xml.XMLConstants;

import javax.xml.namespace.NamespaceContext;

public class FeaturesNamespaceContext
implements   NamespaceContext {

    /**
     * The URI of the namespace of the features.
     */
    public final static String NAMESPACE_URI =
        "http://www.intevation.de/2012/flys/features";

    /**
     * The XML prefix for the features namespace.
     */
    public final static String NAMESPACE_PREFIX = "ftr";

    /**
     * Final instance to be easily used to avoid creation
     * of instances.
     */
    public static final FeaturesNamespaceContext INSTANCE =
        new FeaturesNamespaceContext();

    /**
     * The default constructor.
     */
    public FeaturesNamespaceContext() {
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(String)
     * @param prefix The prefix
     * @return The corresponing URI
     */
    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new NullPointerException("Null prefix");
        }

        if (NAMESPACE_PREFIX.equals(prefix)) {
            return NAMESPACE_URI;
        }

        if ("xml".equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }

        return XMLConstants.NULL_NS_URI;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(String)
     * @param uri The URI
     * @return nothing.
     * @throws java.lang.UnsupportedOperationException
     */
    @Override
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     * @param uri The URI
     * @return nothing
     * @throws java.lang.UnsupportedOperationException
     */
    @Override
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
