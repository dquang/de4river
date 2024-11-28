/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.JSON;
import org.dive4elements.artifacts.common.utils.StringUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;
import org.dive4elements.river.client.shared.MapUtils;
import org.dive4elements.river.client.shared.model.MapConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/*
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
*/
/* Used by direct API call. -> Enforce GPLv3
import org.mapfish.print.MapPrinter;
import org.mapfish.print.output.OutputFactory;
import org.mapfish.print.output.OutputFormat;

import org.mapfish.print.utils.PJsonObject;
*/

public class MapPrintServiceImpl
extends      HttpServlet
{
    private static final String MAPFISH_DATA_PREFIX = "mapfish_data_";

    private static final Logger log =
        LogManager.getLogger(MapPrintServiceImpl.class);

    protected static class Layer implements Comparable<Layer> {

        protected int    pos;
        protected String url;
        protected String layers;
        protected String description;

        public Layer() {
        }

        public boolean setup(Element element, HttpServletRequest req) {

            Element parent = (Element)element.getParentNode();
            String parentName = parent.getAttribute("name");
            if (!(parentName.equals("map")
            ||    parentName.equals("floodmap"))) {
                return false;
            }

            String ns = ArtifactNamespaceContext.NAMESPACE_URI;

            String visible = element.getAttributeNS(ns, "visible");
            String active  = element.getAttributeNS(ns, "active");

            if (visible.equals("0") || active.equals("0")) {
                return false;
            }

            url = element.getAttributeNS(ns, "url");
            try {
                // if given URL is not absolute, complement it
                url = new URL(new URL(req.getRequestURL().toString()),
                    url).toString();
            } catch (MalformedURLException mue) {
                log.error("Failed to complement layer URL: "
                    + mue.getMessage());
            }
            layers      = element.getAttributeNS(ns, "layers");
            description = element.getAttributeNS(ns, "description");

            try {
                pos = Integer.parseInt(element.getAttributeNS(ns, "pos"));
            }
            catch (NumberFormatException nfe) {
                return false;
            }

            return true;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> layer = new LinkedHashMap<String, Object>();

            layer.put("type", "WMS");
            List<Object> subLayers = new ArrayList<Object>(1);
            subLayers.add(layers);
            layer.put("layers", subLayers);
            // XXX: osm.intevation.de mapache only offers low dpi maps
            // so we need to use the uncached service
            layer.put("baseURL", url.replace(
                    "http://osm.intevation.de/mapcache/?",
                    "http://osm.intevation.de/cgi-bin/germany.fcgi?"));
            layer.put("format", "image/png"); // TODO: Make configurable.

            return layer;
        }

        @Override
        public int compareTo(Layer other) {
            int d = pos - other.pos;
            if (d < 0) return -1;
            return d > 0 ? +1 : 0;
        }
    } // class Layer

    protected static String generateSpec(
        HttpServletRequest req,
        Document descDocument,
        MapConfig mapConfig,
        Double minX, Double minY,
        Double maxX, Double maxY,
        Map<String, Object> pageSpecs
    ) {
        Map<String, Object> spec = new LinkedHashMap<String, Object>();
        int dpi = 254;
        spec.put("layout",       "A4 landscape");
        spec.put("pageSize",     "A4");
        spec.put("landscape",    "true");
        spec.put("srs",          "EPSG:" + mapConfig.getSrid());
        spec.put("dpi",          dpi);
        spec.put("units",        "m");
        spec.put("geodaetic",    "true");
        spec.put("outputFormat", "pdf");

        spec.putAll(pageSpecs);

        String ns = ArtifactNamespaceContext.NAMESPACE_URI;

        List<Layer> ls = new ArrayList<Layer>();
        Layer l = new Layer();

        NodeList facets = descDocument.getElementsByTagNameNS(ns, "facet");

        for (int i = 0, N = facets.getLength(); i < N; ++i) {
            Element element = (Element)facets.item(i);
            if (l.setup(element, req)) {
                ls.add(l);
                l = new Layer();
            }
        }

        // Establish Z order.
        Collections.sort(ls);

        List<Object> layers = new ArrayList<Object>(ls.size());

        for (int i = ls.size()-1; i >= 0; --i) {
            layers.add(ls.get(i).toMap());
        }

        spec.put("layers", layers);
        spec.put("name", "Name");

        List<Object> pages = new ArrayList<Object>(1);


        Map<String, Object> page = new LinkedHashMap<String, Object>();

        List<Object> bounds = new ArrayList<Object>(4);
        bounds.add(minX);
        bounds.add(minY);
        bounds.add(maxX);
        bounds.add(maxY);
        page.put("bbox", bounds);

        /*
        bounds.add(Double.valueOf((minX+maxX)*0.5));
        bounds.add(Double.valueOf((minY+maxY)*0.5));

        page.put("center", bounds);
        page.put("scale", Integer.valueOf(50000));
        */

        page.put("rotation", Integer.valueOf(0));

        // This may overwrite default settings above
        page.putAll(pageSpecs);

        pages.add(page);
        spec.put("pages", pages);

        List<Object> legends = new ArrayList<Object>(layers.size());

        for (Layer layer: ls) {
            Map<String, Object> legend = new LinkedHashMap<String, Object>();
            List<Object> classes = new ArrayList<Object>(1);
            Map<String, Object> clazz = new LinkedHashMap<String, Object>();
            String lgu = MapUtils.getLegendGraphicUrl(
                layer.url, encode(layer.layers), dpi);
            clazz.put("icon", lgu);
            clazz.put("name", layer.description);
            classes.add(clazz);
            legend.put("classes", classes);
            legend.put("name", layer.description);
            legends.add(legend);
        }

        spec.put("legends", legends);

        return JSON.toJSONString(spec);
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws  ServletException, IOException
    {
        log.info("MapPrintServiceImpl.doGet");

        String uuid = req.getParameter("uuid");

        if (uuid == null || !StringUtils.checkUUID(uuid)) {
            throw new ServletException("Missing or misspelled UUID");
        }

        String minXS = req.getParameter("minx");
        String maxXS = req.getParameter("maxx");
        String minYS = req.getParameter("miny");
        String maxYS = req.getParameter("maxy");

        Double minX = null;
        Double maxX = null;
        Double minY = null;
        Double maxY = null;

        if (minXS != null && maxXS != null
        &&  minYS != null && maxYS != null) {
            log.debug("all parameters found -> parsing");
            try {
                minX = Double.parseDouble(minXS);
                minY = Double.parseDouble(minYS);
                maxX = Double.parseDouble(maxXS);
                maxY = Double.parseDouble(maxYS);
            }
            catch (NumberFormatException nfe) {
                throw new ServletException(
                    "Misspelled minX, minY, maxX or maxY");
            }
        }

        String mapType = req.getParameter("maptype");

        if (mapType == null || !mapType.equals("floodmap")) {
            mapType = "map";
        }

        // Retrieve print settings from request
        Map<String, Object> pageSpecs = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List<Object> payload = new ArrayList<Object>();
        data.put("data", payload);
        Enumeration paramNames = req.getParameterNames();
        List<String> params = Collections.list(paramNames);
        Collections.sort(params);
        final int prefixLength = MAPFISH_DATA_PREFIX.length();
        for (String paramName : params) {
            if (paramName.startsWith(MAPFISH_DATA_PREFIX)) {
                // You can add mapfish_data variables that will be mapped
                // to "info"/"value" pairs to provide meta data for the map.
                // If the "info" part starts with a number for sorting, that
                // number will be stripped
                Map<String, Object> data3 = new HashMap<String, Object>();
                try {
                    Integer.parseInt(paramName.substring(
                            prefixLength, prefixLength + 1));
                    data3.put("info", paramName.substring(prefixLength + 1));
                } catch (NumberFormatException nfe) {
                    data3.put("info", paramName.substring(prefixLength));
                    payload.add(data3);
                }

                String paramValue = req.getParameter(paramName);
                data3.put("value", paramValue.equals("null") ? "" : paramValue);
                payload.add(data3);
            } else if (paramName.startsWith("mapfish_")) {
                String paramValue = req.getParameter(paramName);
                if (paramValue.equals("null"))
                    paramValue = "";
                pageSpecs.put(paramName.substring(8), paramValue);
            }
        }
        if (!payload.isEmpty()) {
            pageSpecs.put("data", data);
            List<Object> columns = new ArrayList<Object>();
            columns.add("info");
            columns.add("value");
            data.put("columns", columns);
        }

        String url = getURL();

        Document requestOut =
            ClientProtocolUtils.newOutCollectionDocument(
                uuid, mapType, mapType);
        Document requestDesc =
            ClientProtocolUtils.newDescribeCollectionDocument(uuid);

        Document outDocument;
        Document descDocument;

        try {
            HttpClient client = new HttpClientImpl(url);

            descDocument = (Document)client.doCollectionAction(
                requestDesc, uuid, new DocumentResponseHandler());

            InputStream is = client.collectionOut(
                requestOut, uuid, mapType);

            try {
                outDocument = XMLUtils.parseDocument(is);
            }
            finally {
                is.close();
                is = null;
            }

        }
        catch (ConnectionException ce) {
            log.error(ce);
            throw new ServletException(ce);
        }

        MapConfig mapConfig = MapHelper.parseConfig(outDocument);

        if (minX == null) {
            log.debug("parameters missing -> fallback to max extent");
            String [] parts = mapConfig.getMaxExtent().split("\\s+");
            if (parts.length < 4) {
                throw new ServletException(
                    "Max extent has less than 4 values");
            }
            try {
                minX = Double.valueOf(parts[0]);
                minY = Double.valueOf(parts[1]);
                maxX = Double.valueOf(parts[2]);
                maxY = Double.valueOf(parts[3]);
            }
            catch (NumberFormatException nfe) {
                throw new ServletException(nfe);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("minX: " + minX);
            log.debug("maxX: " + maxX);
            log.debug("minY: " + minY);
            log.debug("maxY: " + maxY);
        }

        String spec = generateSpec(
            req,
            descDocument,
            mapConfig,
            minX, minY,
            maxX, maxY,
            pageSpecs);

        if (log.isDebugEnabled()) {
            log.debug("Generated spec:");
            log.debug(spec);
            //System.err.println(spec);
        }

        producePDF(spec, resp);
    }

    protected String getURL() throws ServletException {
        String url = getServletContext().getInitParameter("server-url");
        if (url == null) {
            throw new ServletException("Missing server-url");
        }
        return url;
    }

    private static final String encode(String s) {
        try {
            if (s == null) return null;
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException usee) {
            // Should not happen.
            log.error(usee.getMessage());
            return s;
        }
    }

    protected void producePDF(String json, HttpServletResponse resp)
    throws ServletException, IOException
    {
        String printUrl = getInitParameter("print-url");

        if (printUrl == null) {
            throw new ServletException("Missing 'print-url' in web.xml");
        }

        String url = printUrl + "/print.pdf?spec=" + encode(json);

        org.apache.commons.httpclient.HttpClient client =
            new org.apache.commons.httpclient.HttpClient(
                new MultiThreadedHttpConnectionManager());

        // FIXME: The request is not authenticated.
        //        Currently this is not a problem because /flys/map-print
        //        is whitelisted in GGInAFilter.
        GetMethod get = new GetMethod(url);
        get.addRequestHeader("X_NO_GGINA_AUTH", "TRUE");
        int result = client.executeMethod(get);
        InputStream in = get.getResponseBodyAsStream();

        if (in != null) {
            try {
                OutputStream out = resp.getOutputStream();
                try {
                    byte [] buf = new byte[4096];
                    int r;
                    if (result < 200 || result >= 300) {
                        resp.setContentType("text/plain");
                    } else {
                        // Only send content disposition and filename content
                        // type when we have a pdf
                        resp.setHeader("Content-Disposition",
                                "attachment;filename=flys-karte.pdf");
                        resp.setContentType("application/pdf");
                    }
                    while ((r = in.read(buf)) >= 0) {
                        out.write(buf, 0, r);
                    }
                    out.flush();
                }
                finally {
                    out.close();
                }
            }
            finally {
                in.close();
            }
        }
    }

    /* Use this if you want directly call the MapPrinter. Enforces GPLv3!

    protected MapPrinter getMapPrinter() throws ServletException, IOException {
        String configPath = getInitParameter("config");
        if (configPath == null) {
            throw new ServletException("Missing configuration in web.xml");
        }

        File configFile = new File(configPath);
        if (!configFile.isAbsolute()) {
            configFile = new File(getServletContext().getRealPath(configPath));
        }

        if (!configFile.isFile() || !configFile.canRead()) {
            throw new ServletException("Cannot read '" + configFile + "'");
        }

        return new MapPrinter(configFile);
    }

    protected void producePDF(String json, HttpServletResponse resp)
    throws ServletException, IOException
    {
        PJsonObject jsonSpec = MapPrinter.parseSpec(json);

        MapPrinter printer = getMapPrinter();

        OutputFormat outputFormat = OutputFactory.create(
            printer.getConfig(), jsonSpec);

        resp.setHeader("Content-Disposition", "attachment;filename=print.pdf");
        resp.setHeader("Content-Type", "application/pdf");

        // XXX: Streaming the generated PDF directly
        // to the request out does not work. :-/
        File tmpFile = File.createTempFile("map-printing", null);

        try {
            OutputStream out =
                new BufferedOutputStream(
                new FileOutputStream(tmpFile));
            try {
                outputFormat.print(printer, jsonSpec, out, "");
                out.flush();
            }
            catch (Exception e) {
                log.error(e);
                throw new ServletException(e);
            }
            finally {
                printer.stop();
                out.close();
            }
            InputStream in =
                new BufferedInputStream(
                new FileInputStream(tmpFile));
            out = resp.getOutputStream();
            try {
                byte [] buf = new byte[4096];
                int r;
                while ((r = in.read(buf)) >= 0) {
                    out.write(buf, 0, r);
                }
                out.flush();
            }
            finally {
                in.close();
                out.close();
            }
        }
        finally {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }
    */
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
