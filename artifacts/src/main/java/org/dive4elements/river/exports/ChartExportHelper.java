/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Graphics2D;
import java.awt.Transparency;

import java.awt.geom.Rectangle2D;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.text.NumberFormat;
import java.util.Map;

import org.jfree.chart.ChartRenderingInfo;

import javax.imageio.ImageIO;

import au.com.bytecode.opencsv.CSVWriter;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.jfree.XYMetaSeriesCollection;
import org.dive4elements.river.utils.Formatter;


/**
 * This class is a helper class which supports some methods to export charts
 * into specific formats.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartExportHelper {

    public static final String FORMAT_PNG = "png";

    public static final String FORMAT_PDF = "pdf";

    public static final String FORMAT_SVG = "svg";

    public static final String FORMAT_CSV = "csv";

    /**
     * Constant field to define A4 as default page size.
     */
    public static final String  DEFAULT_PAGE_SIZE = "A4";

    /**
     * Constant field to define UTF-8 as default encoding.
     */
    public static final String  DEFAULT_ENCODING  = "UTF-8";

    /** The default separator for the CSV export. */
    public static final char DEFAULT_CSV_SEPARATOR = ';';


    /**
     * Logger used for logging with log4j.
     */
    private static Logger log = LogManager.getLogger(ChartExportHelper.class);


    /**
     * A method to export a <code>JFreeChart</code> as image to an
     * <code>OutputStream</code> with a given format, width and height.
     *
     * @param out OutputStream
     * @param chart JFreeChart object to be exported.
     * @param cc context, in which e.g. size is stored.
     *
     * @throws IOException if writing image to OutputStream failed.
     */
    public static void exportImage(
        OutputStream out,
        JFreeChart   chart,
        CallContext  cc
    )
    throws IOException
    {
        log.info("export chart as png");

        ChartRenderingInfo info = new ChartRenderingInfo();

        String format = (String) cc.getContextValue("chart.image.format");

        int[] size = getSize(cc);

        ImageIO.write(
            chart.createBufferedImage(
                size[0], size[1], Transparency.BITMASK, info
            ),
            format,
            out
        );
    }


    /**
     * A method to export a <code>JFreeChart</code> as SVG to an
     * <code>OutputStream</code>.
     *
     * @param out OutputStream
     * @param chart JFreeChart to be exported
     * @param context The CallContext object that contains extra chart
     * parameters.
     */
    public static void exportSVG(
        OutputStream out,
        JFreeChart   chart,
        CallContext  context
    ) {
        String encoding = (String) context.getContextValue("chart.encoding");

        log.info("export chart as svg");

        if (encoding == null)
            encoding = DEFAULT_ENCODING;

        org.w3c.dom.Document document = XMLUtils.newDocument();
        SVGGraphics2D        graphics = new SVGGraphics2D(document);

        int[] size = getSize(context);

        ChartRenderingInfo info = new ChartRenderingInfo();

        chart.draw(
            graphics,
            new Rectangle2D.Double(0.0D, 0.0D,size[0],size[1]),
            info);

        try {
            graphics.stream(new OutputStreamWriter(out, encoding));
        }
        catch (SVGGraphics2DIOException svge) {
            log.error(
                "Error while writing svg export to output stream.", svge);
        }
        catch (UnsupportedEncodingException uee) {
            log.error("Unsupported encoding: " + encoding, uee);
        }
    }


    /**
     * A method to export a <code>JFreeChart</code> as PDF to an
     * <code>OutputStream</code>.
     *
     * @param out OutputStream
     * @param chart JFreeChart
     */
    public static void exportPDF(
        OutputStream out,
        JFreeChart   chart,
        CallContext  cc
    ) {
        log.info("export chart as pdf.");

        String pageFormat = (String) cc.getContextValue("chart.page.format");

        if (pageFormat == null)
            pageFormat = DEFAULT_PAGE_SIZE;

        // Max size of the chart.
        Rectangle page = PageSize.getRectangle(pageFormat);
        float pageWidth  = page.getWidth();
        float pageHeight = page.getHeight();

        // The chart width.
        int[] size = getSize(cc);

        boolean landscape = size[0] > size[1];

        float width  = 0;
        float height = 0;
        if (landscape) {
            width  = pageHeight;
            height = pageWidth;
        }
        else {
            width  = pageWidth;
            height = pageHeight;
        }

        float marginLeft = (Float) cc.getContextValue(
            "chart.marginLeft");

        float marginRight = (Float) cc.getContextValue(
            "chart.marginRight");

        float marginTop = (Float) cc.getContextValue(
            "chart.marginTop");

        float marginBottom = (Float) cc.getContextValue(
            "chart.marginBottom");

        float spaceX = width  - marginLeft - marginRight;
        if (size[0] > spaceX) {
            log.warn(
                "Width of the chart is too big for pdf -> resize it now.");
            double ratio = ((double)spaceX) / size[0];
            size[0]  *= ratio;
            size[1] *= ratio;
            log.debug("Resized chart to " + size[0] + "x" + size[1]);
        }

        float spaceY = height - marginTop  - marginBottom;
        if (size[1] > spaceY) {
            log.warn(
                "Height of the chart is too big for pdf -> resize it now.");
            double ratio = ((double)spaceY) / size[1];
            size[0]  *= ratio;
            size[1] *= ratio;
            log.debug("Resized chart to " + size[0] + "x" + size[1]);
        }

        Document document = null;
        if (landscape) {
            document = new Document(page.rotate());
            log.debug("Create landscape pdf.");
        } else {
            document = new Document(page);
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.addSubject(
                chart.getTitle() != null ? chart.getTitle().getText() : "");
            document.addCreationDate();
            document.open();

            try {
                PdfContentByte content  = writer.getDirectContent();

                PdfTemplate template = content.createTemplate(width, height);
                Graphics2D  graphics = template.createGraphics(width, height);

                double[] origin = getCenteredAnchor(
                    marginLeft, marginRight, marginBottom, marginTop,
                    width, height,
                    size[0], size[1]);

                Rectangle2D area = new Rectangle2D.Double(
                    origin[0], origin[1], size[0], size[1]);

                ChartRenderingInfo info = new ChartRenderingInfo();

                chart.draw(graphics, area, info);
                graphics.dispose();
                content.addTemplate(template, 0f, 0f);
            }
            finally {
                document.close();
            }
        } catch (DocumentException de) {
            log.error("Error while exporting chart to pdf.", de);
        }
    }


    /**
     * A method to export a CSV file to an
     * <code>OutputStream</code>.
     *
     * @param out OutputStream
     * @param chart JFreeChart containing the data.
     * @param context The CallContext object that contains extra parameters.
     */
    public static void exportCSV(
        OutputStream out,
        JFreeChart chart,
        CallContext context)
    {
        log.debug("export chart as CSV");
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(
                new OutputStreamWriter(
                    out,
                    DEFAULT_ENCODING),
                DEFAULT_CSV_SEPARATOR, '"', '\\', "\r\n");
        }
        catch(UnsupportedEncodingException uee) {
            log.warn("Wrong encoding for CSV export.");
            return;
        }

        NumberFormat format = Formatter.getCSVFormatter(context);

        XYPlot plot = chart.getXYPlot();
        int count = plot.getDatasetCount();
        for (int i = 0; i < count; i++) {
            XYDataset data = plot.getDataset(i);
            int scount = data.getSeriesCount();
            for (int j = 0; j < scount; j++) {
                Comparable seriesKey = data.getSeriesKey(j);
                log.debug("series key: " + seriesKey.toString());
                Map<String, String> metaData = null;
                if (data instanceof XYMetaSeriesCollection) {
                    metaData = ((XYMetaSeriesCollection) data).getMetaData();
                }
                writeCSVHeader(writer, seriesKey.toString(), metaData);
                writeCSVData(writer, data, format);
            }
        }
        try {
            writer.close();
        }
        catch(IOException ioe) {
            log.error("Writing CSV export failed!");
        }
    }


    protected static void writeCSVHeader(
        CSVWriter writer,
        String key,
        Map<String, String> metaData)
    {
        writer.writeNext(new String[] {"#"});
        if (metaData != null) {
            writer.writeNext(new String[] {"# " + key});
            for (Map.Entry<String, String> entry: metaData.entrySet()) {
                if (entry.getKey().equals("X") || entry.getKey().equals("Y")) {
                    continue;
                }
                writer.writeNext(new String[]
                    {"# " + entry.getKey() + ": " + entry.getValue()});
            }
            writer.writeNext(new String[] {"#"});
            writer.writeNext(new String[] {
                metaData.get("X") != null ? metaData.get("X") : "X",
                metaData.get("Y") != null ? metaData.get("Y") : "Y"});
        }
        else {
            writer.writeNext(new String[] {"# " + key});
            writer.writeNext(new String[] {"#"});
            writer.writeNext(new String[] {"X", "Y"});
        }
    }


    /** Get x/y data from axis set and write it, on pair per line. */
    protected static void writeCSVData(
        CSVWriter writer, XYDataset data, NumberFormat format) {
        int series = data.getSeriesCount();
        for (int i = 0; i < series; i++) {
            int items = data.getItemCount(i);
            double lastX = java.lang.Double.MAX_VALUE;
            double lastY = java.lang.Double.MAX_VALUE;

            for (int j = 0; j < items; j++) {
                Number x = data.getX(i, j);
                Number y = data.getY(i, j);
                double xVal = data.getXValue(i, j);
                double yVal = data.getYValue(i, j);

                if (lastX == xVal && lastY == yVal) {
                    // comparing equality is ok here as we want
                    // to find data duplicates like they are added
                    // for example by the StyledSeriesBuilder in
                    // addStepPointsKmQ
                    log.debug("removing duplicate point in series");
                    continue;
                }
                lastX = xVal;
                lastY = yVal;

                String xString;
                String yString;

                try {
                    xString = java.lang.Double.isNaN(xVal)
                        ? ""
                        : format.format(x);
                    yString = java.lang.Double.isNaN(yVal)
                        ? ""
                        : format.format(y);
                }
                catch (NumberFormatException nfe) {
                    xString = x.toString();
                    yString = y.toString();
                }
                writer.writeNext(new String[] {
                    xString,
                    yString});
            }
        }
    }


    public static int[] getSize(CallContext cc) {
        int[] size = new int[2];

        size[0] = (Integer) cc.getContextValue("chart.width");
        size[1] = (Integer) cc.getContextValue("chart.height");

        return size;
    }


    /**
     * Returns the anchor of the chart so that the chart is centered
     * according to the given parameters.
     *
     * @param mLeft Left margin
     * @param mRight Right margin
     * @param mBottom Bottom margin
     * @param mTop Top margin
     * @param width The complete width of the drawing area.
     * @param height The complete height of the drawing area.
     * @param chartWidth The width of the chart.
     * @param chartHeight The height of the chart.
     *
     * @return an array that contains the anchor for a chart with the given
     * parameters. The first value is the x point, the second value is the y
     * point.
     */
    public static double[] getCenteredAnchor(
        double mLeft,      double mRight,      double mBottom, double mTop,
        double width,      double height,
        double chartWidth, double chartHeight
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Calculate centered origin...");
            log.debug("-> PDF width    : " + width);
            log.debug("-> PDF height   : " + height);
            log.debug("-> Chart width  : " + chartWidth);
            log.debug("-> Chart height : " + chartHeight);
            log.debug("-> margin left  : " + mLeft);
            log.debug("-> margin right : " + mRight);
            log.debug("-> margin bottom: " + mBottom);
            log.debug("-> margin top   : " + mTop);
        }

        double[] origin = new double[2];

        double centerX = width  / 2;
        double centerY = height / 2;

        origin[0] = centerX - chartWidth / 2;
        origin[1] = centerY - chartHeight / 2;

        origin[0] = origin[0] >= mLeft ? origin[0] : mLeft;
        origin[1] = origin[1] >= mTop ? origin[1] : mTop;

        if (log.isDebugEnabled()) {
            log.debug("==> centered left origin: " + origin[0]);
            log.debug("==> centered top  origin: " + origin[1]);
        }

        return origin;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
