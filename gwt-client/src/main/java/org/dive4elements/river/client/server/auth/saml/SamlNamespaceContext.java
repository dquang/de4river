/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.saml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/**
 * The namespace context for SAML documents.
 */
public class SamlNamespaceContext implements NamespaceContext
{
    /**
     * The URI of the namespace of SAML assertions.
     */
    public static final String SAML_NS_ASSERT =
        "urn:oasis:names:tc:SAML:1.0:assertion";

    /**
     * The URI of the namespace of the SAML protocol.
     */
    public static final String SAML_NS_PROTO =
        "urn:oasis:names:tc:SAML:1.0:protocol";

    /**
     * The URI of the namespace for XML signatures.
     */
    public static final String XML_SIG_NS =
        "http://www.w3.org/2000/09/xmldsig#";

    /**
     * Final instance to be easily used to avoid creation
     * of instances.
     */
    public static final SamlNamespaceContext INSTANCE =
        new SamlNamespaceContext();


    /**
     * The default constructor.
     */
    public SamlNamespaceContext() {
    }


    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(String)
     * @param prefix The prefix
     * @return The corresponing URI
     */
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new NullPointerException("Null prefix");
        }

        if ("saml".equals(prefix)) {
            return SAML_NS_ASSERT;
        }

        if ("samlp".equals(prefix)) {
            return SAML_NS_PROTO;
        }

        if ("ds".equals(prefix)) {
            return XML_SIG_NS;
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
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }


    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     * @param uri The URI
     * @return nothing
     * @throws java.lang.UnsupportedOperationException
     */
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
