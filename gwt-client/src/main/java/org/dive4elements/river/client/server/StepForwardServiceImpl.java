/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.client.services.StepForwardService;


/**
 * This interface provides a method that bundles the artifact specific
 * operations FEED and ADVANCE.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StepForwardServiceImpl
extends      AdvanceServiceImpl
implements   StepForwardService
{
    private static final Logger log =
        LogManager.getLogger(StepForwardServiceImpl.class);


    /** XPath that points to the result type of a feed or advance operation.*/
    public static final String XPATH_RESULT = "/art:result/@art:type";

    /** XPath that points to the result type of a feed or advance operation.*/
    public static final String XPATH_RESULT_MSG = "/art:result/text()";

    /** A constant that marks errors.*/
    public static final String OPERATION_FAILURE = "FAILURE";

    /** The error message key that is thrown if an error occured while feeding
     * new data.*/
    public static final String ERROR_FEED_DATA = "error_feed_data";


    /**
     * This method wraps the artifact operations FEED and ADVANCE. FEED is
     * always triggerd, ADVANCE only, if there is at least one reachable state.
     *
     * @param locale The locale used for the request.
     * @param artifact The artifact that needs to be fed.
     * @param data An array of Data objects that contain the information that
     *
     * @return the modified artifact.
     */
    public Artifact go(String locale, Artifact artifact, Data[] data)
    throws ServerException
    {
        log.info("StepForwardServiceImpl.go");

        String url = getServletContext().getInitParameter("server-url");

        Artifact afterFeed = feed(url, locale, artifact, data);

        if (afterFeed == null) {
            log.warn("StepForwardService.feed() - FAILED");
            throw new ServerException(ERROR_FEED_DATA);
        }

        ArtifactDescription desc = afterFeed.getArtifactDescription();
        String[] reachable       = desc.getReachableStates();

        if (reachable == null || reachable.length == 0) {
            log.debug("Did not find any reachable state.");
            return afterFeed;
        }

        // We use the first reachable state as default target, maybe we need to
        // change this later.
        return advance(locale, afterFeed, reachable[0]);
    }


    /**
     * This method triggers the FEED operation.
     *
     * @param url The url of the artifact server.
     * @param artifact The artifact that needs to be fed.
     * @param data An array of Data objects that contain the information that
     * are used for the FEED operation.
     *
     * @return a new artifact parsed from the description of FEED.
     */
    protected Artifact feed(
        String   url,
        String   locale,
        Artifact artifact,
        Data[]   data)
    throws    ServerException
    {
        log.info("StepForwardServiceImpl.feed");

        Document feed = ClientProtocolUtils.newFeedDocument(
            artifact.getUuid(),
            artifact.getHash(),
            createKVP(data));

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document description = (Document) client.feed(
                new org.dive4elements.artifacts.httpclient.objects.Artifact(
                    artifact.getUuid(),
                    artifact.getHash()),
                feed,
                new DocumentResponseHandler());

            if (description == null) {
                log.warn("StepForwardService.feed() - FAILED");
                throw new ServerException(ERROR_FEED_DATA);
            }

            String result = XMLUtils.xpathString(
                description,
                XPATH_RESULT,
                ArtifactNamespaceContext.INSTANCE);

            if (result == null || !result.equals(OPERATION_FAILURE)) {
                log.debug("StepForwardService.feed() - SUCCESS");
                return (Artifact) new FLYSArtifactCreator().create(description);
            }
            else if (result != null && result.equals(OPERATION_FAILURE)) {
                String msg = XMLUtils.xpathString(
                    description,
                    XPATH_RESULT_MSG,
                    ArtifactNamespaceContext.INSTANCE);
                throw new ServerException(msg);
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        log.warn("StepForwardService.feed() - FAILED");
        throw new ServerException(ERROR_FEED_DATA);
    }


    /**
     * This method creates an array of key/value pairs from an array of Data
     * objects. The string array is used as parameter for the feed() operation.
     *
     * @param data The data that should be transformed into the string array.
     *
     * @return a string array that contains key/value pairs.
     */
    protected String[][] createKVP(Data[] data) {
        String[][] kvp = new String[data.length][];

        int i = 0;

        for (Data d: data) {
            DataItem[] items = d.getItems();
            String key       = d.getLabel();
            String value     = d.getStringValue();

            kvp[i++] = new String[] { key, value };
        }

        return kvp;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
