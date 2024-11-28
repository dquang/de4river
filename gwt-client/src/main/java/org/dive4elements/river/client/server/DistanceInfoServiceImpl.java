/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.client.services.DistanceInfoService;
import org.dive4elements.river.client.shared.model.DistanceInfoObject;
import org.dive4elements.river.client.shared.model.DistanceInfoObjectImpl;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DistanceInfoServiceImpl
extends      RemoteServiceServlet
implements   DistanceInfoService
{
    private static final Logger log =
        LogManager.getLogger(DistanceInfoServiceImpl.class);

    public static final String ERROR_NO_DISTANCEINFO_FOUND =
        "error_no_distanceinfo_found";

    public static final String XPATH_DISTANCES = "art:distances/art:distance";


    public DistanceInfoObject[] getDistanceInfo(
        String locale,
        String river)
    throws ServerException
    {
        log.info("DistanceInfoServiceImpl.getDistanceInfo");

        String url  = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element riverEl = ec.create("river");

        riverEl.setTextContent(river);

        doc.appendChild(riverEl);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document result = client.callService(url, "distanceinfo", doc);

            log.debug("Extract distance info objects now.");
            DistanceInfoObject[] objects = extractDistanceInfoObjects(result);

            if (objects != null && objects.length > 0) {
                return objects;
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_DISTANCEINFO_FOUND);
    }


    /**
     * Extracts all distance info objects from <i>result</i> document.
     *
     * @param result The document retrieved by the server.
     *
     * @return a list of DistanceInfoObjects.
     */
    protected DistanceInfoObject[] extractDistanceInfoObjects(Document result)
    throws    ServerException
    {
        NodeList list = result.getElementsByTagName("distance");

        if (list == null || list.getLength() == 0) {
            log.warn("No distance info found.");
            throw new ServerException(ERROR_NO_DISTANCEINFO_FOUND);
        }

        int num = list.getLength();
        log.debug("Response contains " + num + " objects.");

        List<DistanceInfoObject> objects =
            new ArrayList<DistanceInfoObject>(num);

        for (int i = 0; i < num; i++) {
            DistanceInfoObject obj = buildDistanceInfoObject(
                (Element)list.item(i));

            if (obj != null) {
                objects.add(obj);
            }
        }

        log.debug("Retrieved " + objects.size() + " distances.");

        return (DistanceInfoObject[])
            objects.toArray(new DistanceInfoObject[num]);
    }


    /**
     * Extracts information for a single distance info object and intializes an
     * DistanceInfoObject with them.
     *
     * @param node The node that contains the information.
     *
     * @return a valid DistanceInfoObject.
     */
    protected DistanceInfoObject buildDistanceInfoObject(Element node) {

        String desc      = node.getAttribute("description").trim();
        String from      = node.getAttribute("from").trim();
        String to        = node.getAttribute("to").trim();
        String riverside = node.getAttribute("riverside").trim();
        String bottom    = node.getAttribute("bottom").trim();
        String top       = node.getAttribute("top").trim();

        if (desc.length() > 0 && from.length() > 0) {
            try {
                Double f  = new Double(from);
                Double t  = to    .length() > 0 ? new Double(to)     : null;
                Double b  = bottom.length() > 0 ? new Double(bottom) : null;
                Double tp = top   .length() > 0 ? new Double(top)    : null;

                return new DistanceInfoObjectImpl(desc, f, t, riverside, b, tp);
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe.getLocalizedMessage());
            }
        }

        log.warn("Invalid distance info object found.");

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
