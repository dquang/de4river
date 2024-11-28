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

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;

import org.dive4elements.artifacts.httpclient.utils.ArtifactCreator;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.CalculationMessage;
import org.dive4elements.river.client.shared.model.ChartArtifact;
import org.dive4elements.river.client.shared.model.DefaultArtifact;
import org.dive4elements.river.client.shared.model.FixAnalysisArtifact;
import org.dive4elements.river.client.shared.model.GaugeDischargeCurveArtifact;
import org.dive4elements.river.client.shared.model.MapArtifact;
import org.dive4elements.river.client.shared.model.MINFOArtifact;
import org.dive4elements.river.client.shared.model.StaticSQRelationArtifact;
import org.dive4elements.river.client.shared.model.WINFOArtifact;


/**
 * An implementation of an {@link ArtifactCreator}. This class uses the document
 * that is returned by the artifact server to parse important information (like
 * uuid, hash) and returns a new {@link Artifact} instance.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FLYSArtifactCreator implements ArtifactCreator {

    private static final Logger log =
        LogManager.getLogger(FLYSArtifactCreator.class);


    /** The XPath to the artifact's uuid.*/
    public static final String XPATH_UUID = "/art:result/art:uuid/@art:value";

    /** The XPath to the artifact's hash value.*/
    public static final String XPATH_HASH = "/art:result/art:hash/@art:value";

    /** The XPath to the artifact's name value.*/
    public static final String XPATH_NAME = "/art:result/art:name/@art:value";

    /** The XPath to the value that determines if the artifact is processing in
     * background.*/
    public static final String XPATH_BACKGROUND_VALUE =
        "/art:result/art:background-processing/@art:value";

    /** The XPath that points to the (if existing) background messages.*/
    public static final String XPATH_BACKGROUND =
        "/art:result/art:background-processing";


    /**
     * Creates a new instance of an {@link ArtifactCreator}.
     */
    public FLYSArtifactCreator() {
    }


    /**
     * This concreate implementation returns an instance of {@link Artifact}
     * that is used in the FLYS GWT Client code.
     *
     * @param doc A document that describes the artifact that has been created
     * in the artifact server.
     *
     * @return an instance if {@link Artifact}.
     */
    public Object create(Document doc) {
        Artifact artifact = extractArtifact(doc);
        artifact.setArtifactDescription(
            ArtifactDescriptionFactory.createArtifactDescription(doc));

        return artifact;
    }


    /**
     * This method extracts the UUID und HASH information of the returned
     * artifact document.
     *
     * @param doc The result of the CREATE operation.
     *
     * @return an instance of an {@link Artifact}.
     */
    protected Artifact extractArtifact(Document doc) {
        log.debug("FLYSArtifactCreator - extractArtifact()");

        String uuid = XMLUtils.xpathString(
            doc, XPATH_UUID, ArtifactNamespaceContext.INSTANCE);

        String hash = XMLUtils.xpathString(
            doc, XPATH_HASH, ArtifactNamespaceContext.INSTANCE);

        String name = XMLUtils.xpathString(
            doc, XPATH_NAME, ArtifactNamespaceContext.INSTANCE);

        String backgroundStr = XMLUtils.xpathString(
            doc, XPATH_BACKGROUND_VALUE, ArtifactNamespaceContext.INSTANCE);

        boolean background = false;
        if (backgroundStr != null && backgroundStr.length() > 0) {
            background = Boolean.valueOf(backgroundStr);
        }

        List<CalculationMessage> msg = parseBackgroundMessages(doc);

        log.debug("NEW Artifact UUID: " + uuid);
        log.debug("NEW Artifact HASH: " + hash);
        log.debug("NEW Artifact NAME: " + name);
        log.debug("NEW Artifact IN BACKGROUND: " + background);

        if (name == null) {
            return new DefaultArtifact(uuid, hash, background, msg);
        }

        name = name.trim();

        if (name.length() > 0 && name.equals("winfo")) {
            log.debug("+++++ NEW WINFO ARTIFACT.");
            return new WINFOArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("new_map")) {
            log.debug("+++++ NEW MAP ARTIFACT.");
            return new MapArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("new_chart")) {
            log.debug("+++++ NEW CHART ARTIFACT.");
            return new ChartArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("minfo")) {
            log.debug("+++++ NEW MINFO ARTIFACT.");
            return new MINFOArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("fixanalysis")) {
            log.debug("+++++ NEW FIXANALYSIS ARTIFACT.");
            return new FixAnalysisArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("gaugedischargecurve")) {
            log.debug("+++++ NEW GAUGEDISCHARGECURVE ARTIFACT.");
            return new GaugeDischargeCurveArtifact(uuid, hash, background, msg);
        }
        else if (name.length() > 0 && name.equals("staticsqrelation")) {
            log.debug("+++++ STATICSQRELATION ARTIFACT.");
            return new StaticSQRelationArtifact(uuid, hash, background, msg);
        }

        return new DefaultArtifact(uuid, hash, background, msg);
    }


    public static List<CalculationMessage> parseBackgroundMessages(Document d) {
        NodeList list = (NodeList) XMLUtils.xpath(
            d, XPATH_BACKGROUND, XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int len = list != null ? list.getLength() : 0;

        log.debug("Found " + len + " background messages.");

        List<CalculationMessage> res = new ArrayList<CalculationMessage>(len);

        for (int i = 0; i < len; i++) {
            CalculationMessage msg = parseBackgroundMessage(
                (Element) list.item(i));

            if (msg != null) {
                res.add(msg);
            }
        }

        return res;
    }


    public static CalculationMessage parseBackgroundMessage(Element e) {
        String steps       = e.getAttribute("art:steps");
        String currentStep = e.getAttribute("art:currentStep");
        String message     = e.getTextContent();

        int lenCurStep = currentStep != null ? currentStep.length() : 0;
        int lenSteps   = steps       != null ? steps.length()       : 0;
        int lenMessage = message     != null ? message.length()     : 0;

        if (lenSteps > 0 && lenMessage > 0 && lenCurStep > 0) {
            try {
                return new CalculationMessage(
                    Integer.parseInt(steps),
                    Integer.parseInt(currentStep),
                    message);

            }
            catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
