/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

/**
 * UserClient is a class to allow easier communication
 * with the REST based artifact user protocol
 */
public class UserClient {

    private static final Logger log = LogManager.getLogger(UserClient.class);

    private String url;

    public UserClient(String url) {
        this.url = url;
    }

    public boolean userExists(User user) throws ConnectionException {
        if (user == null) {
            return false;
        }

        Element data = this.findUser(user);

        String XPATH_USERACCOUNT = "/art:user/art:account/@art:name";

        String account = XMLUtils.xpathString(
            data, XPATH_USERACCOUNT, ArtifactNamespaceContext.INSTANCE);

        if (account == null) {
            return false;
        }

        return account.equals(user.getAccount());
    }

    public boolean createUser(User user) throws ConnectionException {
        if(user == null) {
            log.warn("createUser: given user is null");
            return false;
        }

        log.debug("Creating new user " + user.getName());
        HttpClient client = new HttpClientImpl(this.url);

        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action = creator.create("action");

        Element type = creator.create("type");
        type.setAttribute("name", "create");
        Element artuser = creator.create("user");
        artuser.setAttribute("name", user.getName());
        Element account = creator.create("account");
        account.setAttribute("name", user.getAccount());

        //TODO create roles
        artuser.appendChild(account);
        action.appendChild(type);
        action.appendChild(artuser);
        document.appendChild(action);

        log.debug("Create user request xml: " + XMLUtils.toString(document));

        Document resp = client.createUser(document);

        log.debug("Create user response xml: " + XMLUtils.toString(resp));

        String XPATH_RESPONSE = "/art:result";
        Node nresult = (Node) XMLUtils.xpath(
            resp,
            XPATH_RESPONSE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);
        String result = nresult.getTextContent();
        return (result != null && result.equalsIgnoreCase("success"));
    }

    public NodeList listUsers() throws ConnectionException {
        HttpClient client = new HttpClientImpl(this.url);

        Document users = (Document) client.listUsers();

        String XPATH_USERS = "/art:users/art:user";

        return (NodeList) XMLUtils.xpath(
            users,
            XPATH_USERS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);
    }

    public Element findUser(User user) throws ConnectionException {
        if(user == null) {
            throw new IllegalArgumentException("user is null");
        }

        HttpClient client = new HttpClientImpl(this.url);

        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action = creator.create("action");

        Element type = creator.create("type");
        type.setAttribute("name", "find");
        Element account = creator.create("account");
        account.setAttribute("name", user.getAccount());

        action.appendChild(type);
        action.appendChild(account);
        document.appendChild(action);

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Find user request xml: " +
                XMLUtils.toString(document));
        }

        Document resp = client.findUser(document);

        if (debug) {
            log.debug("Find user request response xml: " +
                XMLUtils.toString(resp));
        }

        return resp.getDocumentElement();
    }
}
// vim: set si et fileencoding=utf-8 ts=4 sw=4 tw=80:

