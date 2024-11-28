/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.text.NumberFormat;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.collections.D4EArtifactCollection;

import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.Formatter;


/**
 * Abstract exporter that implements some basic methods for exporting data of
 * artifacts.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class AbstractExporter implements OutGenerator {

    /** The log used in this exporter.*/
    private static Logger log = LogManager.getLogger(AbstractExporter.class);

    /* XXX: Why does AbstractExporter do not implement FacetTypes? */
    public static String FIX_PARAMETERS = "fix_parameters";

    /** The name of the CSV facet which triggers the CSV creation. */
    public static final String FACET_CSV = "csv";

    /** The name of the PDF facet which triggers the PDF creation. */
    public static final String FACET_PDF = "pdf";

    /** The default charset for the CSV export. */
    public static final String DEFAULT_CSV_CHARSET = "UTF-8";

    /** The default separator for the CSV export. */
    public static final char DEFAULT_CSV_SEPARATOR = ';';

    public static final String START_META_CHAR = "#";

    /** XPath that points to the desired export facet. */
    public static final String XPATH_FACET = "/art:action/@art:type";

    /** The out name to serve. */
    protected String outName;

    /** The document of the incoming out() request. */
    protected Document request;

    /** The output stream where the data should be written to. */
    protected OutputStream out;

    /** The CallContext object. */
    protected CallContext context;

    /** The selected facet. */
    protected String facet;

    /** The collection.*/
    protected D4EArtifactCollection collection;

    /** The master artifact. */
    protected Artifact master;

    private NumberFormat kmFormat;

    private NumberFormat wFormat;

    private NumberFormat qFormat;


    /**
     * Concrete subclasses need to use this method to write their special data
     * objects into the CSV document.
     *
     * @param writer The CSVWriter.
     */
    protected abstract void writeCSVData(CSVWriter writer) throws IOException;


    /**
     * Write lines of informative content to CSV file.
     * Usually this will be done above the column headers from within
     * the implementation of writeCSVData in concret subclasses.
     *
     * @param writer The CSVWriter
     * @param infolines Array of Strings with informative content.
     * Each will be written to a separate line prefixed with START_META_CHAR.
     */
    protected void  writeCSVInfo(CSVWriter writer, String[] infolines) {
        String[] metaline = new String[1];

        for (String infoline: infolines) {
            metaline[0] = START_META_CHAR + " " + infoline;
            writer.writeNext(metaline);
        }

    }

    /**
     * Concrete subclasses need to use this method to write their special data
     * objects into the PDF document.
     */
    protected abstract void writePDF(OutputStream out);


    /**
     * This method enables concrete subclasses to collected its own special
     * data.
     *
     * @param data The artifact that stores the data that has to be
     * exported.
     */
    protected abstract void addData(Object data);

    public void setup(Object config) {
        log.debug("AbstractExporter.setup");
    }


    @Override
    public void init(
        String       outName,
        Document     request,
        OutputStream out,
        CallContext  context
    ) {
        log.debug("AbstractExporter.init");

        this.outName = outName;
        this.request = request;
        this.out     = out;
        this.context = context;
    }


    @Override
    public void setMasterArtifact(Artifact master) {
        this.master = master;
    }

    /** Get the callcontext that this exporter has been initialized
     * with. */
    public CallContext getCallContext() {
        return this.context;
    }


    @Override
    public void setCollection(D4EArtifactCollection collection) {
        this.collection = collection;
    }


    /**
     * This doOut() just collects the data of multiple artifacts. Therefore, it
     * makes use of the addData() method which enables concrete subclasses to
     * store its data on its own. The real output creation takes place in the
     * concrete generate() methods.
     *
     * @param artifactFacet The artifact and facet.
     * The facet to add - NOTE: the facet needs to fit to the first
     * facet inserted into this exporter. Otherwise this artifact/facet is
     * skipped.
     * @param attr The attr document.
     */
    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    attr,
        boolean          visible
    ) {
        String name = artifactFacet.getFacetName();

        log.debug("AbstractExporter.doOut: " + name);

        if (!isFacetValid(name)) {
            log.warn("Facet '" + name + "' not valid. No output created!");
            return;
        }

        addData(artifactFacet.getData(context));
    }


    /**
     * Generates an export based on a specified facet.
     */
    @Override
    public void generate()
    throws IOException
    {
        log.debug("AbstractExporter.generate");

        if (facet == null) {
            throw new IOException("invalid (null) facet for exporter");
        }

        if (facet.equals(FACET_CSV)) {
            generateCSV();
        }
        else if (facet.equals(FACET_PDF)) {
            generatePDF();
        }
        else {
            throw new IOException(
                "invalid facet for exporter: '" + facet + "'");
        }
    }


    /**
     * Determines if the desired facet is valid for this exporter. If no facet
     * is currently set, <i>facet</i> is set.
     *
     * @param facet The desired facet.
     *
     * @return true, if <i>facet</i> is valid, otherwise false.
     */
    protected boolean isFacetValid(String facet) {
        log.debug("AbstractExporter.isFacetValid : "
            + facet + " (" + getFacet() + ")" );

        String thisFacet = getFacet();

        if (thisFacet == null || thisFacet.length() == 0) {
            return false;
        }
        else if (facet == null || facet.length() == 0) {
            return false;
        }
        else {
            return thisFacet.equals(facet);
        }
    }


    /**
     * Returns the name of the desired facet.
     *
     * @return the name of the desired facet.
     */
    protected String getFacet() {
        if (facet == null) {
            facet = getFacetFromRequest();
        }

        return facet;
    }


    /**
     * Extracts the name of the requested facet from request document.
     *
     * @return the name of the requested facet.
     */
    protected String getFacetFromRequest() {
        return XMLUtils.xpathString(
            request, XPATH_FACET, ArtifactNamespaceContext.INSTANCE);
    }

    protected String msg(String key) {
        return Resources.getMsg(context.getMeta(), key, key);
    }

    protected String msg(String key, String def) {
        return Resources.getMsg(context.getMeta(), key, def);
    }

    protected String msg(String key, Object[] args) {
        return Resources.getMsg(context.getMeta(), key, key, args);
    }

    protected String msg(String key, String def, Object[] args) {
        return Resources.getMsg(context.getMeta(), key, def, args);
    }


    /**
     * This method starts CSV creation. It makes use of writeCSVData() which has
     * to be implemented by concrete subclasses.
     */
    protected void generateCSV()
    throws    IOException
    {
        log.info("AbstractExporter.generateCSV");

        char quote = '"';
        char escape = '\\';

        CSVWriter writer = new CSVWriter(
            new OutputStreamWriter(
                out,
                DEFAULT_CSV_CHARSET),
            DEFAULT_CSV_SEPARATOR, quote, escape, "\r\n");

        writeCSVData(writer);

        writer.close();
    }


    /**
     * This method starts PDF creation.
     */
    protected void generatePDF()
    throws    IOException
    {
        log.info("AbstractExporter.generatePDF");
        writePDF(this.out);
    }


    /**
     * Returns an instance of <i>EmptySettings</i> currently!
     *
     * @return an instance of <i>EmptySettings</i>.
     */
    public Settings getSettings() {
        return new EmptySettings();
    }


    /**
     * This method is not implemented. Override it in subclasses if those need a
     * <i>Settings</i> object.
     */
    public void setSettings(Settings settings) {
        // do nothing
    }


    /**
     * Returns the number formatter for kilometer values.
     *
     * @return the number formatter for kilometer values.
     */
    protected NumberFormat getKmFormatter() {
        if (kmFormat == null) {
            kmFormat = Formatter.getWaterlevelKM(context);
        }
        return kmFormat;
    }


    /**
     * Returns the number formatter for W values.
     *
     * @return the number formatter for W values.
     */
    protected NumberFormat getWFormatter() {
        if (wFormat == null) {
            wFormat = Formatter.getWaterlevelW(context);
        }
        return wFormat;
    }


    /**
     * Returns the number formatter for Q values.
     *
     * @return the number formatter for Q values.
     */
    protected NumberFormat getQFormatter() {
        if (qFormat == null) {
            qFormat = Formatter.getWaterlevelQ(context);
        }
        return qFormat;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
