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
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.minfo.MiddleBedHeightData;

import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.RiverUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MiddleBedHeightExporter extends AbstractExporter {

    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(MiddleBedHeightExporter.class);

    public static final String CSV_KM =
        "export.bedheight_middle.csv.header.km";

    public static final String CSV_SOUNDING =
        "export.bedheight_middle.csv.header.sounding";

    public static final String CSV_HEIGHT =
        "export.bedheight_middle.csv.header.height";

    public static final String CSV_UNCERTAINTY =
        "export.bedheight_middle.csv.header.uncertainty";

    public static final String CSV_DATA_GAP =
        "export.bedheight_middle.csv.header.datagap";

    public static final String CSV_SOUNDING_WIDTH =
        "export.bedheight_middle.csv.header.soundingwidth";

    public static final String CSV_LOCATIONS =
        "export.bedheight_middle.csv.header.locations";

    public static final String CSV_META_YEAR =
        "meta.bedheight.year";

    public static final String CSV_META_TYPE =
        "meta.bedheight.type";

    public static final String CSV_META_CUR_ELEV_MODEL =
        "meta.bedheight.cur.elevation";

    public static final String CSV_META_OLD_ELEV_MODEL =
        "meta.bedheight.old.elevation";

    public static final String CSV_META_RIVER_ELEV_MODEL =
        "meta.bedheight.river.elevation";

    public static final String CSV_META_RANGE =
        "meta.bedheight.range";

    public static final String CSV_META_LOC_SYSTEM =
        "meta.bedheight.location.system";

    public static final String CSV_META_EVAL_BY =
        "meta.bedheight.evalby";

    protected List<MiddleBedHeightData> data;

    public MiddleBedHeightExporter() {
        data = new ArrayList<MiddleBedHeightData>();
    }

    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult) d).getData();

            if (d instanceof MiddleBedHeightData[]) {
                log.debug("Add new data of type MiddleBedHeightData");
                for (MiddleBedHeightData mD :(MiddleBedHeightData[]) d) {
                    data.add(mD);
                }
            }
        }
    }


    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.info("MiddleBedHeightExporter.writeCSVData");
        log.debug("CSV gets " + data.size() + " MiddleBedHeightData objects.");


        Collections.sort(data);

        writeCSVHeader(writer);

        for (MiddleBedHeightData d: data) {
            data2CSV(writer, d);
        }
    }


    protected void writeCSVHeader(CSVWriter writer) {
        River river = RiverUtils.getRiver((D4EArtifact) master);
        String riverUnit = river.getWstUnit().getName();
        writer.writeNext(new String[] {
            msg(CSV_KM),
            msg(CSV_SOUNDING),
            msg(CSV_HEIGHT, new Object[] {riverUnit}),
            msg(CSV_UNCERTAINTY),
            msg(CSV_DATA_GAP),
            msg(CSV_SOUNDING_WIDTH),
            msg(CSV_LOCATIONS)
        });
    }


    protected void data2CSV(CSVWriter writer, MiddleBedHeightData data) {
        log.debug("Add next MiddleBedHeightData to CSV");

        D4EArtifact flys = (D4EArtifact) master;

        writeMetaData(writer, data);

        NumberFormat kmF     = Formatter.getMiddleBedHeightKM(context);
        NumberFormat heightF = Formatter.getMiddleBedHeightHeight(context);
        NumberFormat uncertF = Formatter.getMiddleBedHeightUncert(context);
        NumberFormat gapF    = Formatter.getMiddleBedHeightDataGap(context);
        NumberFormat soundF  = Formatter.getMiddleBedHeightSounding(context);

        heightF.setMaximumFractionDigits(1);
        soundF.setMaximumFractionDigits(1);

        SortedMap <Double, Integer> kmIndexMap = new TreeMap<Double, Integer>();

        for (int i = 0, n = data.size(); i < n; i++) {
            kmIndexMap.put(data.getKM(i), i);
        }

        for (int i: kmIndexMap.values()) {
            String uncert = !Double.isNaN(data.getUncertainty(i)) ?
                uncertF.format(data.getUncertainty(i)) : "";
            String gap = !Double.isNaN(data.getDataGap(i)) ?
                gapF.format(data.getDataGap(i)) + "%" : "";
            String sound = !Double.isNaN(data.getSoundingWidth(i)) ?
                soundF.format(data.getSoundingWidth(i)) : "";
            writer.writeNext(new String[] {
                    kmF.format(data.getKM(i)),
                    data.getDescription(),
                    heightF.format(data.getMiddleHeight(i)),
                    uncert,
                    gap,
                    sound,
                    RiverUtils.getLocationDescription(flys, data.getKM(i)),
                });
        }
    }


    private void writeMetaData(CSVWriter writer, MiddleBedHeightData data) {
         String year = "";
         if (data.getYear() != 0) {
             year = String.valueOf(data.getYear());
         }
         writeCSVInfo(writer, new String[] {
                 "", // blank meta-line to separate datasets in CSV
                 msg(CSV_META_YEAR) + ": " + year,
                 msg(CSV_META_TYPE) + ": " + data.getType(),
                 msg(CSV_META_LOC_SYSTEM) + ": " + data.getLocationSystem(),
                 msg(CSV_META_CUR_ELEV_MODEL) + ": " +
                     data.getCurElevationModel(),
                 msg(CSV_META_OLD_ELEV_MODEL) + ": " +
                     data.getOldElevationModel(),
                 msg(CSV_META_RIVER_ELEV_MODEL) + ": " +
                     data.getRiverElevationModel(),
                 msg(CSV_META_RANGE) + ": " + data.getStations().min() +
                     " - " + data.getStations().max(),
                 msg(CSV_META_EVAL_BY) + ": " + data.getEvaluatedBy()
             });
    }

    @Override
    protected void writePDF(OutputStream out) {
        log.error("TODO: Implement MiddleBedHeightExporter.writePDF");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
