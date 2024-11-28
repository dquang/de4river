/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FlowVelocityData;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FlowVelocityExporter extends AbstractExporter {

    private static final Logger log =
        LogManager.getLogger(FlowVelocityExporter.class);


    public static final String CSV_KM =
        "export.flow_velocity.csv.header.km";

    public static final String CSV_V_TOTAL =
        "export.flow_velocity.csv.header.v_total";

    public static final String CSV_V_MAIN =
        "export.flow_velocity.csv.header.v_main";

    public static final String CSV_TAU_MAIN =
        "export.flow_velocity.csv.header.tau_main";

    public static final String CSV_Q =
        "export.flow_velocity.csv.header.q";

    public static final String CSV_LOCATIONS =
        "export.flow_velocity.csv.header.locations";


    protected List<FlowVelocityData[]> data;

    public FlowVelocityExporter() {
        data = new ArrayList<FlowVelocityData[]>();
    }

    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult) d).getData();

            if (d instanceof FlowVelocityData[]) {
                log.debug("Add new data of type FlowVelocityData");
                data.add((FlowVelocityData[]) d);
            }
        }
    }


    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.info("FlowVelocityExporter.writeCSVData");
        log.debug("CSV gets " + data.size() + " FlowVelocityData objects.");

        writeCSVHeader(writer);

        for (FlowVelocityData[] d: data) {
            data2CSV(writer, d);
        }
    }


    protected void writeCSVHeader(CSVWriter writer) {
        writer.writeNext(new String[] {
            msg(CSV_KM, CSV_KM),
            msg(CSV_V_MAIN, CSV_V_MAIN),
            msg(CSV_V_TOTAL, CSV_V_TOTAL),
            msg(CSV_TAU_MAIN, CSV_TAU_MAIN),
            msg(CSV_Q, CSV_Q),
            msg(CSV_LOCATIONS, CSV_LOCATIONS)
        });
    }


    protected void data2CSV(CSVWriter writer, FlowVelocityData[] fData) {
        log.debug("Add next FlowVelocityData to CSV");

        D4EArtifact flys = (D4EArtifact) master;

        for (FlowVelocityData data: fData) {
            for (int i = 0, n = data.size(); i < n; i++) {
                NumberFormat kmF  = Formatter.getFlowVelocityKM(context);
                NumberFormat valF = Formatter.getFlowVelocityValues(context);
                NumberFormat qF   = Formatter.getFlowVelocityQ(context);

                String vMain = "";
                String vTotal = "";

                if (data.getType().equals("main")
                    || data.getType().equals("main_total")
                ) {
                    vMain = valF.format(data.getVMain(i));
                }
                if (data.getType().equals("total")
                    || data.getType().equals("main_total")
                ) {
                    vTotal = valF.format(data.getVTotal(i));
                }
                writer.writeNext(new String[] {
                    kmF.format(data.getKM(i)),
                    vMain,
                    vTotal,
                    valF.format(data.getTauMain(i)),
                    qF.format(data.getQ(i)) + "=" + data.getZone(),
                    RiverUtils.getLocationDescription(flys, data.getKM(i)),
                });
            }
        }
    }


    @Override
    protected void writePDF(OutputStream out) {
        log.error("TODO: Implement FlowVelocityExporter.writePDF");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
