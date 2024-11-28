/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.dive4elements.artifactdatabase.DefaultService;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.Service;

/** Serve chart. */
public abstract class AbstractChartService extends DefaultService {

    public static final int DEFAULT_WIDTH = 240;
    public static final int DEFAULT_HEIGHT = 180;

    public static final String DEFAULT_FORMAT = "png";

    private static final Logger log = LogManager
        .getLogger(AbstractChartService.class);

    // TODO: Load fancy image from resources.
    public static final byte[] EMPTY = { (byte) 0x89, (byte) 0x50, (byte) 0x4e,
        (byte) 0x47, (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0d, (byte) 0x49,
        (byte) 0x48, (byte) 0x44, (byte) 0x52, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x3a, (byte) 0x7e, (byte) 0x9b, (byte) 0x55,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x73,
        (byte) 0x52, (byte) 0x47, (byte) 0x42, (byte) 0x00, (byte) 0xae,
        (byte) 0xce, (byte) 0x1c, (byte) 0xe9, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x09, (byte) 0x70, (byte) 0x48, (byte) 0x59,
        (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x0b, (byte) 0x13,
        (byte) 0x00, (byte) 0x00, (byte) 0x0b, (byte) 0x13, (byte) 0x01,
        (byte) 0x00, (byte) 0x9a, (byte) 0x9c, (byte) 0x18, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x74, (byte) 0x49,
        (byte) 0x4d, (byte) 0x45, (byte) 0x07, (byte) 0xdc, (byte) 0x04,
        (byte) 0x04, (byte) 0x10, (byte) 0x30, (byte) 0x15, (byte) 0x7d,
        (byte) 0x77, (byte) 0x36, (byte) 0x0b, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x08, (byte) 0x74, (byte) 0x45, (byte) 0x58,
        (byte) 0x74, (byte) 0x43, (byte) 0x6f, (byte) 0x6d, (byte) 0x6d,
        (byte) 0x65, (byte) 0x6e, (byte) 0x74, (byte) 0x00, (byte) 0xf6,
        (byte) 0xcc, (byte) 0x96, (byte) 0xbf, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x0a, (byte) 0x49, (byte) 0x44, (byte) 0x41,
        (byte) 0x54, (byte) 0x08, (byte) 0xd7, (byte) 0x63, (byte) 0xf8,
        (byte) 0x0f, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01,
        (byte) 0x00, (byte) 0x1b, (byte) 0xb6, (byte) 0xee, (byte) 0x56,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49,
        (byte) 0x45, (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42,
        (byte) 0x60, (byte) 0x82 };

    private static final Output empty() {
        return new Output(EMPTY, "image/png");
    }

    protected abstract JFreeChart createChart(Document data,
        GlobalContext globalContext, CallMeta callMeta);

    protected abstract void init();

    protected abstract void finish();

    @Override
    public Service.Output process(Document data, GlobalContext globalContext,
        CallMeta callMeta) {
        log.debug("process");

        init();
        try {
            JFreeChart chart = createChart(data, globalContext, callMeta);

            if (chart == null) {
                return empty();
            }

            Dimension extent = getExtent(data);
            String format = getFormat(data);

            return encode(chart, extent, format);
        }
        finally {
            finish();
        }
    }

    protected static Output encode(JFreeChart chart, Dimension extent,
        String format) {
        BufferedImage image = chart.createBufferedImage(extent.width,
            extent.height, Transparency.BITMASK, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, format, out);
        }
        catch (IOException ioe) {
            log.warn("writing image failed", ioe);
            return empty();
        }

        return new Output(out.toByteArray(), "image/" + format);
    }

    protected static Dimension getExtent(Document input) {

        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        NodeList extents = input.getElementsByTagName("extent");

        if (extents.getLength() > 0) {
            Element element = (Element) extents.item(0);
            String w = element.getAttribute("width");
            String h = element.getAttribute("height");

            try {
                width = Math.max(1, Integer.parseInt(w));
            }
            catch (NumberFormatException nfe) {
                log.warn("width '" + w + "' is not a valid.");
            }

            try {
                height = Math.max(1, Integer.parseInt(h));
            }
            catch (NumberFormatException nfe) {
                log.warn("height '" + h + "' is not a valid");
            }
        }

        return new Dimension(width, height);
    }

    protected static String getFormat(Document input) {
        String format = DEFAULT_FORMAT;

        NodeList formats = input.getElementsByTagName("format");

        if (formats.getLength() > 0) {
            String type = ((Element) formats.item(0)).getAttribute("type");
            if (type.length() > 0) {
                format = type;
            }
        }

        return format;
    }
}
