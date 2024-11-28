/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.saml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a SAML assertion about a user.
 */
public class Assertion {

    private static Logger log = LogManager.getLogger(Assertion.class);

    private Element assertion;
    private LinkedList<String> roles;
    private String user_id;
    private String name_id;
    private String group_id;
    private String group_name;
    private Date notbefore;
    private Date notonorafter;

    private static final String ATTR_CONT_USER_ID =
        "urn:conterra:names:sdi-suite:policy:attribute:user-id";
    private static final String ATTR_CONT_GROUP_ID =
        "urn:conterra:names:sdi-suite:policy:attribute:group-id";
    private static final String ATTR_CONT_GROUP_NAME =
        "urn:conterra:names:sdi-suite:policy:attribute:group-name";
    private static final String ATTR_CONT_ROLE =
        "urn:conterra:names:sdi-suite:policy:attribute:role";

    public Assertion(Element assertion) {
        this.assertion = assertion;
        this.roles = new LinkedList<String>();
        this.parseCondition();
        this.parseAttributeStatement();
    }

    private void parseCondition() {
        Element conditions = (Element)XPathUtils.xpathNode(this.assertion,
                                                           "saml:Conditions");
        if (conditions == null) {
            log.error("Cannot find Assertion conditions element");
            return;
        }

        this.notbefore = parseDateAttribute(conditions, "NotBefore");
        if (this.notbefore == null) {
            log.warn("Could not extract NotBefore date.");
        }
        this.notonorafter = parseDateAttribute(conditions, "NotOnOrAfter");
        if (this.notonorafter == null) {
            log.warn("Could not extract NotOnOrAfter date.");
        }
    }

    private Date parseDateAttribute(Element element, String name) {
        SimpleDateFormat dateformat = new SimpleDateFormat();
        // format should be "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" but that's
        // only available in java 7+. However, parsing without the
        // time-zone yields Date values in the local time-zone,
        // therefore we need to convert to GMT ourselves.
        dateformat.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String value = element.getAttribute(name);
        try {
            return toGMT(dateformat.parse(value));
        }
        catch(ParseException e) {
            log.error("Cannot parse Condition attribute "
                         + name + " with value " + value
                         + " (" + e.getLocalizedMessage() + ")");
        }
        return null;
    }

    private Date toGMT(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.ZONE_OFFSET, 0);
        cal.set(Calendar.DST_OFFSET, 0);
        return cal.getTime();
    }

    private void parseAttributeStatement() {
        Element attrstatement = (Element)XPathUtils.xpathNode(this.assertion,
                                                   "saml:AttributeStatement");
        if (attrstatement == null) {
            log.error("Cannot find Assertion AttributeStatement element");
            return;
        }

        this.name_id = XPathUtils.xpathString(attrstatement,
                                              "saml:Subject"
                                              + "/saml:NameIdentifier");

        this.user_id = getAttrValue(attrstatement, ATTR_CONT_USER_ID);
        this.group_id = getAttrValue(attrstatement, ATTR_CONT_GROUP_ID);
        this.group_name = getAttrValue(attrstatement, ATTR_CONT_GROUP_NAME);
        this.roles = getAttrValues(attrstatement, ATTR_CONT_ROLE);
    }

    static Object getAttrObject(Element attrs, String name, QName returnType) {
        return XPathUtils.xpath(attrs,
                                "saml:Attribute[@AttributeName='" + name + "']"
                                + "/saml:AttributeValue",
                                returnType);
    }

    static String getAttrValue(Element attrs, String name) {
        return (String)getAttrObject(attrs, name, XPathConstants.STRING);
    }

    static LinkedList<String> getAttrValues(Element attrs, String name) {
        LinkedList<String> strings = new LinkedList<String>();
        NodeList nodes = (NodeList)getAttrObject(attrs, name,
                                                 XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            strings.add(nodes.item(i).getTextContent());
        }

        return strings;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public String getUserID() {
        return this.user_id;
    }

    public String getNameID() {
        return this.name_id;
    }

    public String getGroupID() {
        return this.group_id;
    }

    public String getGroupName() {
        return this.group_name;
    }

    public Date getFrom() {
        return this.notbefore;
    }

    public Date getUntil() {
        return this.notonorafter;
    }

    /**
     * Returns whether the ticket to which the assertion belongs is
     * valid at the time the method is called. The method returns true,
     * if both dates (notbefore and notonorafter) have been determined
     * successfully and the current date/time is between both (with given
     * tolerance).
     * @return Whether the ticket is valid now.
     */
    public boolean isValidNow(int timeEps) {
        Date now = new Date();
        return (this.notbefore != null && this.notonorafter != null
            && now.after(new Date(this.notbefore.getTime() - timeEps))
            && now.before(new Date(this.notonorafter.getTime() + timeEps)));
    }
}
// vim: set fileencoding=utf-8 ts=4 sw=4 et si tw=80:
