/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

public class Config
{
    public static final String SKIP_DEFAULT =
        "flys.backend.importer.skip.default";

    public static final String DRY_RUN =
        "flys.backend.importer.dry.run";

    public static final String INFO_GEW_FILE =
        "flys.backend.importer.infogew.file";

    public static final String ANNOTATION_TYPES =
        "flys.backend.importer.annotation.types";

    public static final String SKIP_GAUGES =
        "flys.backend.importer.skip.gauges";

    public static final String SKIP_BWASTR =
        "flys.backend.importer.skip.bwastr";

    public static final String SKIP_HISTORICAL_DISCHARGE_TABLES =
        "flys.backend.importer.skip.historical.discharge.tables";

    public static final String SKIP_ANNOTATIONS =
        "flys.backend.importer.skip.annotations";

    public static final String SKIP_PRFS =
        "flys.backend.importer.skip.prfs";

    public static final String SKIP_DA50S =
        "flys.backend.importer.skip.da50s";

    public static final String SKIP_W80S =
        "flys.backend.importer.skip.w80s";

    public static final String SKIP_W80_CSVS =
        "flys.backend.importer.skip.w80.csvs";

    public static final String SKIP_HYKS =
        "flys.backend.importer.skip.hyks";

    public static final String SKIP_WST =
        "flys.backend.importer.skip.wst";

    public static final String SKIP_EXTRA_WSTS =
        "flys.backend.importer.skip.extra.wsts";

    public static final String SKIP_FIXATIONS =
        "flys.backend.importer.skip.fixations";

    public static final String SKIP_OFFICIAL_LINES =
        "flys.backend.importer.skip.official.lines";

    public static final String SKIP_FLOOD_WATER =
        "flys.backend.importer.skip.flood.water";

    public static final String SKIP_FLOOD_PROTECTION =
        "flys.backend.importer.skip.flood.protection";

    public static final String SKIP_BED_HEIGHT =
        "flys.backend.importer.skip.bed.height";

    public static final String SKIP_DA66S =
        "flys.backend.importer.skip.da66s";

    public static final String SKIP_SEDIMENT_DENSITY =
        "flys.backend.importer.skip.sediment.density";

    public static final String SKIP_POROSITY =
        "flys.backend.importer.skip.porosity";

    public static final String SKIP_MORPHOLOGICAL_WIDTH =
        "flys.backend.importer.skip.morphological.width";

    public static final String SKIP_FLOW_VELOCITY =
        "flys.backend.importer.skip.flow.velocity";

    public static final String SKIP_SEDIMENT_LOAD_LS =
        "flys.backend.importer.skip.sediment.load.ls";

    public static final String SKIP_SEDIMENT_LOAD =
        "flys.backend.importer.skip.sediment.load";

    public static final String SKIP_WATERLEVELS =
        "flys.backend.importer.skip.waterlevels";

    public static final String SKIP_WATERLEVEL_DIFFERENCES =
        "flys.backend.importer.skip.waterlevel.differences";

    public static final String SKIP_MEASUREMENT_STATIONS =
        "flys.backend.importer.skip.measurement.stations";

    public static final String SKIP_SQ_RELATION =
        "flys.backend.importer.skip.sq.relation";

    public static final Double CROSS_SECTION_SIMPLIFICATION_EPSILON =
        getDouble("flys.backend.importer.cross.section.simplification.epsilon");


    public static final Config INSTANCE = new Config();

    private Config() {
    }

    public static final boolean getFlag(String key) {
        String flag = System.getProperty(key);
        return flag != null
            ? Boolean.valueOf(flag)
            : Boolean.getBoolean(SKIP_DEFAULT);
    }

    public static final Double getDouble(String key) {
        try {
            String value = System.getProperty(key);
            return value != null
                ? Double.valueOf(value)
                : null;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public Double getCrossSectionSimplificationEpsilon() {
        return CROSS_SECTION_SIMPLIFICATION_EPSILON;
    }

    public boolean dryRun() {
        return getFlag(DRY_RUN);
    }

    public String getInfoGewFile() {
        return System.getProperty(INFO_GEW_FILE);
    }

    public String getAnnotationTypes() {
        return System.getProperty(ANNOTATION_TYPES);
    }

    public boolean skipGauges() {
        return getFlag(SKIP_GAUGES);
    }

    public boolean skipHistoricalDischargeTables() {
        return getFlag(SKIP_HISTORICAL_DISCHARGE_TABLES);
    }

    public boolean skipBWASTR() {
        return getFlag(SKIP_BWASTR);
    }

    public boolean skipAnnotations() {
        return getFlag(SKIP_ANNOTATIONS);
    }

    public boolean skipPRFs() {
        return getFlag(SKIP_PRFS);
    }

    public boolean skipDA50s() {
        return getFlag(SKIP_DA50S);
    }

    public boolean skipW80CSVs() {
        return getFlag(SKIP_W80_CSVS);
    }

    public boolean skipW80s() {
        return getFlag(SKIP_W80S);
    }

    public boolean skipHYKs() {
        return getFlag(SKIP_HYKS);
    }

    public boolean skipWst() {
        return getFlag(SKIP_WST);
    }

    public boolean skipExtraWsts() {
        return getFlag(SKIP_EXTRA_WSTS);
    }

    public boolean skipFixations() {
        return getFlag(SKIP_FIXATIONS);
    }

    public boolean skipOfficialLines() {
        return getFlag(SKIP_OFFICIAL_LINES);
    }

    public boolean skipFloodWater() {
        return getFlag(SKIP_FLOOD_WATER);
    }

    public boolean skipFloodProtection() {
        return getFlag(SKIP_FLOOD_PROTECTION);
    }

    public boolean skipDA66s() {
        return getFlag(SKIP_DA66S);
    }

    public boolean skipBedHeight() {
        return getFlag(SKIP_BED_HEIGHT);
    }

    public boolean skipSedimentDensity() {
        return getFlag(SKIP_SEDIMENT_DENSITY);
    }

    public boolean skipPorosity() {
        return getFlag(SKIP_POROSITY);
    }

    public boolean skipMorphologicalWidth() {
        return getFlag(SKIP_MORPHOLOGICAL_WIDTH);
    }

    public boolean skipFlowVelocity() {
        return getFlag(SKIP_FLOW_VELOCITY);
    }

    public boolean skipSedimentLoadLS() {
        return getFlag(SKIP_SEDIMENT_LOAD_LS);
    }

    public boolean skipSedimentLoad() {
        return getFlag(SKIP_SEDIMENT_LOAD);
    }

    public boolean skipWaterlevels() {
        return getFlag(SKIP_WATERLEVELS);
    }

    public boolean skipWaterlevelDifferences() {
        return getFlag(SKIP_WATERLEVEL_DIFFERENCES);
    }

    public boolean skipMeasurementStations() {
        return getFlag(SKIP_MEASUREMENT_STATIONS);
    }

    public boolean skipSQRelation() {
        return getFlag(SKIP_SQ_RELATION);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
