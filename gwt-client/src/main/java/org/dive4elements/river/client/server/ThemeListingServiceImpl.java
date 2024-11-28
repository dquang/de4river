/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.client.services.ThemeListingService;

import org.dive4elements.river.client.shared.exceptions.ServerException;

import org.dive4elements.river.client.shared.model.Style;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This interface provides a method to list themes filtered by name.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class ThemeListingServiceImpl
extends      RemoteServiceServlet
implements   ThemeListingService
{
    private static final Logger log =
        LogManager.getLogger(ThemeListingServiceImpl.class);


    private static final String XPATH_THEME_GROUPS = "/themes/themegroup";
    /** The error message key that is thrown if an error occured while reading
     * the supported rivers from server.*/
    public static final String ERROR_NO_GROUPS_FOUND = "error_no_groups_found";


    public Map<String, Style> list(String locale, String name)
    throws ServerException
    {
        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            null,
            null);

        Element e = ec.create("theme");
        ec.addAttr(e, "name", name);
        doc.appendChild(e);
        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document res = client.callService(url, "themelisting", doc);

            NodeList themeGroups = (NodeList) XMLUtils.xpath(
                res,
                XPATH_THEME_GROUPS,
                XPathConstants.NODESET,
                null);

            if (themeGroups == null || themeGroups.getLength() == 0) {
                throw new ServerException(ERROR_NO_GROUPS_FOUND);
            }

            int count = themeGroups.getLength();

            Map<String, Style> theStyles = new HashMap<String, Style>(count);

            for (int i = 0; i < count; i++) {
                Element tmp = (Element)themeGroups.item(i);

                String groupName = tmp.getAttribute("name");
                NodeList theTheme = (NodeList) XMLUtils.xpath(
                    tmp,
                    "theme",
                    XPathConstants.NODESET,
                    null);

                for (int j = 0; j < theTheme.getLength(); j++) {
                    Element elem = (Element) theTheme.item(j);
                    theStyles.put(groupName, StyleHelper.getStyle(elem));
                }
            }

            return theStyles;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_GROUPS_FOUND);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
