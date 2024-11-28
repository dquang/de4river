/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.saml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Iterator;
import java.util.Date;
import javax.security.cert.X509Certificate;
import javax.security.cert.CertificateException;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.httpclient.utils.XMLUtils;


/**
 * Validator for SAML tickets.
 */
public class TicketValidator {

    /**
     * The log used by the TicketValidator instances.
     */
    private static Logger log = LogManager.getLogger(TicketValidator.class);

    /**
     * The trusted Key for signature checks.
     */
    private Key trustedKey;

    /**
     * Tolerance in milliseconds for validation based on NotBefore and
     * NotOnOrAfter of the SAML ticket
     */
    private int timeEps;

    /**
     * Creates a new TicketValidator from a trusted key.
     * @param trustedKey  The trusted key for the signature checks.
     */
    public TicketValidator(Key trustedKey, int timeEps) {
        this.trustedKey = trustedKey;
        this.timeEps = timeEps;
    }

    /**
     * Creates a new TicketValidator, loading the trusted key from a
     * file.
     * @param filename The filename of the X509 certificate containing
     * the trusted public key.
     */
    public TicketValidator(String filename, int timeEps)
        throws IOException, CertificateException {
        this.trustedKey = loadKey(filename);
        this.timeEps = timeEps;
    }

    /**
     * Loads the public key from a file containing an X509 certificate.
     */
    private Key loadKey(String filename) throws IOException,
                                                CertificateException {
        X509Certificate cert = X509Certificate.getInstance(
                                               new FileInputStream(filename));
        cert.checkValidity(new Date());
        return cert.getPublicKey();
    }


    /**
     * Check the ticket represented by the given DOM element.
     * @param root the DOM element under which the signature can be
     * found.
     * @return The assertion element from the signed data.
     */
    public Assertion checkTicket(Element root) throws Exception {
        markAssertionIdAttributes(root);

        Node signode = XPathUtils.xpathNode(root, ".//ds:Signature");

        DOMValidateContext context = new DOMValidateContext(this.trustedKey,
                                                            signode);
        context.setProperty("javax.xml.crypto.dsig.cacheReference", true);

        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = factory.unmarshalXMLSignature(context);
        if (!signature.validate(context)) {
            log.error("Signature of SAML ticket could not be validated.");
            return null;
        }

        Element assertionElement = extractAssertion(signature, context);
        if (assertionElement == null) {
            log.error("Could not extract assertion from signed content.");
            return null;
        }

        Assertion assertion = new Assertion(assertionElement);
        if (!assertion.isValidNow(this.timeEps)) {
            log.error("Ticket is not valid now"
                         + " (NotBefore: " + assertion.getFrom()
                         + ", NotOnOrAfter: " + assertion.getUntil()
                         + ", Tolerance (milliseconds): " + this.timeEps);
            return null;
        }

        return assertion;
    }

    /**
     * Check the ticket read from an InputStream containing a SAML
     * document.
     * @param xml InputStream with the SAML ticket as XML
     * @return The assertion element from the signed data.
     */
    public Assertion checkTicket(InputStream in) throws Exception {
        return checkTicket(XMLUtils.readDocument(in).getDocumentElement());
    }

    /**
     * Mark the AssertionID attribute of SAML Assertion elements as ID
     * attribute, so that the signature checker can resolve the
     * references properly and find the signed data.
     */
    private void markAssertionIdAttributes(Element root) {
        NodeList nodes = XPathUtils.xpathNodeList(root, "saml:Assertion");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element)nodes.item(i);
            el.setIdAttribute("AssertionID", true);
        }
    }

    private Element extractAssertion(XMLSignature sig,
                                     DOMValidateContext context) {
        for (Object obj: sig.getSignedInfo().getReferences()) {
            Data data = ((Reference)obj).getDereferencedData();
            if (data instanceof NodeSetData) {
                Iterator i = ((NodeSetData)data).iterator();
                for (int k = 0; i.hasNext(); k++) {
                    Object node = i.next();
                    if (node instanceof Element) {
                        Element el = (Element)node;
                        if (el.getTagName().equals("Assertion"))
                            return el;
                    }
                }
            }
        }

        return null;
    }
}
