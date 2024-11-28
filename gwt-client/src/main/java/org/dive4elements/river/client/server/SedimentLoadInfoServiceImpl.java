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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.river.client.client.services.SedimentLoadInfoService;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoObject;
import org.dive4elements.river.client.shared.model.SedimentLoadInfoObjectImpl;


/** Service to fetch info about sediment load. */
public class SedimentLoadInfoServiceImpl
extends      RemoteServiceServlet
implements   SedimentLoadInfoService
{
    private static final Logger log =
        LogManager.getLogger(SedimentLoadInfoServiceImpl.class);

    public static final String ERROR_NO_SEDIMENTLOADINFO_FOUND =
        "error_no_sedimentloadinfo_found";

    public static final String ERROR_NO_SEDIMENTLOADINFO_DATA =
        "error_no_sedimentloadinfo_data";

    @Override
    public SedimentLoadInfoObject[] getSedimentLoadInfo(
        String locale,
        String river,
        String type,
        double startKm,
        double endKm,
        String sq_ti_id)
    throws ServerException
    {
        log.info("SedimentLoadInfoServiceImpl.getSedimentLoadInfo");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element riverEl = ec.create("river");
        Element location = ec.create("location");
        Element from = ec.create("from");
        Element to = ec.create("to");
        Element typeEl = ec.create("type");
        Element sqTiEl = ec.create("sq_ti_id");
        riverEl.setTextContent(river);
        from.setTextContent(String.valueOf(startKm));
        to.setTextContent(String.valueOf(endKm));
        sqTiEl.setTextContent(sq_ti_id);
        typeEl.setTextContent(type);

        location.appendChild(from);
        location.appendChild(to);
        riverEl.appendChild(location);
        riverEl.appendChild(typeEl);
        riverEl.appendChild(sqTiEl);
        doc.appendChild(riverEl);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document result = client.callService(url, "sedimentloadinfo", doc);

            log.debug("Extract sedimentload info objects now.");
            SedimentLoadInfoObject[] objects =
                extractSedimentLoadInfoObjects(result);

            if (objects != null && objects.length > 0) {
                return objects;
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_SEDIMENTLOADINFO_FOUND);
    }


    /**
     * Extracts all distance info objects from <i>result</i> document.
     *
     * @param result The document retrieved by the server.
     *
     * @return a list of DistanceInfoObjects.
     */
    protected SedimentLoadInfoObject[] extractSedimentLoadInfoObjects(
        Document result)
    throws ServerException
    {
        NodeList list = result.getElementsByTagName("sedimentload");

        if (list == null || list.getLength() == 0) {
            log.warn("No sedimentload info found.");
            throw new ServerException(ERROR_NO_SEDIMENTLOADINFO_DATA);
        }

        int num = list.getLength();
        log.debug("Response contains " + num + " objects.");

        List<SedimentLoadInfoObject> objects =
            new ArrayList<SedimentLoadInfoObject>(num);

        for (int i = 0; i < num; i++) {
            SedimentLoadInfoObject obj = buildSedimentLoadInfoObject(
                (Element)list.item(i));

            if (obj != null) {
                objects.add(obj);
            }
        }

        log.debug("Retrieved " + objects.size() + " sediment loads.");

        return (SedimentLoadInfoObject[])
            objects.toArray(new SedimentLoadInfoObject[num]);
    }


    /**
     * Extracts information for a single distance info object and intializes an
     * DistanceInfoObject with them.
     *
     * @param node The node that contains the information.
     *
     * @return a valid DistanceInfoObject.
     */
    protected SedimentLoadInfoObject buildSedimentLoadInfoObject(Element node) {

        String desc      = node.getAttribute("description").trim();
        String date      = node.getAttribute("date").trim();
        String sq_ti_date= node.getAttribute("sq_date").trim();
        String sq_ti_id  = node.getAttribute("sq_ti_id").trim();

        if (desc.length() > 0 && date.length() > 0) {
            return new SedimentLoadInfoObjectImpl(
                desc, date, sq_ti_date, sq_ti_id);
        }

        log.warn("Invalid sediment load info object found.");

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
