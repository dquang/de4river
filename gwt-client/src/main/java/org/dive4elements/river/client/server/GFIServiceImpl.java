/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.client.services.GFIService;

import org.dive4elements.river.client.shared.exceptions.ServerException;

import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.FeatureInfo;
import org.dive4elements.river.client.shared.model.FeatureInfoResponse;
import org.dive4elements.river.client.shared.model.Theme;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GFIServiceImpl
extends      RemoteServiceServlet
implements   GFIService
{
    public static final String ERR_NO_VALID_GFI_URL =
        "error_no_valid_gfi_url";

    public static final String ERR_GFI_REQUEST_FAILED =
        "error_gfi_req_failed";

    public static final String ERR_PARSING_RESPONSE_FAILED =
        "error_gfi_parsing_failed";


    private static final Logger log =
        LogManager.getLogger(GFIServiceImpl.class);


    /**
     * @param theme
     * @param format
     * @param bbox
     * @param height
     * @param width
     * @param x
     * @param y
     *
     * @return
     */
    public FeatureInfoResponse query(
        Theme       theme,
        String      format,
        String      bbox,
        String      projection,
        int         height,
        int         width,
        int         x,
        int         y
    ) throws ServerException
    {
        log.info("GFIServiceImpl.query");

        String path = createGetFeautureInfoURL(
            theme, format, bbox, projection, height, width, x, y);

        log.debug("URL=" + path);

        try {
            URL url = new URL(path);

            URLConnection conn = url.openConnection();
            conn.connect();

            InputStream is = conn.getInputStream();

            return parseResponse(is);

        }
        catch (IOException ioe) {
            log.warn(ioe, ioe);
        }

        throw new ServerException(ERR_GFI_REQUEST_FAILED);
    }


    /**
     * @param map
     * @param theme
     * @param format
     * @param x
     * @param y
     *
     * @return
     */
    protected String createGetFeautureInfoURL(
        Theme       theme,
        String      infoFormat,
        String      bbox,
        String      projection,
        int         height,
        int         width,
        int         x,
        int         y
    ) throws ServerException
    {
        String url = getUrl(theme);

        if (url == null || url.length() == 0) {
            throw new ServerException(ERR_NO_VALID_GFI_URL);
        }

        String layers = ((AttributedTheme)theme).getAttr("layers");

        StringBuilder sb = new StringBuilder();
        sb.append(url);

        if (url.indexOf("?") < 0) {
            sb.append("?SERVICE=WMS");
        }
        else {
            sb.append("&SERVICE=WMS");
        }

        sb.append("&VERSION=1.1.1");
        sb.append("&REQUEST=GetFeatureInfo");
        sb.append("&LAYERS=" + layers);
        sb.append("&QUERY_LAYERS=" + layers);
        sb.append("&BBOX=" + bbox);
        sb.append("&HEIGHT=" + height);
        sb.append("&WIDTH=" + width);
        sb.append("&FORMAT=image/png");
        sb.append("&INFO_FORMAT=" + infoFormat);
        sb.append("&SRS=" + projection);
        sb.append("&X=" + String.valueOf(x));
        sb.append("&Y=" + String.valueOf(y));

        return sb.toString();
    }


    protected String getUrl(Theme theme) {
        AttributedTheme attr = (AttributedTheme) theme;
        return attr.getAttr("url");
    }


    protected FeatureInfoResponse parseResponse(InputStream is) {
        log.debug("GFIServiceImpl.parseResponse");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte [] buf = new byte[1024];

        int r = -1;
        try {
            while ((r = is.read(buf)) >= 0) {
                baos.write(buf, 0, r);
            }
        } catch (IOException ex) {
            log.warn("GetFeatureInfo response could not be read: ", ex);
            return new FeatureInfoResponse();
        }

        String content;
        try {
            content = baos.toString("UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            content = baos.toString();
        }

        Document response = XMLUtils.parseDocument(content);
        if (response != null) {
            List<FeatureInfo> features = new ArrayList<FeatureInfo>();
            parseFeatureInfos(response, features);
            return new FeatureInfoResponse(features, null);
        }
        // Unable to parse just return the response as is
        return new FeatureInfoResponse(new ArrayList<FeatureInfo>(), content);
    }


    protected void parseFeatureInfos(Node node, List<FeatureInfo> features) {
        log.debug("GFIServiceImpl.parseFeatureInfos");

        String name = node.getNodeName();

        if (name.endsWith("_layer")) {
            features.add(parseFeature(node));

            return;
        }

        NodeList children = node.getChildNodes();

        if (children != null && children.getLength() > 0) {
            for (int i = 0, n = children.getLength(); i < n; i++) {
                parseFeatureInfos(children.item(i), features);
            }
        }
    }


    protected FeatureInfo parseFeature(Node node) {
        log.debug("GFIServiceImpl.parseFeature");

        String layername = node.getNodeName();

        FeatureInfo f = new FeatureInfo(layername);

        NodeList children = node.getChildNodes();
        int numChildren   = children != null ? children.getLength() : 0;

        log.debug("Feature '" + layername + "' has " + numChildren + " nodes.");

        for (int i = 0; i < numChildren; i++) {
            Node  tmp       = children.item(i);
            String nodeName = tmp.getNodeName();

            log.debug("   node name: '" + nodeName + "'");

            if (nodeName.equals("gml:name")) {
                log.debug("NAME node has child: "
                    + tmp.getFirstChild().getNodeValue());
                f.setLayername(tmp.getFirstChild().getNodeValue());
            }
            else if (nodeName.endsWith("_feature")) {
                parseFeatureAttributes(tmp, f);
            }
        }

        return f;
    }


    protected void parseFeatureAttributes(Node node, FeatureInfo f) {
        log.debug("GFIServiceImpl.parseFeatureAttributes");

        NodeList children = node.getChildNodes();
        int numChildren   = children != null ? children.getLength() : 0;

        log.debug("Has " + numChildren + " attributes.");

        for (int i = 0; i < numChildren; i++) {
            Node   tmp  = children.item(i);
            String name = tmp.getNodeName();

            log.debug("  tmp attribute name: '" + name + "'");

            if (name.equals("gml:boundedBy")) {
                // TODO
            }
            else {
                Node child = tmp.getFirstChild();
                if (child != null) {
                    f.addAttr(name, child.getNodeValue());
                }
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
