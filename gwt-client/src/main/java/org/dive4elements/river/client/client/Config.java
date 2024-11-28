/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;


/**
 * A class that is used to handle the global configuration of this client. You
 * can retrieve an instance of this class using the <code>getInstance</code>
 * methods. <b>NOTE:</b> the configuration is initialized using {@link
 * getInstance(Document)} the first time.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Config {

    /** The instance of the configuration. */
    protected static Config INSTANCE;

    /** The xml document that contains the configuration options. */
    protected Document config;

    protected String helpUrl;

    /**
     * Get an instance by using {@link getInstance(Document)} or {@link
     * getInstance()}.
     */
    private Config(Document config) {
        this.config = config;
    }


    /**
     * Returns an instance of this class and initializes the configuration of
     * this has not been done so far.
     *
     * @param config The client configuration.
     *
     * @return an instance of this Config class.
     */
    public static Config getInstance(Document config) {
        if (INSTANCE == null) {
            INSTANCE = new Config(config);
        }

        return INSTANCE;
    }


    /**
     * Returns an instance of this class. If it has not been initialized with a
     * valid configuration, null is returned.
     *
     * @return an instance of this class or null, if the Config has not been
     * initialized using {@link getInstance(Document)} so far.
     */
    public static Config getInstance() {
        return INSTANCE;
    }


    /**
     * Returns the URL of the artifact server.
     *
     * @return the artifact server url.
     */
    public String getServerUrl() {
        Node server = config.getElementsByTagName("server").item(0);
        return server.getFirstChild().getNodeValue();
    }


    /**
     * Returns the URL of the FLYS/d4e-Wiki.
     *
     * @return wiki base URL
     */
    public String getWikiUrl() {
        return this.helpUrl;
    }

    public void setWikiUrl(String url) {
        this.helpUrl = url;
    }


    /**
     * Returns the name of the current locale.
     *
     * @return the name of the current locale.
     */
    public String getLocale() {
        return LocaleInfo.getCurrentLocale().getLocaleName();
    }

    public boolean getHideLogout() {
        Node hide_logout = config.getElementsByTagName("hide-logout").item(0);
        if (hide_logout == null) {
            return false;
        }
        String value = hide_logout.getFirstChild().getNodeValue();
        return value.toLowerCase().equals("true");
    }

    /**
     * Returns the integer configured at
     * <i>/config/projectlist/update-interval/text()</i> or -1 if an error
     * occured or no such option is defined.
     *
     * @return the update interval of the project list.
     */
    public int getProjectListUpdateInterval() {
        Node projectlist = config.getElementsByTagName("projectlist").item(0);

        if (projectlist == null) {
            return -1;
        }

        Node interval = config.getElementsByTagName("update-interval").item(0);

        if (interval == null) {
            return -1;
        }

        String value = interval.getFirstChild().getNodeValue();

        return value != null ? Integer.valueOf(value) : -1;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
