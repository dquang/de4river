/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.FixAccess;

import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;

import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.artifacts.model.fixings.FixResult;

import org.dive4elements.river.exports.AbstractExporter;

import org.dive4elements.river.model.River;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.NodeList;

/** Export result of fixation analysis. */
public class FixATExport extends AbstractExporter {

    /** Private log. */
    private static Logger log =
        LogManager.getLogger(FixATExport.class);

    protected Function function;
    protected Parameters parameters;


    @Override
    public void doOut(
        ArtifactAndFacet bundle,
        ThemeDocument attr,
        boolean visible
    ) {
        log.debug("AT Export doOut().");
        Object data = bundle.getData(context);
        if (data instanceof CalculationResult) {
            CalculationResult cr = (CalculationResult)data;
            Object resData = cr.getData();
            if (resData instanceof FixResult) {
                this.parameters = ((FixResult)resData).getParameters();
            }
        }
        else {
            log.debug("No CalculationResult found for AT export.");
            return;
        }
        FixAccess access = new FixAccess((D4EArtifact)this.master);
        String f = access.getFunction();
        if (f == null || f.length() == 0) {
            log.debug("No function found for AT export.");
            return;
        }
        this.function = FunctionFactory.getInstance().getFunction(f);
    }

    @Override
    public void generate() throws IOException {
        if (this.function == null || this.parameters == null) {
            log.debug("No function or paramters for AT export.");
            return;
        }

        Writer writer = new OutputStreamWriter(out, DEFAULT_CSV_CHARSET);

        FixATWriter atWriter = new FixATWriter(this.function, this.parameters);
        NodeList nodes = request.getElementsByTagName("km");
        String km = nodes.item(0).getTextContent();
        double dkm = Double.parseDouble(km);
        River river = RiverUtils.getRiver((D4EArtifact)master);
        atWriter.write(writer, context.getMeta(), river, dkm);
        writer.close();
    }

    @Override
    protected void writeCSVData(CSVWriter writer) throws IOException {
        // The concrete writer is used to write csv data.
    }

    @Override
    protected void writePDF(OutputStream out) {
        // Implement me!
    }

    @Override
    protected void addData(Object data) {
        // Nothing to do here.
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
