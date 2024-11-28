/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

/**
 * Trivial implementation of a user. Useful to be subclassed.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultUser
implements   User
{
    /** The identifier of the user.*/
    protected String identifier;

    /** The name of the user.*/
    protected String name;

    /** The saml ticket used for single sign-on.*/
    protected String samlXML;

    /**
     * The default constructor.
     */
    public DefaultUser() {
    }


    /**
     * A constructor that creates a new user.
     *
     * @param identifier The uuid of the user.
     * @param name The name of the user.
     */
    public DefaultUser(String identifier, String name, String samlXML) {
        this.identifier = identifier;
        this.name       = name;
        this.samlXML    = samlXML;
    }


    /**
     * Returns the identifier of this user.
     *
     * @return the identifier of this user.
     */
    public String identifier() {
        return identifier;
    }


    /**
     * Returns the name of the user.
     *
     * @return the name of the user.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the SAML ticket for single sign-on.
     *
     * @return the SAML ticket as base64 encoded XML
     */
    public String getSamlXMLBase64() {
        return samlXML;
    }


    /**
     * Set the identifier of the user.
     *
     * @param identifier The new identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Set the name of the user.
     *
     * @param name The name for this user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the SAML Ticket for single sign-on.
     *
     * @param samlXML the SAML ticket as base64 encoded XML.
     */
    public void setSamlXMLBase64(String samlXML) {
        this.samlXML = samlXML;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
