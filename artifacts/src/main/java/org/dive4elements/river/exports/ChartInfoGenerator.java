/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.java2d.NOPGraphics2D;
import org.dive4elements.river.themes.ThemeDocument;

import java.io.IOException;
import java.io.OutputStream;

import java.awt.Transparency;
import java.awt.Graphics2D;

import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;

import org.dive4elements.artifacts.common.utils.XMLUtils;


/**
 * An OutGenerator that generates meta information for charts. A concrete
 * ChartInfoGenerator need to instantiate a concrete ChartGenerator and dispatch
 * the methods to that instance. The only thing this ChartInfoGenerator needs
 * to, is to overwrite the generate() method which doesn't write the chart image
 * to the OutputStream but a Document that contains some meta information of the
 * created chart.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class ChartInfoGenerator implements OutGenerator {

    public static final boolean USE_NOP_GRAPHICS =
        Boolean.getBoolean("info.rendering.nop.graphics");

    /** The log used in this generator.*/
    private static Logger log =
        LogManager.getLogger(ChartInfoGenerator.class);


    /** The OutGenerator that creates the charts.*/
    protected ChartGenerator generator;

    protected OutputStream out;



    public ChartInfoGenerator(ChartGenerator generator) {
        this.generator = generator;
    }

    public void setup(Object config) {
        log.debug("ChartInfoGenerator.setup");
    }


    /**
     * Dispatches the operation to the instantiated generator.
     *
     * @param request
     * @param out
     * @param context
     */
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        this.out = out;

        generator.init(outName, request, out, context);
    }


    /**
     * Dispatches the operation to the instantiated generator.
     *
     * @param master The master artifact
     */
    public void setMasterArtifact(Artifact master) {
        generator.setMasterArtifact(master);
    }


    /**
     * Dispatches the operation to the instantiated generator.
     *
     * @param collection The collection.
     */
    public void setCollection(D4EArtifactCollection collection) {
        generator.setCollection(collection);
    }


    /**
     * Dispatches the operation to the instantiated generator.
     */
    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    attr,
        boolean          visible
    ) {
        generator.doOut(artifactFacet, attr, visible);
    }


    /**
     * This method generates the chart using a concrete ChartGenerator but
     * doesn't write the chart itself to the OutputStream but a Document that
     * contains meta information of the created chart.
     */
    @Override
    public void generate()
    throws IOException
    {
        log.debug("ChartInfoGenerator.generate");

        JFreeChart chart = generator.generateChart();

        int[] size = generator.getSize();
        if (size == null) {
            size = generator.getDefaultSize();
        }

        ChartRenderingInfo info = new ChartRenderingInfo();

        long startTime = System.currentTimeMillis();

        if (USE_NOP_GRAPHICS) {
            BufferedImage image =
                new BufferedImage(size[0], size[1], Transparency.BITMASK);

            Graphics2D g2d  = image.createGraphics();
            Graphics2D nop = new NOPGraphics2D(g2d);

            chart.draw(
                nop,
                new Rectangle2D.Double(0, 0, size[0], size[1]),
                null,
                info);

            nop.dispose();
        }
        else {
            chart.createBufferedImage(
                size[0], size[1], Transparency.BITMASK, info);
        }

        long stopTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Rendering info took: " +
                (stopTime-startTime) + "ms");
        }


        InfoGeneratorHelper helper = new InfoGeneratorHelper(generator);
        Document doc = helper.createInfoDocument(chart, info);

        XMLUtils.toStream(doc, out);
    }


    /**
     * A proxy method which calls <i>generator</i>.getSettings() and returns its
     * return value.
     *
     * @return a Settings object provided by <i>generator</i>.
     */
    @Override
    public Settings getSettings() {
        return generator.getSettings();
    }


    /**
     * A proxy method which calls <i>generator</i>.setSettings().
     *
     * @param settings A settings object for the <i>generator</i>.
     */
    @Override
    public void setSettings(Settings settings) {
        generator.setSettings(settings);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
