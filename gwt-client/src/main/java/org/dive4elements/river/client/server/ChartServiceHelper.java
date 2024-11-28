/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartServiceHelper {

    private static final Logger log =
        LogManager.getLogger(ChartServiceHelper.class);


    /** The default chart width if no value is specified in the request.*/
    public static final int DEFAULT_CHART_WIDTH  = 600;

    /** The default chart height if no value is specified in the request.*/
    public static final int DEFAULT_CHART_HEIGHT = 400;


    private ChartServiceHelper() {
    }

    /**
     * This method returns a document which might contain parameters to adjust
     * chart settings. The document is created using the information that are
     * contained in the request object.
     *
     * @param req The request document.
     *
     * @return a document to adjust chart settings.
     */
    protected static Document getChartAttributes(Map<String, String> req) {
        log.debug("ChartServiceHelper.getChartAttributes");

        Document doc = XMLUtils.newDocument();

        ElementCreator ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element attributes = ec.create("attributes");

        appendChartSize(req, attributes, ec);
        appendFormat(req, attributes, ec);
        appendXRange(req, attributes, ec);
        appendYRange(req, attributes, ec);
        appendCurrentKm(req, attributes, ec);

        doc.appendChild(attributes);

        return doc;
    }


    /**
     * This method extracts the size (width/height) of a chart from request
     * object and append those values - if they exist - to the attribute
     * document used to adjust chart settings.
     *
     * @param req The request object that might contain the chart size.
     * @param attributes The attributes element used to adjust chart settings.
     * @param ec The ElementCreator that might be used to create new Elements.
     */
    protected static void appendChartSize(
        Map<String, String> req,
        Element             attributes,
        ElementCreator      ec)
    {
        log.debug("ChartServiceHelper.appendChartSize");

        Element size = ec.create("size");

        String width  = req.get("width");
        String height = req.get("height");

        if (width == null || height == null) {
            width  = String.valueOf(DEFAULT_CHART_WIDTH);
            height = String.valueOf(DEFAULT_CHART_HEIGHT);
        }

        ec.addAttr(size, "width", width, true);
        ec.addAttr(size, "height", height, true);

        attributes.appendChild(size);
    }


    /**
     * This method extracts the x range for the chart from request object and
     * appends those range - if it exists - to the attribute document used to
     * adjust the chart settings.
     *
     * @param req The request object that might contain the chart size.
     * @param doc The attribute document used to adjust chart settings.
     * @param ec The ElementCreator that might be used to create new Elements.
     */
    protected static void appendXRange(
        Map<String, String> req,
        Element             attributes,
        ElementCreator      ec)
    {
        log.debug("ChartServiceHelper.appendXRange");

        Element range = ec.create("xrange");

        String from = req.get("minx");
        String to   = req.get("maxx");

        if (from != null && to != null) {
            ec.addAttr(range, "from", from, true);
            ec.addAttr(range, "to", to, true);

            attributes.appendChild(range);
        }
    }


    /**
     * This method extracts the x range for the chart from request object and
     * appends those range - if it exists - to the attribute document used to
     * adjust the chart settings.
     *
     * @param req The request object that might contain the chart size.
     * @param doc The attribute document used to adjust chart settings.
     * @param ec The ElementCreator that might be used to create new Elements.
     */
    protected static void appendYRange(
        Map<String, String> req,
        Element             attributes,
        ElementCreator      ec)
    {
        log.debug("ChartServiceHelper.appendYRange");

        Element range = ec.create("yrange");

        String from = req.get("miny");
        String to   = req.get("maxy");

        if (from != null && to != null) {
            ec.addAttr(range, "from", from, true);
            ec.addAttr(range, "to", to, true);

            attributes.appendChild(range);
        }
    }


    /**
     * This method extracts the format string from request object and appends
     * those format - if existing - to the attribute document used to adjust
     * the chart settings.
     *
     * @param req The request object that might contain the chart format.
     * @param doc The attribute document used to adjust chart settings.
     * @param ec The ElementCreator that might be used to create new Elements.
     */
    protected static void appendFormat(
        Map<String, String> req,
        Element             attributes,
        ElementCreator      ec

    ) {
        log.debug("ChartServiceHelper.appendFormat");

        String formatStr = req.get("format");
        if (formatStr == null || formatStr.length() == 0) {
            return;
        }

        Element format = ec.create("format");
        ec.addAttr(format, "value", formatStr, true);

        attributes.appendChild(format);
    }


    /**
     * This method extracts the current km for the chart from request object and
     * appends this km - if it exists - to the attribute document used to
     * adjust the chart settings.
     *
     * @param req The request object that might contain the chart size.
     * @param doc The attribute document used to adjust chart settings.
     * @param ec The ElementCreator that might be used to create new Elements.
     */
    protected static void appendCurrentKm(
        Map<String, String> req,
        Element             attributes,
        ElementCreator      ec)
    {
        log.debug("ChartServiceHelper.appendCurrentKm");

        Element currentKm = ec.create("currentKm");

        String km = req.get("km");

        if (km != null) {
            ec.addAttr(currentKm, "km", km, true);

            attributes.appendChild(currentKm);
        }
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
