/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.artifacts.model.fixings.FixAnalysisResult;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.exports.AbstractExporter;

import java.io.IOException;
import java.io.OutputStream;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ParametersExporter
extends      AbstractExporter
{
    private static Logger log = LogManager.getLogger(ParametersExporter.class);

    protected List<Parameters> parametersList;

    public ParametersExporter() {
        parametersList = new ArrayList<Parameters>();
    }

    @Override
    protected void addData(Object d) {
        log.debug("ParametersExporter.addData");
        if (!(d instanceof CalculationResult)) {
            log.warn("Invalid data type");
            return;
        }

        Object data = ((CalculationResult)d).getData();
        if (!(data instanceof FixAnalysisResult)) {
            log.warn("Invalid data stored in result.");
            return;
        }

        FixAnalysisResult result = (FixAnalysisResult)data;
        parametersList.add(result.getParameters());
    }

    @Override
    public void generate()
    throws IOException
    {
        log.debug("ParametersExporter.generate");

        if (facet == null) {
            throw new IOException("invalid (null) facet for exporter");
        }

        if (facet.equals(FIX_PARAMETERS)) {
            generateCSV();
        }
        else {
            throw new IOException(
                "invalid facet for exporter: '" + facet + "'");
        }
    }

    @Override
    protected void writeCSVData(final CSVWriter writer) throws IOException {

        if (parametersList.isEmpty()) {
            return;
        }

        Parameters parameters = parametersList.get(0);
        writer.writeNext(parameters.getColumnNames());

        final int numColumns = parameters.getNumberColumns();

        parameters.visit(new Parameters.Visitor() {

            String [] row = new String[numColumns];

            NumberFormat format = NumberFormat.getInstance(
                Resources.getLocale(context.getMeta()));

            @Override
            public void visit(double [] data) {
                for (int i = 0; i < data.length; ++i) {
                    row[i] = format.format(data[i]);
                }
                writer.writeNext(row);
            }
        }, new double[numColumns]);

        writer.flush();
    }

    @Override
    protected void writePDF(OutputStream out) {
        // TODO: Implement me!
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
