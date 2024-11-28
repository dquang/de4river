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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.CreationFilter;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.utils.ArtifactNamespaceContext;
import org.dive4elements.artifacts.httpclient.utils.XMLUtils;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;

import org.dive4elements.river.client.shared.model.Recommendation;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ArtifactHelper {

    /** Private logging instance. */
    private static final Logger log = LogManager.getLogger(ArtifactHelper.class);


    /** The error message key that is thrown if an error occured while artifact
     * creation.*/
    public static final String ERROR_CREATE_ARTIFACT = "error_create_artifact";

    /** Name of the factory to generate a GaugeDischargeCurveArtifact. */
    private static final String GAUGE_DISCHARGE_CURVE_ARTIFACT =
        "gaugedischargecurve";

    /** Name of the factory to generate a MainvaluesArtifact. */
    private static final String MAINVALUE_ARTIFACT_FACTORY = "mainvalue";

    private static final String SQ_RELATION_ARTIFACT = "staticsqrelation";

    // To prevent pile up of create artifact calls only permit a limited
    // number of parallel creates.
    public static final int MAX_CREATE = 5;

    private static final Semaphore CREATE_SEMAPHORE = new Semaphore(MAX_CREATE);

    private ArtifactHelper() {
    }


    /**
     * @param factory ArtifactFactory to use.
     */
    public static Artifact createArtifact(
        String         serverUrl,
        String         locale,
        String         factory,
        Recommendation recommendation)
    throws ServerException
    {
        String         uuid;
        String         ids;
        CreationFilter filter;
        String         targetOut;

        if (recommendation != null) {
            uuid      = recommendation.getMasterArtifact();
            ids       = recommendation.getIDs();
            filter    = convertFilter(recommendation.getFilter());
            targetOut = recommendation.getTargetOut();
        }
        else {
            uuid      = null;
            ids       = null;
            filter    = null;
            targetOut = null;
        }

        log.debug("ArtifactHelper.create for master: " + uuid + " ids: " + ids +
                " filter: " + filter + " targetOut: " + targetOut);
        Document create = ClientProtocolUtils.newCreateDocument(
            factory, uuid, ids, filter, targetOut);

        return sendCreate(serverUrl, locale, create);
    }

    /**
     * Creates a new MainvaluesArtifact.
     *
     * @param river the name of the river
     */
    public static Artifact createMainvalueArtifact(
            String serverUrl,
            String locale,
            String river,
            Long   gaugeRef)
    throws ServerException
    {
        Document create = ClientProtocolUtils.newCreateDocument(
                MAINVALUE_ARTIFACT_FACTORY);

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            create,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = create.getDocumentElement();

        Element eriver = ec.create("river");
        ec.addAttr(eriver, "name", river);

        Element egauge = ec.create("gauge");
        ec.addAttr(egauge, "reference", gaugeRef.toString());

        root.appendChild(eriver);
        root.appendChild(egauge);

        return sendCreate(serverUrl, locale, create);
    }

    /**
     * Sends a create document to the artifact server.
     */
    private static Artifact sendCreate(
            String   serverUrl,
            String   locale,
            Document doc)
    throws ServerException
    {
        try {
            CREATE_SEMAPHORE.acquire();
        }
        catch (InterruptedException ie) {
            throw new ServerException(ERROR_CREATE_ARTIFACT);
        }
        try {
            HttpClient client = new HttpClientImpl(serverUrl, locale);

            try {
                return (Artifact) client.create(doc, new FLYSArtifactCreator());
            }
            catch (ConnectionException ce) {
                log.error(ce, ce);
            }

            throw new ServerException(ERROR_CREATE_ARTIFACT);
        }
        finally {
            CREATE_SEMAPHORE.release();
        }
    }


    /**
     * Create CreationFilter from Recommendation.Filter.
     */
    public static CreationFilter convertFilter(Recommendation.Filter filter) {

        if (filter == null) {
            return null;
        }

        CreationFilter cf = new CreationFilter();

        Map<String, List<Recommendation.Facet>> outs = filter.getOuts();

        for (Map.Entry<String, List<Recommendation.Facet>> entry:
            outs.entrySet()) {
            List<Recommendation.Facet> rfs = entry.getValue();
            List<CreationFilter.Facet> cfs =
                new ArrayList<CreationFilter.Facet>(rfs.size());
            for (Recommendation.Facet rf: rfs) {
                cfs.add(new CreationFilter.Facet(rf.getName(), rf.getIndex()));
            }
            cf.add(entry.getKey(), cfs);
        }

        return cf;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
