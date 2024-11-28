/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.MainValueType;
import org.dive4elements.river.model.NamedMainValue;
import org.dive4elements.river.model.OfficialLine;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.model.RiverFactory;

import static org.dive4elements.river.backend.utils.EpsilonComparator.CMP;


/**
 * This service returns the main values of a river's gauge based on the start
 * and end point of the river.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MainValuesService extends D4EService {

    /** The log that is used by this service.*/
    private static Logger log = LogManager.getLogger(MainValuesService.class);

    /** XPath that points to the river definition of the incoming request.*/
    public static final String XPATH_RIVER =
        "/art:mainvalues/art:river/text()";

    /** XPath that points to the start definition of the incoming request.*/
    public static final String XPATH_START =
        "/art:mainvalues/art:start/text()";

    /** The XPath that points to the end definition of the incoming request.*/
    public static final String XPATH_END = "/art:mainvalues/art:end/text()";

    protected CallMeta callMeta;


    /**
     * The default constructor.
     */
    public MainValuesService() {
    }

    private static final Document error(String msg) {
        log.debug(msg);
        return XMLUtils.newDocument();
    }


    @Override
    public Document doProcess(
        Document      data,
        GlobalContext context,
        CallMeta      callMeta
    ) {
        log.debug("MainValuesService.process");

        this.callMeta = callMeta;

        River river = getRequestedRiver(data);
        if (river == null) {
            return error("no river found.");
        }

        double[] minmax = getRequestedStartEnd(data, river);
        Gauge gauge = river.determineRefGauge(minmax,
                CMP.compare(minmax[0], minmax[1]) != 0);

        if (gauge == null) {
            return error("no gauge found.");
        }

        List<MainValue> mainValues = getMainValues(river, gauge);

        return buildDocument(river, gauge, mainValues, context);
    }


    /**
     * This method extracts the river from the incoming request. If no river
     * string was found or no river is found in the database based on this
     * string a NullPointerException is thrown.
     *
     * @param data The incoming request data.
     *
     * @return the River object.
     */
    protected River getRequestedRiver(Document data)
    throws    NullPointerException
    {
        log.debug("MainValuesService.getRequestedRiver");

        String riverStr = XMLUtils.xpathString(
            data, XPATH_RIVER, ArtifactNamespaceContext.INSTANCE);

         return riverStr != null && (riverStr = riverStr.trim()).length() > 0
            ? RiverFactory.getRiver(riverStr)
            : null;
    }


    /**
     * This method extracts the start and end point from incoming request
     * document and returns both values in an array.
     * If no start and end strings
     * are found in the document, the min/max values of the <i>river</i> are
     * returned.
     *
     * @param data The incoming request data.
     * @param river The river of the request.
     *
     * @return the start and end point.
     */
    protected double[] getRequestedStartEnd(Document data, River river) {
        log.debug("MainValuesService.getStartEnd");

        String startStr = XMLUtils.xpathString(
            data, XPATH_START, ArtifactNamespaceContext.INSTANCE);

        String endStr = XMLUtils.xpathString(
            data, XPATH_END, ArtifactNamespaceContext.INSTANCE);

        if (startStr == null || endStr == null) {
            return river.determineMinMaxDistance();
        }

        try {
            double start = Double.parseDouble(startStr);
            double end   = Double.parseDouble(endStr);

            if (log.isDebugEnabled()) {
                log.debug("Found start: " + start);
                log.debug("Found end: " + end);
            }

            return new double[] { start, end };
        }
        catch (NumberFormatException nfe) {
            log.warn(nfe, nfe);
            return river.determineMinMaxDistance();
        }
    }


    /**
     * This method creates the result document that includes the main values of
     * the specified <i>gauge</i>.
     *
     * @param river The river.
     * @param gauge The gauge.
     *
     * @return a document that includes the main values of the specified river
     * at the specified gauge.
     */
    protected List<MainValue> getMainValues(River river, Gauge gauge) {

        if (log.isDebugEnabled()) {
            log.debug("MainValuesService.buildMainValues");
            log.debug("River: " + river.getName());
            log.debug("Gauge: " + gauge.getName());
        }

        List<MainValue> mainValues = gauge.getMainValues();

        if (log.isDebugEnabled()) {
            log.debug(mainValues.size() + " main values found.");
        }

        return mainValues;
    }


    protected Document buildDocument(
        River           river,
        Gauge           gauge,
        List<MainValue> mainValues,
        Object          context)
    {
        log.debug("MainValuesService.buildDocument");

        Document doc = XMLUtils.newDocument();

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element rootEl = cr.create("service");
        cr.addAttr(rootEl, "name", "mainvalues");

        doc.appendChild(rootEl);

        appendMetaInformation(doc, rootEl, river, gauge, context);
        appendMainValues(doc, rootEl, mainValues, river.getId(), context);

        return doc;
    }


    /**
     * This method appends some meta information to the result document.
     * Currently, the river's and gauge's names and the gauge's range are
     * appended.
     *
     * @param root The root element of the result document.
     * @param river The river.
     * @param gauge The gauge.
     * @param context The context object.
     */
    protected void appendMetaInformation(
        Document doc,
        Element  root,
        River    river,
        Gauge    gauge,
        Object   context)
    {
        log.debug("MainValuesService.appendMetaInformation");

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Range range = gauge.getRange();

        Element riverEl = cr.create("river");
        cr.addAttr(riverEl, "name", river.getName());

        Element gaugeEl = cr.create("gauge");
        cr.addAttr(gaugeEl, "name", gauge.getName());
        cr.addAttr(gaugeEl, "from", range.getA().toString());
        cr.addAttr(gaugeEl, "to", range.getB().toString());

        root.appendChild(riverEl);
        root.appendChild(gaugeEl);
    }


    /** Checks i a main value has an official associated, */
    protected static boolean hasOfficialLine(
        NamedMainValue nmv,
        Integer riverId
    ) {
        for (OfficialLine ol: nmv.getOfficialLines()) {
            if (
                ol.getWstColumn().getWst().getRiver().getId().equals(riverId)
            ) {
                return true;
            }
        }
        return false;
    }


    /** Append xml representation of main values to document. */
    protected void appendMainValues(
        Document        doc,
        Element         root,
        List<MainValue> mainValues,
        Integer         riverId,
        Object          context)
    {
        log.debug("MainValuesService.appendMainValues");

        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element list = cr.create("mainvalues");

        for (MainValue mainValue: mainValues) {
            Element newEl = buildMainValueElement(
                doc, mainValue, riverId, context);

            if (newEl != null) {
                list.appendChild(newEl);
            }
        }

        root.appendChild(list);
    }


    /**
     * This method builds a concrete mainvalue element. This element consists of
     * three attributes: the value, its name and its type.
     *
     * @param doc The owner document.
     * @param mainValue The mainvalue.
     * @param context The context object.
     *
     * @return a mainvalue element.
     */
    protected Element buildMainValueElement(
        Document  doc,
        MainValue mainValue,
        Integer   riverId,
        Object    context)
    {
        ElementCreator cr = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        NamedMainValue namedMainValue = mainValue.getMainValue();
        MainValueType  mainValueType  = namedMainValue.getType();

        Element el = cr.create("mainvalue");

        cr.addAttr(el, "value", mainValue.getValue().toString());
        cr.addAttr(el, "name", namedMainValue.getName());
        cr.addAttr(el, "type", mainValueType.getName());
        if (mainValue.getTimeInterval() != null) {
            if (mainValue.getTimeInterval().getStartTime() != null) {
                cr.addAttr(el, "starttime",
                    Long.toString(
                        mainValue.getTimeInterval().getStartTime().getTime()));
            }
            if (mainValue.getTimeInterval().getStopTime() != null) {
                cr.addAttr(el, "stoptime",
                    Long.toString(
                        mainValue.getTimeInterval().getStopTime().getTime()));
            }
        }

        if (hasOfficialLine(namedMainValue, riverId)) {
            cr.addAttr(el, "official", "true");
        }

        return el;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
