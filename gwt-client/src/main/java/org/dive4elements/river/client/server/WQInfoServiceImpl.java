/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import org.dive4elements.river.client.client.services.WQInfoService;
import org.dive4elements.river.client.shared.model.WQInfoObject;
import org.dive4elements.river.client.shared.model.WQInfoObjectImpl;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQInfoServiceImpl
extends      RemoteServiceServlet
implements   WQInfoService
{
    private static final Logger log =
        LogManager.getLogger(WQInfoServiceImpl.class);

    public static final String ERROR_NO_WQINFO_FOUND =
        "error_no_wqinfo_found";

    public static final String XPATH_WQS =
        "art:service/art:mainvalues/art:mainvalue";


    public WQInfoObject[] getWQInfo(
        String locale,
        String river,
        double from,
        double to)
    throws ServerException
    {
        log.info("WQInfoServiceImpl.getWQInfo");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element mainvalues = ec.create("mainvalues");
        Element riverEl = ec.create("river");
        Element startEl = ec.create("start");
        Element endEl   = ec.create("end");

        riverEl.setTextContent(river);
        startEl.setTextContent(Double.valueOf(from).toString());
        endEl.setTextContent(Double.valueOf(to).toString());

        mainvalues.appendChild(riverEl);
        mainvalues.appendChild(startEl);
        mainvalues.appendChild(endEl);

        doc.appendChild(mainvalues);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document result = client.callService(url, "mainvalues", doc);

            log.debug("Extract wq info objects now.");
            WQInfoObject[] objects = extractWQInfoObjects(result);

            if (objects.length > 0) {
                return objects;
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_WQINFO_FOUND);
    }


    /**
     * Extracts all wq info objects from <i>result</i> document.
     *
     * @param result The document retrieved by the server.
     *
     * @return a list of WQInfoObjects.
     */
    protected WQInfoObject[] extractWQInfoObjects(Document result)
    throws    ServerException
    {
        NodeList list = (NodeList) XMLUtils.xpath(
            result,
            XPATH_WQS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (list == null || list.getLength() == 0) {
            log.warn("No wq info found.");

            throw new ServerException(ERROR_NO_WQINFO_FOUND);
        }

        boolean debug = log.isDebugEnabled();

        int num = list.getLength();
        if (debug) {
            log.debug("Response contains " + num + " objects.");
        }

        List<WQInfoObject> objects =
            new ArrayList<WQInfoObject>(num);

        for (int i = 0; i < num; i++) {
            WQInfoObject obj = buildWQInfoObject(list.item(i));

            if (obj != null) {
                objects.add(obj);
            }
        }

        if (debug) {
            log.debug("Retrieved " + objects.size() + " wq values");
        }

        WQInfoObject [] array = (WQInfoObject[])
            objects.toArray(new WQInfoObject[objects.size()]);

        Arrays.sort(array, WQ_INFO_OBJECT_CMP);

        return array;
    }

    public static final Comparator<WQInfoObject> WQ_INFO_OBJECT_CMP =
        new Comparator<WQInfoObject>() {
            @Override
            public int compare(WQInfoObject a, WQInfoObject b) {

                // Descending by type: Qs before Ds
                int cmp = a.getType().compareTo(b.getType());
                if (cmp < 0) return +1;
                if (cmp > 0) return -1;

                // Ascending by value
                double diff = a.getValue() - b.getValue();
                if (diff < 0d) return -1;
                if (diff > 0d) return +1;
                return 0;
            }
        };

    /**
     * Extracts information for a single wq info object and intializes an
     * WQInfoObject with them.
     *
     * @param node The node that contains the information.
     *
     * @return a valid WQInfoObject.
     */
    protected static WQInfoObject buildWQInfoObject(Node node) {

        String name = XMLUtils.xpathString(
            node, "@name", ArtifactNamespaceContext.INSTANCE);

        String type = XMLUtils.xpathString(
            node, "@type", ArtifactNamespaceContext.INSTANCE);

        String value = XMLUtils.xpathString(
            node, "@value", ArtifactNamespaceContext.INSTANCE);

        String official = XMLUtils.xpathString(
            node, "@official", ArtifactNamespaceContext.INSTANCE);

        String starttime = XMLUtils.xpathString(
            node, "@starttime", ArtifactNamespaceContext.INSTANCE);

        String stoptime = XMLUtils.xpathString(
            node, "@stoptime", ArtifactNamespaceContext.INSTANCE);

        if (name != null && type != null) {
            try {
                Calendar cal = Calendar.getInstance();
                java.util.Date start = null;
                java.util.Date stop = null;
                if (!starttime.equals("")) {
                    cal.setTimeInMillis(Long.parseLong(starttime));
                    start = cal.getTime();
                }
                if (!stoptime.equals("")) {
                    cal.setTimeInMillis(Long.parseLong(stoptime));
                    stop = cal.getTime();
                }
                return new WQInfoObjectImpl(
                    name,
                    type,
                    new Double(value),
                    official != null && official.equalsIgnoreCase("true"),
                    start,
                    stop);
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe.getLocalizedMessage());
            }
        }

        log.warn("Invalid wq info object found.");

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
