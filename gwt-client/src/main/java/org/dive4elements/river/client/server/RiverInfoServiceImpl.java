/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.client.services.RiverInfoService;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.DefaultGaugeInfo;
import org.dive4elements.river.client.shared.model.DefaultMeasurementStation;
import org.dive4elements.river.client.shared.model.DefaultRiverInfo;
import org.dive4elements.river.client.shared.model.GaugeInfo;
import org.dive4elements.river.client.shared.model.MeasurementStation;
import org.dive4elements.river.client.shared.model.RiverInfo;


/**
 * GWT Service to serve the gauge and measurement station info
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class RiverInfoServiceImpl
extends      RemoteServiceServlet
implements   RiverInfoService
{
    private static final Logger log =
        LogManager.getLogger(RiverInfoServiceImpl.class);

    public static final String ERROR_NO_RIVER_INFO_FOUND =
        "error_no_riverinfo_found";

    private static final String XPATH_RIVER =
        "/art:river-info/art:river";

    private static final String XPATH_STATIONS =
        "/art:river-info/art:measurement-stations/art:measurement-station";

    private static final String XPATH_GAUGES =
        "/art:river-info/art:gauges/art:gauge";

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(
        DateFormat.SHORT, Locale.GERMANY);

    public RiverInfo getGauges(String river) throws ServerException {
        log.info("RiverInfoServiceImpl.getRiverInfo");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element riverele = ec.create("river");
        riverele.setTextContent(river);

        doc.appendChild(riverele);

        HttpClient client = new HttpClientImpl(url);

        try {
            Document result = client.callService(url, "gaugeoverviewinfo", doc);

            DefaultRiverInfo riverinfo = getRiverInfo(result);
            List<GaugeInfo>gauges = createGauges(result, riverinfo.getName(),
                    riverinfo.isKmUp(), riverinfo.getWstUnit());


            riverinfo.setGauges(gauges);

            log.debug("Finished RiverInfoService.getGauges.");

            return riverinfo;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        log.warn("No gauge found");
        throw new ServerException(ERROR_NO_RIVER_INFO_FOUND);
    }

    public RiverInfo getMeasurementStations(String river)
        throws ServerException {
        log.info("RiverInfoServiceImpl.getMeasurementStations");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element riverele = ec.create("river");
        riverele.setTextContent(river);

        doc.appendChild(riverele);

        HttpClient client = new HttpClientImpl(url);

        try {
            Document result = client.callService(
                url, "measurementstationinfo", doc);

            DefaultRiverInfo riverinfo = getRiverInfo(result);
            List<MeasurementStation> mstations = createMeasurementStations(
                result, riverinfo.getName());

            riverinfo.setMeasurementStations(mstations);

            log.debug("Finished MeasurementStationInfoService.");

            return riverinfo;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        log.warn("No measurement station found");
        throw new ServerException(ERROR_NO_RIVER_INFO_FOUND);
    }

    /**
     * Avoids NullPointerException when parsing double value
     */
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        }
        catch(NumberFormatException e) {
            log.error(e, e);
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        }
        catch(NumberFormatException e) {
            log.error(e, e);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException e) {
            log.error(e, e);
            return null;
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(value);
        }
        catch(ParseException e) {
            log.error(e, e);
            return null;
        }
    }

    private List<MeasurementStation> createMeasurementStations(
        Document result, String rivername) {

        NodeList stationnodes = (NodeList) XMLUtils.xpath(
                result,
                XPATH_STATIONS,
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

        int num = stationnodes == null ? 0 : stationnodes.getLength();

        ArrayList<MeasurementStation> mstations =
            new ArrayList<MeasurementStation>(num);

        if (num == 0) {
            log.warn("No measurement station found.");
        }
        else {
            log.debug("Found " + num + " measurement stations.");

            for (int i = 0; i < num; i++) {
                Element stationele = (Element)stationnodes.item(i);

                String mname = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "name");
                String mstart = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "start");
                String mend = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "end");
                String mtype = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "type");
                String riverside = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "riverside");
                String mid = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "id");
                String moperator = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "operator");
                String mstarttime = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "starttime");
                String mstoptime = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "stoptime");
                String mcomment = stationele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "comment");

                String gaugename = null;

                Element gaugeele = (Element)stationele.getFirstChild();
                if (gaugeele != null) {
                    gaugename = gaugeele.getAttributeNS(
                            ArtifactNamespaceContext.NAMESPACE_URI, "name");
                }


                log.debug("Found measurement station with name " + mname);

                MeasurementStation station = new DefaultMeasurementStation(
                        rivername,
                        mname,
                        parseInteger(mid),
                        parseDouble(mstart),
                        parseDouble(mend),
                        riverside,
                        mtype,
                        moperator,
                        parseDate(mstarttime),
                        parseDate(mstoptime),
                        gaugename,
                        mcomment
                        );

                mstations.add(station);
            }
        }
        return mstations;
    }

    private List<GaugeInfo> createGauges(
            Document result, String rivername, Boolean kmup, String rwstunit) {
        NodeList gaugenodes = (NodeList) XMLUtils.xpath(
                result,
                XPATH_GAUGES,
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

        int num = gaugenodes == null ? 0 : gaugenodes.getLength();

        ArrayList<GaugeInfo> gauges = new ArrayList<GaugeInfo>(num);

        if (num == 0) {
            log.warn("No gauge info found.");
        }
        else {
            log.debug("Found " + num + " gauges.");

            for (int i = 0; i < num; i++) {
                Element gaugeele = (Element)gaugenodes.item(i);

                String gname = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "name");
                String gstart = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "start");
                String gend = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "end");
                String gdatum = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "datum");
                String gaeo = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "aeo");
                String gminq = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "minq");
                String gminw = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "minw");
                String gmaxq = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "maxq");
                String gmaxw = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "maxw");
                String gstation = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "station");
                String gofficial = gaugeele.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "official");

                log.debug("Found gauge with name " + gname);

                GaugeInfo gaugeinfo = new DefaultGaugeInfo(
                        rivername,
                        gname,
                        kmup,
                        parseDouble(gstation),
                        parseDouble(gstart),
                        parseDouble(gend),
                        parseDouble(gdatum),
                        parseDouble(gaeo),
                        parseDouble(gminq),
                        parseDouble(gmaxq),
                        parseDouble(gminw),
                        parseDouble(gmaxw),
                        rwstunit,
                        parseLong(gofficial)
                        );

                gauges.add(gaugeinfo);
            }
        }
        return gauges;
    }

    private DefaultRiverInfo getRiverInfo(Document result) {
        Element riverresp = (Element) XMLUtils.xpath(
                result,
                XPATH_RIVER,
                XPathConstants.NODE,
                ArtifactNamespaceContext.INSTANCE);

        String rname = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "name");
        String rkmup = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "kmup");
        String rstart = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "start");
        String rend = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "end");
        String rwstunit = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "wstunit");
        String rminq = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "minq");
        String rmaxq = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "maxq");
        String rofficial = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "official");
        String rmuuid = riverresp.getAttributeNS(
                ArtifactNamespaceContext.NAMESPACE_URI, "model-uuid");

        log.debug("River is " + rname);

        boolean kmup = rkmup.equalsIgnoreCase("true");
        DefaultRiverInfo riverinfo = new DefaultRiverInfo(
                rname,
                kmup,
                parseDouble(rstart),
                parseDouble(rend),
                rwstunit,
                parseDouble(rminq),
                parseDouble(rmaxq),
                parseLong(rofficial),
                rmuuid
                );

        return riverinfo;
    }
}
