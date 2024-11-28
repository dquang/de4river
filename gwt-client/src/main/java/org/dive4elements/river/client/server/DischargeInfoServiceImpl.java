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
import org.dive4elements.river.client.client.services.DischargeInfoService;
import org.dive4elements.river.client.shared.model.DischargeInfoObject;
import org.dive4elements.river.client.shared.model.DischargeInfoObjectImpl;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DischargeInfoServiceImpl
extends      RemoteServiceServlet
implements   DischargeInfoService
{
    private static final Logger log =
        LogManager.getLogger(DischargeInfoServiceImpl.class);

    public static final String ERROR_NO_DISCHARGEINFO_FOUND =
        "error_no_dischargeinfo_found";

    public static final String XPATH_DISTANCES = "art:discharges/art:discharge";


    @Override
    public DischargeInfoObject[] getDischargeInfo(
        String locale,
        long gauge,
        String river)
    throws ServerException
    {
        log.info("DichargeInfoServiceImpl.getDischargeInfo");

        String url  = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element gaugeEl = ec.create("gauge");
        gaugeEl.setTextContent(String.valueOf(gauge));

        Element riverEl = ec.create("river");
        riverEl.setTextContent(river);

        gaugeEl.appendChild(riverEl);
        doc.appendChild(gaugeEl);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document result = client.callService(url, "dischargeinfo", doc);

            log.debug("Extract discharge info objects now.");
            DischargeInfoObject[] objects = extractDischargeInfoObjects(result);

            if (objects != null && objects.length > 0) {
                return objects;
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_DISCHARGEINFO_FOUND);
    }

    protected DischargeInfoObject[] extractDischargeInfoObjects(
        Document result
    )
    throws ServerException {
        NodeList list = result.getElementsByTagName("discharge");

        if (list == null || list.getLength() == 0) {
            log.warn("No discharge info found.");
            throw new ServerException(ERROR_NO_DISCHARGEINFO_FOUND);
        }

        int num = list.getLength();
        log.debug("Response contains " + num + " objects.");

        List<DischargeInfoObject> objects =
            new ArrayList<DischargeInfoObject>(num);

        for (int i = 0; i < num; i++) {
            DischargeInfoObject obj = buildDischargeInfoObject(
                (Element)list.item(i));

            if (obj != null) {
                objects.add(obj);
            }
        }

        log.debug("Retrieved " + objects.size() + " discharges.");

        return (DischargeInfoObject[])
            objects.toArray(new DischargeInfoObject[num]);

    }

    protected DischargeInfoObject buildDischargeInfoObject(Element node) {

        String desc  = node.getAttribute("description").trim();
        String start = node.getAttribute("start").trim();
        String end   = node.getAttribute("end").trim();
        String bfgId = node.getAttribute("bfg-id").trim();

        if (start.length() > 0 && end.length() > 0) {
            try {
                Integer startYear  = Integer.valueOf(start);
                Integer endYear    = Integer.valueOf(end);
                return new DischargeInfoObjectImpl(
                    desc, startYear, endYear, bfgId);
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
