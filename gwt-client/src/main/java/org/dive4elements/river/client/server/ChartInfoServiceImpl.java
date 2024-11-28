/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.shared.Transform2D;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Axis;
import org.dive4elements.river.client.shared.model.DateAxis;
import org.dive4elements.river.client.shared.model.NumberAxis;
import org.dive4elements.river.client.shared.model.ChartInfo;
import org.dive4elements.river.client.shared.model.Collection;

import org.dive4elements.river.client.client.services.ChartInfoService;


/**
 * This service fetches a document that contains meta information for a specific
 * chart.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartInfoServiceImpl
extends      RemoteServiceServlet
implements   ChartInfoService
{
    private static final Logger log =
        LogManager.getLogger(ChartInfoServiceImpl.class);

    public static final String XPATH_TRANSFORM_MATRIX =
        "/art:chartinfo/art:transformation-matrix/art:matrix";

    public static final String XPATH_X_AXES =
        "/art:chartinfo/art:axes/art:domain";

    public static final String XPATH_Y_AXES =
        "/art:chartinfo/art:axes/art:range";

    public static final String EXCEPTION_STRING = "error_chart_info_service";


    public ChartInfo getChartInfo(
        Collection          collection,
        String              locale,
        String              type,
        Map<String, String> attr)
    throws ServerException
    {
        log.info("ChartInfoServiceImpl.getChartInfo");

        String url  = getServletContext().getInitParameter("server-url");

        Document request = ClientProtocolUtils.newOutCollectionDocument(
                collection.identifier(),
                type,
                type,
                ChartServiceHelper.getChartAttributes(attr));

        try {
            HttpClient client = new HttpClientImpl(url, locale);
            InputStream in    = client.collectionOut(
                request,
                collection.identifier(),
                type + "_chartinfo");

            Document info = XMLUtils.parseDocument(in);

            return parseInfoDocument(info);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        log.debug("Error while fetching chart info.");

        throw new ServerException(EXCEPTION_STRING);
    }


    /**
     * Parse ChartInfo-Part of document, create Transforms and axes
     * from it.
     */
    protected ChartInfo parseInfoDocument(Document doc) {
        Transform2D[] transformer = parseTransformationMatrix(doc);
        Axis[]      xAxes         = parseXAxes(doc);
        Axis[]      yAxes         = parseYAxes(doc);

        return new ChartInfo(xAxes, yAxes, transformer);
    }


    protected Axis[] parseXAxes(Document doc) {
        log.debug("ChartInfoServiceImpl.parseXAxes");

        NodeList axes = (NodeList) XMLUtils.xpath(
            doc,
            XPATH_X_AXES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        return parseAxes(axes);
    }


    protected Axis[] parseYAxes(Document doc) {
        log.debug("ChartInfoServiceImpl.parseYAxes");

        NodeList axes = (NodeList) XMLUtils.xpath(
            doc,
            XPATH_Y_AXES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        return parseAxes(axes);
    }


    protected Axis[] parseAxes(NodeList axes) {
        log.debug("ChartInfoServiceImpl.parseAxes");

        int count = axes != null ? axes.getLength() : 0;

        log.debug("Chart has " + count + " axes.");

        if (count == 0) {
            return null;
        }

        Axis[] result = new Axis[count];

        String ns = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int i = 0; i < count; i++) {
            Element node = (Element) axes.item(i);

            String posStr   = node.getAttributeNS(ns, "pos");
            String fromStr  = node.getAttributeNS(ns, "from");
            String toStr    = node.getAttributeNS(ns, "to");
            String minStr   = node.getAttributeNS(ns, "min");
            String maxStr   = node.getAttributeNS(ns, "max");
            String axisType = node.getAttributeNS(ns, "axistype");

            try {
                int    pos  = Integer.parseInt(posStr);

                if (pos >= result.length) {
                    // this should never happen
                    log.debug("The axis is out of valid range: " + pos);
                    continue;
                }

                if (axisType != null && axisType.equals(DateAxis.TYPE)) {
                    long from = Long.parseLong(fromStr);
                    long to   = Long.parseLong(toStr);
                    long min  = Long.parseLong(minStr);
                    long max  = Long.parseLong(maxStr);

                    if (log.isDebugEnabled()) {
                        log.debug("date axis from: " + new Date(from));
                        log.debug("date axis to  : " + new Date(to));
                        log.debug("date axis min : " + new Date(min));
                        log.debug("date axis max : " + new Date(max));
                    }

                    result[pos] = new DateAxis(pos, from, to, min, max);
                }
                else {
                    double from = Double.parseDouble(fromStr);
                    double to   = Double.parseDouble(toStr);
                    double min  = Double.parseDouble(minStr);
                    double max  = Double.parseDouble(maxStr);

                    result[pos] = new NumberAxis(pos, from, to, min, max);
                }
            }
            catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        log.debug("Parsed " + result.length + " axes");

        return result;
    }


    /**
     * Parses the chart info document and extract the Transform2D values.
     *
     * @param doc The chart info document.
     *
     * @return a Transform2D object to transfrom pixel coordinates into chart
     * coordinates.
     */
    protected Transform2D[] parseTransformationMatrix(Document doc) {
        log.debug("ChartInfoServiceImpl.parseTransformationMatrix");

        NodeList matrix = (NodeList) XMLUtils.xpath(
            doc,
            XPATH_TRANSFORM_MATRIX,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        int num = matrix != null ? matrix.getLength() : 0;

        List<Transform2D> transformer = new ArrayList<Transform2D>(num);

        for (int i = 0; i < num; i++) {
            Transform2D t = createTransformer((Element) matrix.item(i));

            if (t == null) {
                log.warn("Broken transformation matrix at pos: " + i);
                continue;
            }

            transformer.add(t);
        }

        return transformer.toArray(new Transform2D[num]);
    }


    protected Transform2D createTransformer(Element matrix) {
        String ns = ArtifactNamespaceContext.NAMESPACE_URI;

        String sx    = matrix.getAttributeNS(ns, "sx");
        String sy    = matrix.getAttributeNS(ns, "sy");
        String tx    = matrix.getAttributeNS(ns, "tx");
        String ty    = matrix.getAttributeNS(ns, "ty");
        String xType = matrix.getAttributeNS(ns, "xtype");
        String yType = matrix.getAttributeNS(ns, "ytype");

        xType = xType == null || xType.length() == 0 ? "number" : xType;
        yType = yType == null || yType.length() == 0 ? "number" : yType;

        if (sx != null && sy != null && tx != null && ty != null) {
            try {
                log.debug("Create new Transform2D with x format: " + xType);
                log.debug("Create new Transform2D with y format: " + yType);

                return new Transform2D(
                    Double.parseDouble(sx),
                    Double.parseDouble(sy),
                    Double.parseDouble(tx),
                    Double.parseDouble(ty),
                    xType, yType);
            }
            catch (NumberFormatException nfe) {
                log.warn("Error while parsing matrix values.");
            }
        }

        log.warn("No matrix values found.");

        return new Transform2D(1d, 1d, 0d, 0d);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
