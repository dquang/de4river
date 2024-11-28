/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.HashMap;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;

import org.dive4elements.river.artifacts.datacage.Recommendations;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * Following XPaths are evaluated on the incoming document.
 *
 *  "/art:meta/art:artifact-id/@value" The UUID of the artifact. Optional.
 *                                     Used to fill the template enviroment.
 *  "/art:meta/art:user-id/@value"     The UUID of the user. Optional.
 *                                     If given the user specific template is
 *                                     filled.
 *  "/art:meta/art:outs/@value"        The list of outs used to recommend
 *                                     for the
 *                                     various outputs.
 *  "/art:meta/art:parameters/@value"  A list of key/value pairs to inject more
 *                                     filters to the templating, as
 *                                     "key:value;key2:value2"
 */
public class MetaDataService
extends      D4EService
{
    private static Logger log = LogManager.getLogger(MetaDataService.class);

    public static final String XPATH_ARTIFACT_ID =
        "/art:meta/art:artifact-id/@value";
    public static final String XPATH_USER_ID =
        "/art:meta/art:user-id/@value";
    public static final String XPATH_OUTS =
        "/art:meta/art:outs/@value";
    public static final String XPATH_PARAMETERS =
        "/art:meta/art:parameters/@value";

    /** The global context key of the artifact database. */
    public static final String ARTIFACT_DATA_BASE_KEY =
        "global.artifact.database";

    public MetaDataService() {
    }

    @Override
    protected Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("MetaDataService.process");

        String artifactId = XMLUtils.xpathString(
            data, XPATH_ARTIFACT_ID, ArtifactNamespaceContext.INSTANCE);

        if (artifactId != null
        && (artifactId = artifactId.trim()).length() == 0) {
            artifactId = null;
        }

        String userId = XMLUtils.xpathString(
            data, XPATH_USER_ID, ArtifactNamespaceContext.INSTANCE);

        if (userId != null
        && (userId = userId.trim()).length() == 0) {
            userId = null;
        }

        String outs = XMLUtils.xpathString(
            data, XPATH_OUTS, ArtifactNamespaceContext.INSTANCE);

        String parameters = XMLUtils.xpathString(
            data, XPATH_PARAMETERS, ArtifactNamespaceContext.INSTANCE);

        return doService(
            artifactId, userId, outs, parameters, globalContext);
    }


    /**
     * Split parameterstring in the form of key1:value1;key2:value2
     * into hash (key1->value1, key2->value2).
     * @param parameters "key1:value1;key2:value2"
     * @param data Map into wich to put parameter hash and return.
     * @return parameter data
     */
    protected static Map<String, Object> splitParameters(
        String              parameters,
        Map<String, Object> data
    ) {
        if (parameters != null) {
            String [] parts = parameters.split("\\s*;\\s*");
            for (String part: parts) {
                String [] kv = part.split("\\s*:\\s*");
                if (kv.length < 2 || (kv[0] = kv[0].trim()).length() == 0) {
                    continue;
                }
                String [] values = kv[1].split("\\s*,\\s*");
                data.put(kv[0], values.length == 1 ? values[0] : values);
            }
        }
        return data;
    }

    /** Return the document containing matched stuff from meta-data.xml. */
    protected Document doService(
        String        artifactId,
        String        userId,
        String        outsString,
        String        parameters,
        GlobalContext globalContext
    ) {
        Document result = XMLUtils.newDocument();

        D4EArtifact flysArtifact;

        if (log.isDebugEnabled()) {
            log.debug("artifact  : " + artifactId);
            log.debug("user      : " + userId);
            log.debug("outs      : " + outsString);
            log.debug("parameters: " + parameters);
        }

        if (userId != null && !StringUtils.checkUUID(userId)) {
            log.warn("'" + userId + "' is not a UUID");
            return result;
        }

        if (artifactId != null) {
            if (!StringUtils.checkUUID(artifactId)) {
                log.warn("'" + artifactId + "' is not a UUID");
                return result;
            }

            Object dbObject =
                (ArtifactDatabase)globalContext.get(ARTIFACT_DATA_BASE_KEY);

            if (!(dbObject instanceof ArtifactDatabase)) {
                log.error("Cannot find artifact database");
                return result;
            }

            ArtifactDatabase db = (ArtifactDatabase)dbObject;

            Artifact artifact;

            try {
                artifact = db.getRawArtifact(artifactId);
            }
            catch (ArtifactDatabaseException adbe) {
                log.warn("fetching artifact failed", adbe);
                return result;
            }

            if (!(artifact instanceof D4EArtifact)) {
                log.warn("artifact is not a D4E artifact.");
                return result;
            }

            flysArtifact = (D4EArtifact)artifact;
        }
        else {
            flysArtifact = null;
        }


        Map<String, Object> data = splitParameters(
            parameters, new HashMap<String, Object>());

        String [] outs = outsString == null
            ? new String [0]
            : outsString.split("\\s*,\\s*");

        Recommendations rec = Recommendations.getInstance();
        rec.recommend(
            flysArtifact, userId, outs, data, result);

        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
