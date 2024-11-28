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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.shared.exceptions.ServerException;

import org.dive4elements.river.client.client.services.MetaDataService;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.shared.model.AttrList;
import org.dive4elements.river.client.shared.model.DataCageNode;
import org.dive4elements.river.client.shared.model.DataCageTree;

import org.dive4elements.river.client.server.meta.Converter;

/**
 * Service that returns certain meta-data from the backends data, polished to
 * inclusion into current project.
 */
public class MetaDataServiceImpl
extends      RemoteServiceServlet
implements   MetaDataService, DataCageTree.Visitor
{
    /** Our very own log. */
    private static final Logger log =
        LogManager.getLogger(MetaDataServiceImpl.class);

    public static final String ERROR_NO_META_DATA_FOUND =
        "error_no_meta_data_found";


    @Override
    public boolean accept(DataCageNode node) {
        AttrList al = node.getAttributes();
        return al != null && al.hasAttribute("factory");
    }

    /**
     * @param locale needed for i18n.
     * @param artifactId ID of masterartifact (can be null)
     * @param userId can be null
     * @param outs can be null
     * @param parameters can be null or parameters like
     * "load-system:true;key:value"
     */
    @Override
    public DataCageTree getMetaData(
        String locale,
        String artifactId,
        String userId,
        String outs,
        String parameters
    ) throws ServerException
    {
        log.info("MetaDataService.getMetaData");

        // Create the query document.
        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element meta = ec.create("meta");

        if (artifactId != null) {
            Element artifactEl = ec.create("artifact-id");
            artifactEl.setAttribute("value", artifactId);
            meta.appendChild(artifactEl);
        }

        if (userId != null) {
            Element userEl = ec.create("user-id");
            userEl.setAttribute("value", userId);
            meta.appendChild(userEl);
        }

        if (outs != null) {
            Element outsEl = ec.create("outs");
            outsEl.setAttribute("value", outs);
            meta.appendChild(outsEl);
        }

        if (parameters != null) {
            Element paramsEl = ec.create("parameters");
            paramsEl.setAttribute("value", parameters);
            meta.appendChild(paramsEl);
        }

        doc.appendChild(meta);

        // Fire.
        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Converter converter = new Converter();
            DataCageTree tree = converter.convert(
                client.callService(url, "metadata", doc));
            tree.prune(this);
            return tree;
        }
        catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        throw new ServerException(ERROR_NO_META_DATA_FOUND);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
