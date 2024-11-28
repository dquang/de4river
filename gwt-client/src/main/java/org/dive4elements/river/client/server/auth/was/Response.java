/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.was;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.httpclient.utils.XMLUtils;
import org.dive4elements.river.client.server.auth.Authentication;
import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.auth.saml.Assertion;
import org.dive4elements.river.client.server.auth.saml.XPathUtils;
import org.dive4elements.river.client.server.auth.saml.TicketValidator;
import org.dive4elements.river.client.server.auth.saml.User;

import org.dive4elements.river.client.server.features.Features;


public class Response implements Authentication {

    private static Logger log = LogManager.getLogger(Response.class);

    private Element root;
    private String samlTicketXML;
    private Assertion assertion;
    private String username;
    private String password;
    private Features features;
    private String trustedKeyFile;
    private String timeEpsilon;


    public Response(HttpEntity entity, String username, String password,
            Features features, String trustedKeyFile, String timeEpsilon)
        throws AuthenticationException, IOException {

        if (entity == null) {
            throw new ServiceException("Invalid response");
        }

        String contenttype = entity.getContentType().getValue();
        String samlTicketXML = EntityUtils.toString(entity);

        InputStream in = new StringBufferInputStream(samlTicketXML);

        if (!contenttype.equals("application/vnd.ogc.se_xml")) {
            // XXX: Assume base64 encoded content.
            in = new Base64InputStream(in);
        }

        Document doc = XMLUtils.readDocument(in);
        Element root = doc.getDocumentElement();
        String rname = root.getTagName();

        if (rname != null && rname.equals("ServiceExceptionReport")) {
            throw new ServiceException(XPathUtils.xpathString(root,
                                                          "ServiceException"));
        }

        this.samlTicketXML = samlTicketXML;
        this.root = root;
        this.username = username;
        this.password = password;
        this.features = features;
        this.trustedKeyFile = trustedKeyFile;
        this.timeEpsilon = timeEpsilon;
    }

    @Override
    public boolean isSuccess() {
        String status = getStatus();
        return status != null && status.equals("samlp:Success");
    }

    public String getStatus() {
        return XPathUtils.xpathString(this.root,
            "./samlp:Status/samlp:StatusCode/@Value");
    }


    public Assertion getAssertion() {
        if (this.assertion == null && this.root != null) {
            try {
                int timeEps = Integer.parseInt(this.timeEpsilon);
                TicketValidator validator =
                    new TicketValidator(this.trustedKeyFile, timeEps);
                this.assertion = validator.checkTicket(this.root);
            }
            catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return this.assertion;
    }

    @Override
    public User getUser() throws AuthenticationException {
        Assertion assertion = this.getAssertion();
        if (assertion == null) {
            throw new AuthenticationException(
                "Response doesn't contain an assertion");
        }
        List<String> features = this.features.getFeatures(
                this.assertion.getRoles());
        log.debug("User " + this.username + " with features " + features +
                     " successfully authenticated.");
        return new User(assertion, this.samlTicketXML, features, this.password);
    }
}
// vim: set si et fileencoding=utf-8 ts=4 sw=4 tw=80:
