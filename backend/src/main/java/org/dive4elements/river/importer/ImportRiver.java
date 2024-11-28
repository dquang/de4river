/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.artifacts.common.utils.FileTools.HashedFile;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.parsers.AnnotationClassifier;
import org.dive4elements.river.importer.parsers.AnnotationsParser;
import org.dive4elements.river.importer.parsers.BedHeightParser;
import org.dive4elements.river.importer.parsers.CrossSectionParser;
import org.dive4elements.river.importer.parsers.DA50Parser;
import org.dive4elements.river.importer.parsers.DA66Parser;
import org.dive4elements.river.importer.parsers.FlowVelocityMeasurementParser;
import org.dive4elements.river.importer.parsers.FlowVelocityModelParser;
import org.dive4elements.river.importer.parsers.HYKParser;
import org.dive4elements.river.importer.parsers.MeasurementStationsParser;
import org.dive4elements.river.importer.parsers.MorphologicalWidthParser;
import org.dive4elements.river.importer.parsers.OfficialLinesConfigParser;
import org.dive4elements.river.importer.parsers.PRFParser;
import org.dive4elements.river.importer.parsers.PegelGltParser;
import org.dive4elements.river.importer.parsers.PorosityParser;
import org.dive4elements.river.importer.parsers.SQRelationParser;
import org.dive4elements.river.importer.parsers.SedimentDensityParser;
import org.dive4elements.river.importer.parsers.AbstractSedimentLoadParser;
import org.dive4elements.river.importer.parsers.SedimentLoadLSParser;
import org.dive4elements.river.importer.parsers.SedimentLoadParser;
import org.dive4elements.river.importer.parsers.W80Parser;
import org.dive4elements.river.importer.parsers.W80CSVParser;
import org.dive4elements.river.importer.parsers.WaterlevelDifferencesParser;
import org.dive4elements.river.importer.parsers.WaterlevelParser;
import org.dive4elements.river.importer.parsers.WstParser;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Unit;

import org.dive4elements.river.backend.utils.DouglasPeuker;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Query;
import org.hibernate.Session;

