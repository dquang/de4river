/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.client.services.CollectionItemAttributeService;

import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItemAttribute;
import org.dive4elements.river.client.shared.model.Style;
import org.dive4elements.river.client.shared.model.StyleSetting;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class CollectionItemAttributeServiceImpl
extends      RemoteServiceServlet
implements   CollectionItemAttributeService
{
    private static final Logger log =
        LogManager.getLogger(CollectionItemAttributeServiceImpl.class);


    public static final String XPATH_RESULT = "/art:result/text()";

    public static final String OPERATION_FAILURE = "FAILED";

    public static final String ERROR_NO_STYLES_FOUND =
        "error_no_theme_styles_found";


    public CollectionItemAttribute getCollectionItemAttribute(
        Collection collection,
        String artifact,
        String locale)
    throws ServerException
    {
        log.info(
            "CollectionItemAttributeServiceImpl.getCollectionItemAttribute");

        String url  = getServletContext().getInitParameter("server-url");

        Document requestDoc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            requestDoc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = ec.create("action");

        Element type = ec.create("type");
        ec.addAttr(type, "name", "getitemattribute", false);

        Element art = ec.create("artifact");
        ec.addAttr(art, "uuid", artifact, false);

        type.appendChild(art);
        action.appendChild(type);
        requestDoc.appendChild (action);

        try {
            HttpClient client = new HttpClientImpl(url, locale);
            Document res = (Document) client.doCollectionAction(
                requestDoc,
                collection.identifier(),
                new DocumentResponseHandler());
            return readXML (res, artifact);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_STYLES_FOUND);
    }


    public void setCollectionItemAttribute(
        Collection collection,
        String artifact,
        String locale,
        CollectionItemAttribute attributes)
    throws ServerException
    {
        log.info(
            "CollectionItemAttributeServiceImpl.setCollectionItemAttribute");

        String url  = getServletContext().getInitParameter("server-url");

        Document doc = writeXML(attributes, artifact);

        try {
            HttpClient client = new HttpClientImpl(url, locale);
            Document res = (Document) client.doCollectionAction(
                doc,
                collection.identifier(),
                new DocumentResponseHandler());

            return;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
            throw new ServerException(ce.getLocalizedMessage());
        }
    }

    protected CollectionItemAttribute readXML(Document doc, String artifact)
    throws    ServerException
    {
        CollectionItemAttribute cia = new CollectionItemAttribute();
        cia.setArtifact(artifact);

        Element root = doc.getDocumentElement();
        NodeList themes = root.getElementsByTagName("art:themes");

        if (themes == null || themes.getLength() == 0) {
            throw new ServerException(ERROR_NO_STYLES_FOUND);
        }

        Element e = (Element) themes.item(0);
        NodeList items = e.getElementsByTagName("theme");

        for (int i = 0; i < items.getLength(); i++) {
            Style s = StyleHelper.getStyle ((Element) items.item(i));
            if(s == null) {
                throw new ServerException(ERROR_NO_STYLES_FOUND);
            }
            else {
                cia.appendStyle(s);
            }
        }

        return cia;
    }


    protected Document writeXML (
        CollectionItemAttribute attributes,
        String artifact)
    {
        Document styles = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
                styles,
                ArtifactNamespaceContext.NAMESPACE_URI,
                ArtifactNamespaceContext.NAMESPACE_PREFIX);
        Element action = ec.create("action");
        Element type = ec.create("type");
        type.setAttribute("name", "setitemattribute");
        Element art = ec.create("artifact");
        art.setAttribute("uuid", artifact);
        Element attr = ec.create("attribute");
        Element themes = ec.create("themes");
        action.appendChild(type);
        type.appendChild(art);
        art.appendChild(attr);
        attr.appendChild(themes);

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
                styles,
                "",
                "");

        for (int i = 0; i < attributes.getNumStyles(); i++) {
            Style s = attributes.getStyle(i);
            Element theme = creator.create("theme");
            theme.setAttribute("name", s.getName());
            theme.setAttribute("facet", s.getFacet());
            theme.setAttribute("index", String.valueOf(s.getIndex()));
            for (int j = 0; j < s.getNumSettings(); j++) {
                StyleSetting set = s.getSetting(j);
                Element field = creator.create("field");
                field.setAttribute("name", set.getName());
                field.setAttribute("display", set.getDisplayName());
                field.setAttribute("default", set.getDefaultValue());
                field.setAttribute("hints", set.getHints());
                field.setAttribute("type", set.getType());
                field.setAttribute("hidden", String.valueOf(set.isHidden()));
                theme.appendChild(field);
            }
            themes.appendChild(theme);
        }
        styles.appendChild(action);
        return styles;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