/** Import all river-related data (files) that can be found. */
public class ImportRiver
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(ImportRiver.class);

    public static final String PEGEL_GLT = "PEGEL.GLT";

    public static final String FIXATIONS = "Fixierungen";

    public static final String EXTRA_LONGITUDINALS =
        "Zus.Laengsschnitte";

    public static final String [] OFFICIAL_LINES_FOLDERS = {
        "Basisdaten",
        "Fixierungen" };

    public static final String OFFICIAL_LINES =
        "Amtl_Linien.wst";

    public static final String OFFICIAL_LINES_CONFIG =
        "Amtl_Linien.config";

    public static final String FLOOD_WATER = "HW-Marken";

    public static final String FLOOD_PROTECTION =
        "HW-Schutzanlagen";

    public static final String MINFO_DIR = "Morphologie";

    public static final String BED_HEIGHT_DIR = "Sohlhoehen";

    public static final String BED_HEIGHT_SINGLE_DIR = "Einzeljahre";

    public static final String SEDIMENT_DENSITY_DIR = "Sedimentdichte";

    public static final String POROSITY_DIR = "Porositaet";

    public static final String MORPHOLOGICAL_WIDTH_DIR =
        "morphologische_Breite";

    public static final String FLOW_VELOCITY_DIR =
        "Geschwindigkeit_Schubspannung";

    public static final String FLOW_VELOCITY_MODEL = "Modellrechnungen";

    public static final String FLOW_VELOCITY_MEASUREMENTS = "v-Messungen";

    public static final String SEDIMENT_LOAD_DIR = "Fracht";

    public static final String SEDIMENT_LOAD_LS_DIR = "Laengsschnitte";

    public static final String SEDIMENT_LOAD_MS_DIR = "Messstellen";

    public static final String SEDIMENT_LOAD_SINGLE_DIR = "Einzeljahre";

    public static final String SEDIMENT_LOAD_EPOCH_DIR = "Epochen";

    public static final String SEDIMENT_LOAD_OFF_EPOCH_DIR =
        "amtliche Epochen";

    public static final String MINFO_FIXATIONS_DIR = "Fixierungsanalyse";

    public static final String MINFO_WATERLEVELS_DIR = "Wasserspiegellagen";

    public static final String MINFO_WATERLEVEL_DIFF_DIR =
        "Wasserspiegeldifferenzen";

    public static final String MINFO_BASE_DIR = "Basisdaten";

    public static final String MINFO_CORE_DATA_FILE =
        "Stammdaten_Messstellen.csv";

    public static final String MINFO_SQ_DIR =
        "Feststofftransport-Abfluss-Beziehung";

    protected String name;

    protected String modelUuid;

    protected Long officialNumber;

    protected File wstFile;

    protected File bbInfoFile;

    protected List<ImportGauge> gauges;

    protected List<ImportAnnotation> annotations;

    protected List<ImportHYK> hyks;

    protected List<ImportCrossSection> crossSections;

    protected List<ImportWst> extraWsts;

    protected List<ImportWst> fixations;

    protected List<ImportWst> officialLines;

    protected List<ImportWst> floodWater;

    protected List<ImportWst> floodProtection;

    /** Wst-structures from waterlevel-csv files. */
    protected List<ImportWst> waterlevels;

    /** Wst-structures from waterlevel-difference-csv files. */
    protected List<ImportWst> waterlevelDifferences;

    protected List<ImportBedHeight> bedHeights;

    protected List<ImportSedimentDensity> sedimentDensities;

    protected List<ImportPorosity> porosities;

    protected List<ImportMorphWidth> morphologicalWidths;

    protected List<ImportFlowVelocityModel> flowVelocityModels;

    protected List<ImportFlowVelocityMeasurement> flowVelocityMeasurements;

    protected List<ImportSedimentLoadLS> sedimentLoadLSs;

    protected List<ImportSedimentLoad> sedimentLoads;

    protected List<ImportMeasurementStation> measurementStations;

    protected List<ImportSQRelation> sqRelations;

    protected ImportWst wst;

    protected ImportUnit wstUnit;

    protected AnnotationClassifier annotationClassifier;

    /** Database-mapped River instance. */
    protected River peer;


    /** Callback-implementation for CrossSectionParsers. */
    private class ImportRiverCrossSectionParserCallback
    implements    CrossSectionParser.Callback {

        private Set<HashedFile> files = new HashSet<HashedFile>();
        private String          type;

        /**
         * Create new Callback, given type which is used for logging
         * purposes only.
         */
        public ImportRiverCrossSectionParserCallback(String type) {
            this.type = type;
        }


        /** Accept file if not duplicate. */
        @Override
        public boolean accept(File file) {
            HashedFile hf = new HashedFile(file);
            boolean success = files.add(hf);
            if (!success) {
                log.warn(type + " file '" + file
                    + "' seems to be a duplicate.");
            }
            return success;
        }


        /** Add crosssection. */
        @Override
        public void parsed(CrossSectionParser parser) {
           log.debug("callback from " + type + " parser");

            String  description = parser.getDescription();
            Integer year        = parser.getYear();
            ImportTimeInterval ti = year != null
                ? new ImportTimeInterval(yearToDate(year))
                : null;

            Map<Double, List<XY>> data = parser.getData();

            List<ImportCrossSectionLine> lines =
                new ArrayList<ImportCrossSectionLine>(data.size());

            Double simplificationEpsilon =
                Config.INSTANCE.getCrossSectionSimplificationEpsilon();

            long numReadPoints      = 0L;
            long numRemainingPoints = 0L;

            for (Map.Entry<Double, List<XY>> entry: data.entrySet()) {
                Double   km     = entry.getKey();
                List<XY> points = entry.getValue();
                numReadPoints += points.size();
                if (simplificationEpsilon != null) {
                    points = DouglasPeuker.simplify(
                        points, simplificationEpsilon);
                }
                numRemainingPoints += points.size();
                lines.add(new ImportCrossSectionLine(km, points));
            }

            ImportRiver.this.addCrossSections(description, ti, lines);

            if (simplificationEpsilon != null) {
                double percent = numReadPoints > 0L
                    ? ((double)numRemainingPoints/numReadPoints)*100d
                    : 0d;

                log.info(String.format(
                    "Number of points in cross section: %d / %d (%.2f%%)",
                    numReadPoints, numRemainingPoints, percent));
            }
        }
    } // ImportRiverCrossSectionParserCallback


    private void addCrossSections(
        String                       description,
        ImportTimeInterval           ti,
        List<ImportCrossSectionLine> lines
    ) {
        crossSections.add(
            new ImportCrossSection(this, description, ti, lines));
    }


    public ImportRiver() {
        hyks                     = new ArrayList<ImportHYK>();
        crossSections            = new ArrayList<ImportCrossSection>();
        extraWsts                = new ArrayList<ImportWst>();
        fixations                = new ArrayList<ImportWst>();
        officialLines            = new ArrayList<ImportWst>();
        floodWater               = new ArrayList<ImportWst>();
        waterlevels              = new ArrayList<ImportWst>();
        waterlevelDifferences    = new ArrayList<ImportWst>();
        floodProtection          = new ArrayList<ImportWst>();
        sedimentDensities        = new ArrayList<ImportSedimentDensity>();
        porosities               = new ArrayList<ImportPorosity>();
        morphologicalWidths      = new ArrayList<ImportMorphWidth>();
        flowVelocityModels       = new ArrayList<ImportFlowVelocityModel>();
        flowVelocityMeasurements =
            new ArrayList<ImportFlowVelocityMeasurement>();
        sedimentLoadLSs          = new ArrayList<ImportSedimentLoadLS>();
        sedimentLoads            = new ArrayList<ImportSedimentLoad>();
        measurementStations      = new ArrayList<ImportMeasurementStation>();
        sqRelations              = new ArrayList<ImportSQRelation>();
    }

    public ImportRiver(
        String               name,
        String               modelUuid,
        File                 wstFile,
        File                 bbInfoFile,
        AnnotationClassifier annotationClassifier
    ) {
        this();
        this.name                 = name;
        this.modelUuid            = modelUuid;
        this.wstFile              = wstFile;
        this.bbInfoFile           = bbInfoFile;
        this.annotationClassifier = annotationClassifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelUuid() {
        return modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    public Long getOfficialNumber() {
        return this.officialNumber;
    }

    public void setOfficialNumber(Long officialNumber) {
        this.officialNumber = officialNumber;
    }

    public File getWstFile() {
        return wstFile;
    }

    public void setWstFile(File wstFile) {
        this.wstFile = wstFile;
    }

    public File getBBInfo() {
        return bbInfoFile;
    }

    public void setBBInfo(File bbInfoFile) {
        this.bbInfoFile = bbInfoFile;
    }

    public ImportWst getWst() {
        return wst;
    }

    public void setWst(ImportWst wst) {
        this.wst = wst;
    }

    public File getMinfoDir() {
        File riverDir  = wstFile
            .getParentFile().getParentFile().getParentFile();
        return new File(riverDir, MINFO_DIR);
    }

    public void parseDependencies() throws IOException {
        parseGauges();
        parseAnnotations();
        parsePRFs();
        parseDA66s();
        parseDA50s();
        parseW80s();
        parseW80CSVs();
        parseHYKs();
        parseWst();
        parseExtraWsts();
        parseFixations();
        parseOfficialLines();
        parseFloodWater();
        parseFloodProtection();
        parseMeasurementStations();
        parseBedHeight();
        parseSedimentDensity();
        parsePorosity();
        parseMorphologicalWidth();
        parseFlowVelocity();
        parseSedimentLoadLS();
        parseSedimentLoad();
        parseWaterlevels();
        parseWaterlevelDifferences();
        parseSQRelation();
    }

    public void parseFloodProtection() throws IOException {
        if (Config.INSTANCE.skipFloodProtection()) {
            log.info("skip parsing flood protection");
            return;
        }

        log.info("Parse flood protection wst file");

        File riverDir = wstFile.getParentFile().getParentFile();

        File dir = FileTools.repair(new File(riverDir, FLOOD_PROTECTION));

        if (!dir.isDirectory() || !dir.canRead()) {
            log.info("no directory '" + dir + "' found");
            return;
        }

        File [] files = dir.listFiles();

        if (files == null) {
            log.warn("cannot read '" + dir + "'");
            return;
        }

        for (File file: files) {
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!(name.endsWith(".zus") || name.endsWith(".wst"))) {
                continue;
            }
            log.info("found file '" + file.getName() + "'");
            try {
                WstParser wstParser = new WstParser();
                wstParser.parse(file);
                ImportWst iw = wstParser.getWst();
                iw.setKind(5);
                iw.setDescription(
                    FLOOD_PROTECTION + "/" + iw.getDescription());
                floodProtection.add(iw);
            }
            catch (WstParser.ParseException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void storeOfficialNumber() {
        if (Config.INSTANCE.skipBWASTR()) {
            log.info("skip storing official number.");
            return;
        }
        getPeer().setOfficialNumber(officialNumber);
    }

    public void parseBedHeight() throws IOException {
        File minfoDir     = getMinfoDir();
        File bedHeightDir = new File(minfoDir, BED_HEIGHT_DIR);
        File singlesDir   = new File(bedHeightDir, BED_HEIGHT_SINGLE_DIR);

        if (Config.INSTANCE.skipBedHeight()) {
            log.info("skip parsing bed heights.");
        }
        else {
            log.info("Parse bed heights.");
            parseBedHeights(singlesDir);
        }
    }


    protected void parseSedimentDensity() throws IOException {
        if (Config.INSTANCE.skipSedimentDensity()) {
            log.info("skip parsing sediment density.");
            return;
        }

        log.debug("Parse sediment density");

        File minfoDir = getMinfoDir();
        File sediment = new File(minfoDir, SEDIMENT_DENSITY_DIR);

        File[] files = sediment.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + sediment + "'");
            return;
        }

        SedimentDensityParser parser = new SedimentDensityParser();

        for (File file: files) {
            parser.parse(file);
        }

        sedimentDensities = parser.getSedimentDensities();

        log.info("Parsed " + sedimentDensities.size()
            + " sediment densities.");
    }

    protected void parsePorosity() throws IOException {
        if (Config.INSTANCE.skipPorosity()) {
            log.info("skip parsing porosity.");
            return;
        }

        log.debug("Parse porosity");

        File minfoDir = getMinfoDir();
        File porosity = new File(minfoDir, POROSITY_DIR);

        File[] files = porosity.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + porosity + "'");
            return;
        }

        PorosityParser parser = new PorosityParser();

        for (File file: files) {
            parser.parse(file);
        }

        porosities = parser.getPorosities();

        log.info("Parsed " + porosities.size() + " porosities.");
    }

    protected void parseMorphologicalWidth() throws IOException {
        if (Config.INSTANCE.skipMorphologicalWidth()) {
            log.info("skip parsing morphological width.");
            return;
        }

        log.debug("Parse morphological width");

        File minfoDir = getMinfoDir();
        File morphDir = new File(minfoDir, MORPHOLOGICAL_WIDTH_DIR);

        File[] files = morphDir.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + morphDir + "'");
            return;
        }

        MorphologicalWidthParser parser = new MorphologicalWidthParser();

        for (File file: files) {
            parser.parse(file);
        }

        morphologicalWidths = parser.getMorphologicalWidths();

        log.info("Parsed " + morphologicalWidths.size()
            + " morph. widths files.");
    }


    protected void parseFlowVelocity() throws IOException {
        if (Config.INSTANCE.skipFlowVelocity()) {
            log.info("skip parsing flow velocity");
            return;
        }

        log.debug("Parse flow velocity");

        File minfoDir   = getMinfoDir();
        File flowDir    = new File(minfoDir, FLOW_VELOCITY_DIR);
        File modelDir   = new File(flowDir, FLOW_VELOCITY_MODEL);
        File measureDir = new File(flowDir, FLOW_VELOCITY_MEASUREMENTS);

        File[] modelFiles   = modelDir.listFiles();
        File[] measureFiles = measureDir.listFiles();

        if (modelFiles == null) {
            log.warn("Cannot read directory '" + modelDir + "'");
        }
        else {
            FlowVelocityModelParser parser = new FlowVelocityModelParser();

            for (File model: modelFiles) {
                log.debug("Parse file '" + model + "'");
                parser.parse(model);
            }

            flowVelocityModels = parser.getModels();
        }

        if (measureFiles == null) {
            log.warn("Cannot read directory '" + measureDir + "'");
        }
        else {
            FlowVelocityMeasurementParser parser =
                new FlowVelocityMeasurementParser();

            for (File measurement: measureFiles) {
                log.debug("Parse file '" + measurement + "'");
                parser.parse(measurement);
            }

            flowVelocityMeasurements = parser.getMeasurements();
        }
    }


    private void parseSedimentLoadFiles(
        File[] files,
        AbstractSedimentLoadParser parser
    ) throws IOException {
       for (File file: files) {
           if (file.isDirectory()) {
               for (File child: file.listFiles()) {
                   parser.parse(child);
               }
           }
           else {
               parser.parse(file);
           }
       }
    }


    private void parseSedimentLoadDir(
        File sedimentLoadDir,
        AbstractSedimentLoadParser parser
    ) throws IOException {

        File[] sedimentLoadSubDirs = {
            new File(sedimentLoadDir,
                     SEDIMENT_LOAD_SINGLE_DIR),
            new File(sedimentLoadDir,
                     SEDIMENT_LOAD_EPOCH_DIR),
            new File(sedimentLoadDir,
                     SEDIMENT_LOAD_OFF_EPOCH_DIR),
        };

        for (File subDir : sedimentLoadSubDirs) {
            File[] files = subDir.listFiles();

            if (files == null || files.length == 0) {
                log.warn("Cannot read directory '" + subDir + "'");
            }
            else {
                parseSedimentLoadFiles(files, parser);
            }
        }
    }


    protected void parseSedimentLoadLS() throws IOException {
        if (Config.INSTANCE.skipSedimentLoadLS()) {
            log.info("skip parsing sediment load longitudinal section data");
            return;
        }

        log.debug("Parse sediment load longitudinal section data");

        SedimentLoadLSParser parser = new SedimentLoadLSParser();

        File minfoDir          = getMinfoDir();
        File sedimentLoadDir   = new File(minfoDir, SEDIMENT_LOAD_DIR);
        File sedimentLoadLSDir = new File(sedimentLoadDir,
                                          SEDIMENT_LOAD_LS_DIR);

        parseSedimentLoadDir(sedimentLoadLSDir, parser);

        sedimentLoadLSs = parser.getSedimentLoadLSs();
    }


    protected void parseSedimentLoad() throws IOException {
        if (Config.INSTANCE.skipSedimentLoad()) {
            log.info(
                "skip parsing sediment load data at measurement stations");
            return;
        }

        log.debug("Parse sediment load data at measurement stations");

        SedimentLoadParser parser = new SedimentLoadParser(getPeer());

        File minfoDir          = getMinfoDir();
        File sedimentLoadDir   = new File(minfoDir, SEDIMENT_LOAD_DIR);
        File sedimentLoadMSDir = new File(sedimentLoadDir,
                                          SEDIMENT_LOAD_MS_DIR);

        parseSedimentLoadDir(sedimentLoadMSDir, parser);

        sedimentLoads = parser.getSedimentLoads();
    }


    protected void parseWaterlevels() throws IOException {
        if (Config.INSTANCE.skipWaterlevels()) {
            log.info("skip parsing waterlevels");
            return;
        }

        log.info("Parse waterlevels");

        File minfo  = getMinfoDir();
        File fixDir = new File(minfo, MINFO_FIXATIONS_DIR);
        File wspDir = new File(fixDir, MINFO_WATERLEVELS_DIR);

        File[] files = wspDir.listFiles();

        if (files == null) {
            log.warn("Cannot read directory for wl '" + wspDir + "'");
            return;
        }

        WaterlevelParser parser = new WaterlevelParser();

        for (File file: files) {
            parser.parse(file);
        }

        // The parsed ImportWaterlevels are converted to
        // 'fixation'-wsts now.
        for(ImportWst iw: parser.getWaterlevels()) {
            iw.setDescription("CSV/" + iw.getDescription());
            iw.setKind(7);
            waterlevels.add(iw);
        }
    }

    protected void parseMeasurementStations() throws IOException {
        if (Config.INSTANCE.skipMeasurementStations()) {
            log.info("skip parsing measurement stations");
            return;
        }

        log.info("Parse measurement stations");

        File minfo = getMinfoDir();
        File minfoBaseDir = new File(minfo, MINFO_BASE_DIR);
        File coredataFile = new File(minfoBaseDir, MINFO_CORE_DATA_FILE);

        if (coredataFile == null || !coredataFile.exists()) {
            log.warn("No core data file '"
                + coredataFile.getAbsolutePath() + "' found");
            return;
        }

        MeasurementStationsParser parser = new MeasurementStationsParser();
        try {
            parser.parse(coredataFile);
            measurementStations = parser.getMeasurementStations();

            log.info("Successfully parsed " + measurementStations.size()
                + " measurement stations.");
        }
        catch (IOException ioe) {
            log.error("unable to parse file '" + coredataFile.getName() +
                ": " + ioe.getMessage());
        }
    }


    protected void parseWaterlevelDifferences() throws IOException {
        if (Config.INSTANCE.skipWaterlevelDifferences()) {
            log.info("skip parsing waterlevel differences");
            return;
        }

        log.info("Parse waterlevel differences");

        File minfo  = getMinfoDir();
        File fixDir = new File(minfo, MINFO_FIXATIONS_DIR);
        File diffDir = new File(fixDir, MINFO_WATERLEVEL_DIFF_DIR);

        File[] files = diffDir.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + diffDir + "'");
            return;
        }

        WaterlevelDifferencesParser parser = new WaterlevelDifferencesParser();

        for (File file: files) {
            parser.parse(file);
        }

        // WaterlevelDifferences become Wsts now.
        for(ImportWst iw: parser.getDifferences()) {
            iw.setDescription("CSV/" + iw.getDescription());
            iw.setKind(6);
            waterlevelDifferences.add(iw);
        }
    }


    protected void parseSQRelation() throws IOException {
        if (Config.INSTANCE.skipSQRelation()) {
            log.info("skip parsing sq relation");
            return;
        }

        log.info("Parse sq relations");

        File minfo = getMinfoDir();
        File sqDir = new File(minfo, MINFO_SQ_DIR);

        File[] files = sqDir.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + sqDir + "'");
            return;
        }

        SQRelationParser parser = new SQRelationParser(getPeer());

        for (File file: files) {
            parser.parse(file);
        }

        sqRelations = parser.getSQRelations();

        log.debug("Parsed " + sqRelations.size() + " SQ relations.");
    }


    protected void parseBedHeights(File dir) throws IOException {
        log.debug("Parse bed height singles");

        File[] files = dir.listFiles();

        if (files == null) {
            log.warn("Cannot read directory '" + dir + "'");
            return;
        }

        BedHeightParser parser = new BedHeightParser();

        for (File file: files) {
            parser.parse(file);
        }

        bedHeights = parser.getBedHeights();
    }

    public void parseFloodWater() throws IOException {
        if (Config.INSTANCE.skipFloodWater()) {
            log.info("skip parsing flod water");
            return;
        }

        log.info("Parse flood water wst file");

        File riverDir = wstFile.getParentFile().getParentFile();

        File dir = FileTools.repair(new File(riverDir, FLOOD_WATER));

        if (!dir.isDirectory() || !dir.canRead()) {
            log.info("no directory '" + dir + "' found");
            return;
        }

        File [] files = dir.listFiles();

        if (files == null) {
            log.warn("cannot read '" + dir + "'");
            return;
        }

        for (File file: files) {
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!(name.endsWith(".zus") || name.endsWith(".wst"))) {
                continue;
            }
            log.info("found file '" + file.getName() + "'");
            try {
                WstParser wstParser = new WstParser();
                wstParser.parse(file);
                ImportWst iw = wstParser.getWst();
                iw.setKind(4);
                iw.setDescription(FLOOD_WATER + "/" + iw.getDescription());
                floodWater.add(iw);
            }
            catch (WstParser.ParseException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void parseOfficialLines() throws IOException {
        if (Config.INSTANCE.skipOfficialLines()) {
            log.info("skip parsing official lines");
            return;
        }

        log.info("Parse official wst files");

        File riverDir = wstFile.getParentFile().getParentFile();

        for (String folder: OFFICIAL_LINES_FOLDERS) {
            File dir = FileTools.repair(new File(riverDir, folder));

            if (!dir.isDirectory() || !dir.canRead()) {
                log.info("no directory '" + folder + "' found");
                continue;
            }

            File file = FileTools.repair(new File(dir, OFFICIAL_LINES));
            if (!file.isFile() || !file.canRead()) {
                log.warn("no official lines wst file found");
                continue;
            }
            log.debug("Found WST file: " + file);

            ImportWst iw = new ImportWst(
                ImportOfficialWstColumn.COLUMN_FACTORY);

            WstParser wstParser = new WstParser(iw);
            try {
                wstParser.parse(file);
            }
            catch (WstParser.ParseException e) {
                log.error(e.getMessage());
                continue;
            }

            iw.setKind(3);
            iw.setDescription(folder + "/" + iw.getDescription());

            File configFile = FileTools.repair(
                new File(dir, OFFICIAL_LINES_CONFIG));
            if (!configFile.isFile() || !configFile.canRead()) {
                log.warn("no config file for official lines found");
            }
            else {
                OfficialLinesConfigParser olcp =
                    new OfficialLinesConfigParser();
                try {
                    olcp.parse(configFile);
                }
                catch (IOException ioe) {
                    log.warn("Error reading offical lines config", ioe);
                }
                List<String> mainValueNames = olcp.getMainValueNames();
                if (mainValueNames.isEmpty()) {
                    log.warn(
                        "config file for offical lines contains no entries");
                }
                else {
                    // Join as much as possible.
                    Iterator<ImportWstColumn> wi = iw.getColumns().iterator();
                    Iterator<String> si = olcp.getMainValueNames().iterator();
                    while (wi.hasNext() && si.hasNext()) {
                        ImportOfficialWstColumn wc =
                            (ImportOfficialWstColumn)wi.next();
                        String name = si.next();
                        ImportOfficialLine iol =
                            new ImportOfficialLine(name, wc);
                        wc.setOfficialLine(iol);
                    }
                }
            }

            officialLines.add(iw);
        } // for all folders

    }

    public void parseFixations() throws IOException {
        if (Config.INSTANCE.skipFixations()) {
            log.info("skip parsing fixations");
            return;
        }

        log.info("Parse fixation wst files");

        File riverDir = wstFile.getParentFile().getParentFile();

        File fixDir = FileTools.repair(
            new File(riverDir, FIXATIONS));

        if (!fixDir.isDirectory() || !fixDir.canRead()) {
            log.info("no fixation wst file directory found");
            return;
        }

        File [] files = fixDir.listFiles();

        if (files == null) {
            log.warn("cannot read fixations wst file directory");
            return;
        }

        for (File file: files) {
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".wst")) {
                continue;
            }
            log.debug("Found WST file: " + file);

            try {
                WstParser wstParser = new WstParser();
                wstParser.parse(file);
                ImportWst iw = wstParser.getWst();
                iw.setKind(2);
                iw.setDescription(FIXATIONS+ "/" + iw.getDescription());
                fixations.add(iw);
            }
            catch (WstParser.ParseException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void parseExtraWsts() throws IOException {
        if (Config.INSTANCE.skipExtraWsts()) {
            log.info("skip parsing extra WST files");
            return;
        }

        log.info("Parse extra longitudinal wst files");

        File riverDir = wstFile.getParentFile().getParentFile();

        File extraDir = FileTools.repair(
            new File(riverDir, EXTRA_LONGITUDINALS));

        if (!extraDir.isDirectory() || !extraDir.canRead()) {
            log.info("no extra longitudinal wst file directory found");
            return;
        }

        File [] files = extraDir.listFiles();

        if (files == null) {
            log.warn("cannot read extra longitudinal wst file directory");
            return;
        }

        for (File file: files) {
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!(name.endsWith(".zus") || name.endsWith(".wst"))) {
                continue;
            }
            log.debug("Found WST file: " + file);

            try {
                WstParser wstParser = new WstParser();
                wstParser.parse(file);
                ImportWst iw = wstParser.getWst();
                iw.setKind(1);
                iw.setDescription(
                    EXTRA_LONGITUDINALS + "/" + iw.getDescription());
                extraWsts.add(iw);
            }
            catch (WstParser.ParseException e) {
                log.error(e.getMessage());
            }
        }

    }

    public void parseWst() throws IOException {
        if (Config.INSTANCE.skipWst()) {
            log.info("skip parsing WST file");
            return;
        }

        WstParser wstParser = new WstParser();
        try {
            wstParser.parse(wstFile);
            wst = wstParser.getWst();
            wst.setKmUp(wst.guessWaterLevelIncreasing());
        }
        catch (WstParser.ParseException e) {
            log.error(e.getMessage());
        }
    }

    public void parseGauges() throws IOException {
        if (Config.INSTANCE.skipGauges()) {
            log.info("skip parsing gauges");
            return;
        }

        File gltFile = new File(wstFile.getParentFile(), PEGEL_GLT);
        gltFile = FileTools.repair(gltFile);

        if (!gltFile.isFile() || !gltFile.canRead()) {
            log.warn("cannot read gauges from '" + gltFile + "'");
            return;
        }

        PegelGltParser pgltp = new PegelGltParser();
        pgltp.parse(gltFile);

        gauges = pgltp.getGauges();

        for (ImportGauge gauge: gauges) {
            gauge.parseDependencies();
        }
    }

    public void parseAnnotations() throws IOException {
        if (Config.INSTANCE.skipAnnotations()) {
            log.info("skip parsing annotations");
            return;
        }

        File riverDir = wstFile.getParentFile().getParentFile();
        AnnotationsParser aparser =
            new AnnotationsParser(annotationClassifier);
        aparser.parse(riverDir);

        annotations = aparser.getAnnotations();
    }

    public void parseHYKs() {
        if (Config.INSTANCE.skipHYKs()) {
            log.info("skip parsing HYK files");
            return;
        }

        log.info("looking for HYK files");
        HYKParser parser = new HYKParser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        parser.parseHYKs(riverDir, new HYKParser.Callback() {

            Set<HashedFile> hfs = new HashSet<HashedFile>();

            @Override
            public boolean hykAccept(File file) {
                HashedFile hf = new HashedFile(file);
                boolean success = hfs.add(hf);
                if (!success) {
                    log.warn("HYK file '" + file
                        + "' seems to be a duplicate.");
                }
                return success;
            }

            @Override
            public void hykParsed(HYKParser parser) {
                log.debug("callback from HYK parser");
                ImportHYK hyk = parser.getHYK();
                hyk.setRiver(ImportRiver.this);
                hyks.add(hyk);
            }
        });
    }


    /** Create a W80 Parser and parse w80 files found. */
    public void parseW80s() {
        if (Config.INSTANCE.skipW80s()) {
            log.info("skip parsing W80s");
            return;
        }
        W80Parser parser = new W80Parser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        ImportRiverCrossSectionParserCallback w80Callback =
            new ImportRiverCrossSectionParserCallback("w80");
        parser.parseW80s(riverDir, w80Callback);
    }

    /** Create a W80 Parser and parse w80 files found. */
    public void parseW80CSVs() {
        if (Config.INSTANCE.skipW80CSVs()) {
            log.info("skip parsing W80 csvs");
            return;
        }
        W80CSVParser parser = new W80CSVParser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        // Construct the Cross-Section-Data path.
        File csDir = new File(riverDir.getPath()
            + File.separator + "Geodaesie"
            + File.separator + "Querprofile"
            + File.separator + "QP-Daten");

        ImportRiverCrossSectionParserCallback w80CSVCallback =
            new ImportRiverCrossSectionParserCallback("w80-csv");
        parser.parseW80CSVs(csDir, w80CSVCallback);
    }


    /**
     * Create and use a DA50Parser, parse the files found, add the
     * ross-sections found.
     */
    public void parseDA50s() {
        if (Config.INSTANCE.skipDA50s()) {
            log.info("skip parsing DA50s");
            return;
        }
        DA50Parser parser = new DA50Parser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        ImportRiverCrossSectionParserCallback da50Callback =
            new ImportRiverCrossSectionParserCallback("da50");

        parser.parseDA50s(riverDir, da50Callback);
    }


    /** Create a DA66 Parser and parse the da66 files found. */
    // TODO this is a copy of parsePRFs, extract interfaces
    //(e.g. CrossSectionParser).
    public void parseDA66s() {
        if (Config.INSTANCE.skipDA66s()) {
            log.info("skip parsing DA66s");
            return;
        }

        log.info("looking for DA66 files");
        DA66Parser parser = new DA66Parser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        ImportRiverCrossSectionParserCallback da66Callback =
            new ImportRiverCrossSectionParserCallback("da66");

        parser.parseDA66s(riverDir, da66Callback);
    }

    /** Create a PRFParser and let it parse the prf files found. */
    public void parsePRFs() {
        if (Config.INSTANCE.skipPRFs()) {
            log.info("skip parsing PRFs");
            return;
        }

        log.info("looking for PRF files");
        PRFParser parser = new PRFParser();
        File riverDir = wstFile
            .getParentFile()  // Basisdaten
            .getParentFile()  // Hydrologie
            .getParentFile(); // <river>

        ImportRiverCrossSectionParserCallback prfCallback =
            new ImportRiverCrossSectionParserCallback("prf");
        parser.parsePRFs(riverDir, prfCallback);
    }

    public static Date yearToDate(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, 5, 15, 12, 0, 0);
        long ms = cal.getTimeInMillis();
        cal.setTimeInMillis(ms - ms%1000);
        return cal.getTime();
    }

    public void storeDependencies() {
        /* test whether river is already in database.
         * Otherwise it makes no sense to skip waterlevel model WST-file
         * because the altitude reference is taken from there. */
        Session session = ImporterSession.getInstance().getDatabaseSession();
        Query query = session.createQuery("from River where name=:name");
        query.setString("name", name);
        List<River> rivers = query.list();
        if (rivers.isEmpty() && Config.INSTANCE.skipWst()){
            log.error("River not yet in database. "
                + "You cannot skip importing waterlevel model.");
            return;
        }

        storeWstUnit();
        storeAnnotations();
        storeHYKs();
        storeCrossSections();
        storeGauges();
        storeWst();
        storeExtraWsts();
        storeFixations();
        storeOfficialLines();
        storeFloodWater();
        storeFloodProtection();
        storeMeasurementStations();
        storeBedHeight();
        storeSedimentDensity();
        storePorosity();
        storeMorphologicalWidth();
        storeFlowVelocity();
        storeSedimentLoadLS();
        storeSedimentLoad();
        storeWaterlevels();
        storeWaterlevelDifferences();
        storeSQRelations();
        storeOfficialNumber();
    }

    public void storeWstUnit() {
        if (wst == null) {
            log.warn("No unit given. "
                + "Waterlevel-model WST-file has to be imported already.");
        }
        else {
            wstUnit = wst.getUnit();
        }
    }

    public void storeHYKs() {
        if (!Config.INSTANCE.skipHYKs()) {
            log.info("store HYKs");
            getPeer();
            for (ImportHYK hyk: hyks) {
                hyk.storeDependencies();
            }
        }
    }

    public void storeCrossSections() {
        if (!Config.INSTANCE.skipPRFs()
            || !Config.INSTANCE.skipDA66s()
            || !Config.INSTANCE.skipDA50s()
            || !Config.INSTANCE.skipW80s()
            || !Config.INSTANCE.skipW80CSVs()) {
            log.info("store cross sections");
            getPeer();
            for (ImportCrossSection crossSection: crossSections) {
                crossSection.storeDependencies();
            }
        }
    }

    public void storeWst() {
        if (wst != null && !Config.INSTANCE.skipWst()) {
            River river = getPeer();
            wst.storeDependencies(river);

            // The flow direction of the main wst and the corresponding
            // waterlevels determine if the river is 'km_up'.
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            river.setKmUp(wst.getKmUp());
            session.save(river);
        }
    }

    public void storeFixations() {
        if (!Config.INSTANCE.skipFixations()) {
            log.info("store fixation wsts");
            River river = getPeer();
            for (ImportWst fWst: fixations) {
                log.debug("Fixation name: " + fWst.getDescription());
                fWst.storeDependencies(river);
            }
        }
    }


    /** Store wsts from waterlevel-csv files. */
    public void storeWaterlevels() {
        if (!Config.INSTANCE.skipWaterlevels())

        log.info("store waterlevel wsts from csv");
        River river = getPeer();
        for (ImportWst wWst: waterlevels) {
            log.debug("Waterlevel name: " + wWst.getDescription());
            wWst.storeDependencies(river);
        }
    }


    /** Store wsts from waterleveldifference-csv files. */
    public void storeWaterlevelDifferences() {
        if (!Config.INSTANCE.skipWaterlevelDifferences())

        log.info("store waterleveldifferences wsts from csv");
        River river = getPeer();
        for (ImportWst dWst: waterlevelDifferences) {
            log.debug("water.diff.: name " + dWst.getDescription());
            dWst.storeDependencies(river);
        }
    }


    public void storeExtraWsts() {
        if (!Config.INSTANCE.skipExtraWsts()) {
            log.info("store extra wsts");
            River river = getPeer();
            for (ImportWst wst: extraWsts) {
                log.debug("name: " + wst.getDescription());
                wst.storeDependencies(river);
            }
        }
    }

    public void storeOfficialLines() {
        if (Config.INSTANCE.skipOfficialLines() || officialLines.isEmpty()) {
            return;
        }

        log.info("store official lines wsts");
        River river = getPeer();
        for (ImportWst wst: officialLines) {
            log.debug("name: " + wst.getDescription());
            wst.storeDependencies(river);

            // Store the official lines after the columns are store.
            for (ImportWstColumn wc: wst.getColumns()) {
                ImportOfficialWstColumn owc = (ImportOfficialWstColumn)wc;
                ImportOfficialLine ioc = owc.getOfficialLine();
                if (ioc != null) {
                    if (ioc.getPeer(river) == null) {
                        log.warn("Cannot store official line: "
                            + ioc.getName());
                    }
                }
            }
        }
    }

    public void storeFloodWater() {
        if (!Config.INSTANCE.skipFloodWater()) {
            log.info("store flood water wsts");
            River river = getPeer();
            for (ImportWst wst: floodWater) {
                log.debug("name: " + wst.getDescription());
                wst.storeDependencies(river);
            }
        }
    }


    public void storeFloodProtection() {
        if (!Config.INSTANCE.skipFloodProtection()) {
            log.info("store flood protection wsts");
            River river = getPeer();
            for (ImportWst wst: floodProtection) {
                log.debug("name: " + wst.getDescription());
                wst.storeDependencies(river);
            }
        }
    }


    public void storeBedHeight() {
        if (!Config.INSTANCE.skipBedHeight()) {
            log.info("store bed heights");
            River river = getPeer();

            if (bedHeights != null) {
                for (ImportBedHeight tmp: bedHeights) {
                    ImportBedHeight single = (ImportBedHeight) tmp;

                    String desc = single.getDescription();

                    log.debug("name: " + desc);

                    single.storeDependencies(river);
                }
            }
            else {
                log.info("No bed heights to store.");
            }
        }
    }


    public void storeSedimentDensity() {
        if (!Config.INSTANCE.skipSedimentDensity()) {
            log.info("store sediment density");

            River river = getPeer();

            for (ImportSedimentDensity density: sedimentDensities) {
                String desc = density.getDescription();

                log.debug("name: " + desc);

                density.storeDependencies(river);
            }
        }
    }

    public void storePorosity() {
        if (!Config.INSTANCE.skipPorosity()) {
            log.info("store porosity");

            River river = getPeer();

            for (ImportPorosity porosity: porosities) {
                String desc = porosity.getDescription();

                log.debug("name: " + desc);

                porosity.storeDependencies(river);
            }
        }
    }

    public void storeMorphologicalWidth() {
        if (!Config.INSTANCE.skipMorphologicalWidth()) {
            log.info("store morphological width");

            River river = getPeer();

            for (ImportMorphWidth width: morphologicalWidths) {
                width.storeDependencies(river);
            }
        }
    }

    public void storeFlowVelocity() {
        if (!Config.INSTANCE.skipFlowVelocity()) {
            log.info("store flow velocity");

            River river = getPeer();

            for (ImportFlowVelocityModel flowVelocityModel: flowVelocityModels
            ) {
                flowVelocityModel.storeDependencies(river);
            }

            for (ImportFlowVelocityMeasurement m: flowVelocityMeasurements) {
                m.storeDependencies(river);
            }
        }
    }


    public void storeSedimentLoadLS() {
        if (!Config.INSTANCE.skipSedimentLoadLS()) {
            log.info("store sediment load longitudinal section data");

            River river = getPeer();

            for (ImportSedimentLoadLS sedimentLoadLS: sedimentLoadLSs) {
                sedimentLoadLS.storeDependencies(river);
            }
        }
    }


    public void storeSedimentLoad() {
        if (!Config.INSTANCE.skipSedimentLoad()) {
            log.info("store sediment load data at measurement stations");

            for (ImportSedimentLoad sedimentLoad: sedimentLoads) {
                sedimentLoad.storeDependencies();
            }
        }
    }


    public void storeMeasurementStations() {
        if (!Config.INSTANCE.skipMeasurementStations()) {
            log.info("store measurement stations");

            River river = getPeer();

            int count = 0;

            for (ImportMeasurementStation station: measurementStations) {
                boolean success = station.storeDependencies(river);
                if (success) {
                    count++;
                }
            }

            log.info("stored " + count + " measurement stations.");
        }
    }


    public void storeSQRelations() {
        if (!Config.INSTANCE.skipSQRelation()) {
            log.info("store sq relations");

            int count = 0;

            for (ImportSQRelation sqRelation: sqRelations) {
                sqRelation.storeDependencies();
                count++;
            }

            log.info("stored " + count + " sq relations.");
        }
    }


    public void storeAnnotations() {
        if (!Config.INSTANCE.skipAnnotations()) {
            River river = getPeer();
            for (ImportAnnotation annotation: annotations) {
                annotation.getPeer(river);
            }
        }
    }

    public void storeGauges() {
        if (!Config.INSTANCE.skipGauges()) {
            log.info("store gauges:");
            River river = getPeer();
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            for (ImportGauge gauge: gauges) {
                log.info("\tgauge: " + gauge.getName());
                gauge.storeDependencies(river);
                ImporterSession.getInstance().getDatabaseSession();
                session.flush();
            }
        }
    }

    public River getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            Query query = session.createQuery("from River where name=:name");

            Unit u = null;
            if (wstUnit != null) {
                u = wstUnit.getPeer();
            }

            query.setString("name", name);
            List<River> rivers = query.list();
            if (rivers.isEmpty()) {
                log.info("Store new river '" + name + "'");
                peer = new River(name, u, modelUuid);
                if (!Config.INSTANCE.skipBWASTR()) {
                    peer.setOfficialNumber(officialNumber);
                }
                session.save(peer);
            }
            else {
                peer = rivers.get(0);
            }
        }
        return peer;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
