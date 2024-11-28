/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.velocity.Template;
import org.apache.commons.io.FileUtils;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.StaticWKmsArtifact;
import org.dive4elements.river.artifacts.access.DGMAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.model.CalculationMessage;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.LayerInfo;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.fixings.FixRealizingResult;
import org.dive4elements.river.artifacts.model.map.HWS;
import org.dive4elements.river.artifacts.model.map.HWSContainer;
import org.dive4elements.river.artifacts.model.map.HWSFactory;
import org.dive4elements.river.artifacts.model.map.WMSLayerFacet;
import org.dive4elements.river.artifacts.model.map.WSPLGENCalculation;
import org.dive4elements.river.artifacts.model.map.WSPLGENJob;
import org.dive4elements.river.artifacts.model.map.WSPLGENReportFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.WstWriter;
import org.dive4elements.river.model.CrossSectionTrack;
import org.dive4elements.river.model.DGM;
import org.dive4elements.river.model.Floodplain;
import org.dive4elements.river.model.RiverAxis;
import org.dive4elements.river.utils.ArtifactMapfileGenerator;
import org.dive4elements.river.utils.GeometryUtils;
import org.dive4elements.river.utils.MapfileGenerator;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.wsplgen.FacetCreator;
import org.dive4elements.river.wsplgen.JobObserver;
import org.dive4elements.river.wsplgen.Scheduler;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.hibernate.HibernateException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class FloodMapState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(FloodMapState.class);


    public static final String KEEP_ARTIFACT_DIR =
        System.getProperty("flys.uesk.keep.artifactsdir", "false");


    public static final String OUTPUT_NAME = "floodmap";

    public static final String WSP_ARTIFACT = "wsp";

    public static final String WINFO_WSP_STATE_ID = "state.winfo.waterlevel";

    public static final String WSPLGEN_PARAMETER_FILE = "wsplgen.par";
    public static final String WSPLGEN_BARRIERS_LINES = "barrier_lines.shp";
    public static final String WSPLGEN_BARRIERS_POLY  = "barrier_polygons.shp";
    public static final String WSPLGEN_AXIS           = "axis.shp";
    public static final String WSPLGEN_QPS            = "qps.shp";
    public static final String WSPLGEN_FLOODPLAIN     = "talaue.shp";
    public static final String WSPLGEN_WSP_FILE       = "waterlevel.wst";
    public static final String WSPLGEN_OUTPUT_FILE    = "wsplgen.shp";
    public static final String WSPLGEN_USER_RGD_SHAPE = "user-rgd.shp";
    public static final String WSPLGEN_USER_RGD_ZIP   = "user-rgd.zip";
    public static final String WSPLGEN_USER_RGD       = "user-rgd";

    public static final String WSPLGEN_QPS_NAME = "qps";

    public static final int WSPLGEN_DEFAULT_OUTPUT = 0;

    private static final String HWS_LINES_SHAPE = "hws-lines.shp";

    private static final String I18N_HWS_POINTS_OFFICIAL =
        "floodmap.hws.points.official";
    private static final String I18N_HWS_LINES_OFFICIAL =
        "floodmap.hws.lines.official";
    private static final String HWS_LINES = "hws-lines";
    private static final String HWS_POINT_SHAPE = "hws-points.shp";
    private static final String HWS_POINTS = "hws-points";


    /* List of possible map files this state handles . */
    private static final String[] POSSIBLE_ADDITIONAL_MAPFILES = {
        HWS_LINES,
        HWS_POINTS,
        WSPLGEN_USER_RGD,
        "barriers-lines",
        "barriers-poly"};



    /**
     * @param orig
     * @param owner
     * @param context
     * @param callMeta
     */
    @Override
    public void initialize(
        Artifact orig,
        Artifact owner,
        Object   context,
        CallMeta callMeta
    ) {
        log.info("Initialize State with Artifact: " + orig.identifier());

        copyAndFixShapeDir(orig, owner);
        modifyFacets(orig, owner, context, callMeta);

        ArtifactMapfileGenerator amfg = new ArtifactMapfileGenerator();
        try {
            amfg.generate();
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


    protected void copyAndFixShapeDir(Artifact orig, Artifact owner) {
        /* We have a lot of generated mapfiles in here possibly.
         * They use the orig artificact identifier. That has to
         * be fixed.*/
        File origDir = getDirectory((D4EArtifact) orig);
        File thisDir = getDirectory((D4EArtifact) owner);

        FileTools.copyDirectory(origDir, thisDir);

        for (String candidate: POSSIBLE_ADDITIONAL_MAPFILES) {
            File f = new File(
                thisDir, MapfileGenerator.MS_LAYER_PREFIX + candidate);
            if (f.exists()) {
                log.debug("Fixing artifiact id's in: " + f);
                try {
                    String content = FileUtils.readFileToString(f);
                    FileUtils.writeStringToFile(f,
                        content.replaceAll(
                            orig.identifier(), owner.identifier()));
                } catch (IOException e) {
                    log.error("Failed to rewrite file: " + f
                        + " Error: " + e.getMessage());
                }
            }
        }
    }


    protected void modifyFacets(
        Artifact orig,
        Artifact owner,
        Object   context,
        CallMeta callMeta
    ) {
        D4EArtifact flys  = (D4EArtifact) owner;
        List<Facet> facets = flys.getFacets();
        if (facets == null || facets.isEmpty()) {
            log.warn("No facets for '" + OUTPUT_NAME + "' given!");
            return;
        }

        for (Facet facet: facets) {
            if (facet instanceof WMSLayerFacet) {
                WMSLayerFacet wms = (WMSLayerFacet) facet;

                List<String> layers = wms.getLayers();

                for (String layer: layers) {
                    log.debug("Have layer: " + layer);
                    if (layer.contains(orig.identifier())) {
                        wms.removeLayer(layer);

                        String newLayer = layer.replace(
                            orig.identifier(), owner.identifier());

                        wms.addLayer(newLayer);

                        log.debug(
                            "Replaced layer: " + layer + " with " + newLayer);
                    }
                }
            }
        }
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("FloodMapState.computeAdvance");

        File artifactDir = getDirectory(artifact);

        if (artifactDir == null) {
            log.error("Could not create directory for WSPLGEN results!");
            return null;
        }

        WSPLGENCalculation calculation = new WSPLGENCalculation();

        FacetCreator facetCreator = new FacetCreator(
            artifact, context, hash, getID(), facets);

        WSPLGENJob job = prepareWSPLGENJob(
            artifact,
            facetCreator,
            artifactDir,
            context,
            calculation);

        CalculationResult  res   = new CalculationResult(null, calculation);
        WSPLGENReportFacet report= new WSPLGENReportFacet(
            ComputeType.ADVANCE, hash, getID(), res);

        facets.add(report);

        if (job == null) {
            if (KEEP_ARTIFACT_DIR.equals("false")) {
                removeDirectory(artifact);
            }

            calculation.addError(-1, Resources.getMsg(
                context.getMeta(),
                "wsplgen.job.error",
                "wsplgen.job.error"));

            log.error("No WSPLGEN processing has been started!");

            return null;
        }

        context.afterCall(CallContext.BACKGROUND);
        context.addBackgroundMessage(new CalculationMessage(
            JobObserver.STEPS.length,
            0,
            Resources.getMsg(
                context.getMeta(),
                "wsplgen.job.queued",
                "wsplgen.job.queued")
        ));

        GlobalContext gc    = (GlobalContext) context.globalContext();
        Scheduler scheduler = (Scheduler) gc.get(RiverContext.SCHEDULER);
        scheduler.addJob(job);

        return null;
    }


    /**
     * Returns (and creates if not existing) the directory for storing WSPLEN
     * data for the owner artifact.
     *
     * @param artifact The owner Artifact.
     *
     * @return the directory for WSPLEN data.
     */
    protected File getDirectory(D4EArtifact artifact) {
        String shapePath = RiverUtils.getXPathString(
            RiverUtils.XPATH_MAPFILES_PATH);

        File artifactDir = FileTools.getDirectory(
            shapePath, artifact.identifier());

        return artifactDir;
    }


    /**
     * Removes directory and all its content where the required data and the
     * results of WSPLGEN are stored. Should be called in endOfLife().
     */
    protected void removeDirectory(D4EArtifact artifact) {
        String shapePath = RiverUtils.getXPathString(
            RiverUtils.XPATH_MAPFILES_PATH);

        File artifactDir = new File(shapePath, artifact.identifier());

        if (artifactDir.exists()) {
            log.info("Delete directory: " + artifactDir.getAbsolutePath());
            if (!FileTools.deleteRecursive(artifactDir)) {
                log.warn("Could not delete directory: "
                        + artifactDir.getAbsolutePath());
            }
        }
        else {
            log.debug("There is no directory to remove.");
        }
    }


    @Override
    public void endOfLife(Artifact artifact, Object callContext) {
        log.info("FloodMapState.endOfLife: " + artifact.identifier());

        D4EArtifact flys = (D4EArtifact) artifact;

        Scheduler scheduler = Scheduler.getInstance();
        scheduler.cancelJob(flys.identifier());
    }


    protected WSPLGENJob prepareWSPLGENJob(
        D4EArtifact       artifact,
        FacetCreator       facetCreator,
        File               artifactDir,
        CallContext        context,
        WSPLGENCalculation calculation
    ) {
        log.debug("FloodMapState.prepareWSPLGENJob");
        String scenario = artifact.getDataAsString("scenario");

        WSPLGENJob job = new WSPLGENJob(
            artifact,
            artifactDir,
            facetCreator,
            context,
            calculation);

        File paraFile = new File(artifactDir, WSPLGEN_PARAMETER_FILE);

        setOut(artifact, job);
        setRange(artifact, job);
        setDelta(artifact, job);
        setGel(artifact, job);
        setDist(artifact, job);
        setAxis(artifact, artifactDir, job);
        setPro(artifact, artifactDir, job);
        setDgm(artifact, job, context);
        setArea(artifact, artifactDir, job);
        setOutFile(artifact, job);
        setWsp(artifact, context, artifactDir, job);    // WSP
        if (scenario.equals("scenario.current")) {
            setOfficialHWS(artifact, facetCreator, artifactDir, job);
        }
        else if (scenario.equals("scenario.scenario")) {
            setAdditionalHWS(artifact, facetCreator, artifactDir, job);
            setLine(artifact, facetCreator, artifactDir, job);
            setUserShape(artifact, facetCreator, artifactDir, job);
        }
        // TODO
        // setWspTag(artifact, job);

        facetCreator.createExportFacet("zip");
        try {
            job.toFile(paraFile);

            return job;
        }
        catch (IOException ioe) {
            log.warn("Cannot write PAR file: " + ioe.getMessage());
        }
        catch (IllegalArgumentException iae) {
            log.warn("Cannot write PAR file: " + iae.getMessage());
        }

        return null;
    }


    private void setAdditionalHWS(
        D4EArtifact artifact,
        FacetCreator facetCreator,
        File dir,
        WSPLGENJob job) {
        File line = new File(dir, HWS_LINES_SHAPE);
        boolean lines = line.exists();
        log.debug("shp file exists: " + lines);
        if (lines) {
            job.addLin(dir + "/" + HWS_LINES_SHAPE);
            facetCreator.createShapeFacet(I18N_HWS_LINES_OFFICIAL,
                MapfileGenerator.MS_LAYER_PREFIX + HWS_LINES,
                FLOODMAP_LINES, 2);
        }
        File point = new File(dir, HWS_POINT_SHAPE);
        boolean points = point.exists();
        log.debug("shp file exists: " + points);
        if (points) {
            facetCreator.createShapeFacet(I18N_HWS_POINTS_OFFICIAL,
                MapfileGenerator.MS_LAYER_PREFIX + HWS_POINTS,
                FLOODMAP_FIXPOINTS, 3);
        }
    }


    private void setOfficialHWS(
        D4EArtifact artifact,
        FacetCreator facetCreator,
        File artifactDir,
        WSPLGENJob job) {
        String river = artifact.getDataAsString("river");

        HWSContainer hwsLines = HWSFactory.getHWSLines(river);
        List<HWS> selectedLines = hwsLines.getOfficialHWS();

        FeatureCollection collectionLines = FeatureCollections.newCollection();
        SimpleFeatureType lineType = null;
        for (HWS h : selectedLines) {
            lineType = h.getFeatureType();
            collectionLines.add(h.getFeature());
        }
        boolean successLines = false;
        if (lineType != null && collectionLines.size() > 0) {
            File shapeLines = new File(artifactDir, HWS_LINES_SHAPE);
            successLines = GeometryUtils.writeShapefile(
                shapeLines, lineType, collectionLines);
        }
        if (successLines) {
            createMapfile(
                artifact,
                artifactDir,
                MapfileGenerator.MS_LAYER_PREFIX + HWS_LINES,
                HWS_LINES_SHAPE,
                "LINE",
                "31467", // XXX: should be dynamically fetched from database.
                "hws");
            job.addLin(artifactDir + "/" + HWS_LINES_SHAPE);
            facetCreator.createShapeFacet(I18N_HWS_LINES_OFFICIAL,
                MapfileGenerator.MS_LAYER_PREFIX + HWS_LINES,
                FLOODMAP_HWS_LINES,2);
        }
        else log.warn("no lines written");
    }


    public static void createMapfile(
        D4EArtifact artifact,
        File artifactDir,
        String name,
        String hwsShapefile,
        String type,
        String srid,
        String group
    ) {
        LayerInfo info = new LayerInfo();
        info.setName(name + artifact.identifier());
        info.setType(type);
        info.setDirectory(artifact.identifier());
        info.setTitle(name);
        info.setData(hwsShapefile);
        info.setSrid(srid);
        info.setGroupTitle(group);
        info.setGroup(group + artifact.identifier());
        MapfileGenerator generator = new ArtifactMapfileGenerator();
        Template tpl = generator.getTemplateByName(
            MapfileGenerator.SHP_LAYER_TEMPLATE);
        try {
            File layer = new File(artifactDir.getCanonicalPath() + "/" + name);
            generator.writeLayer(info, layer, tpl);
            List<String> layers = new ArrayList<String>();
            layers.add(layer.getAbsolutePath());
            generator.generate();
        }
        catch(FileNotFoundException fnfe) {
            log.warn("Could not find mapfile for hws layer");
        }
        catch (Exception ioe) {
            log.warn("Could not create mapfile for hws layer");
            log.warn(Arrays.toString(ioe.getStackTrace()));
        }
    }


    protected void setOut(D4EArtifact artifact, WSPLGENJob job) {
        job.setOut(WSPLGEN_DEFAULT_OUTPUT);
    }


    protected void setRange(D4EArtifact artifact, WSPLGENJob job) {
        RangeAccess rangeAccess = new RangeAccess(artifact);
        double[] range = rangeAccess.getKmRange();

        job.setStart(range[0]);
        job.setEnd(range[1]);
    }


    protected void setDelta(D4EArtifact artifact, WSPLGENJob job) {
        String from = artifact.getDataAsString("diff_from");
        String to   = artifact.getDataAsString("diff_to");
        String diff = artifact.getDataAsString("diff_diff");

        try {
            job.setFrom(Double.parseDouble(from));
        }
        catch (NumberFormatException nfe) {
        }

        try {
            job.setTo(Double.parseDouble(to));
        }
        catch (NumberFormatException nfe) {
        }

        try {
            job.setDiff(Double.parseDouble(diff));
        }
        catch (NumberFormatException nfe) {
        }
    }


    protected void setGel(D4EArtifact artifact, WSPLGENJob job) {
        String gel = artifact.getDataAsString("scenario");

        log.debug("Selected gel = '" + gel + "'");

        if (gel == null || gel.length() == 0) {
            job.setGel(WSPLGENJob.GEL_NOSPERRE);
        }
        else if (gel.equals("scenario.current")) {
            job.setGel(WSPLGENJob.GEL_SPERRE);
        }
        else if (gel.equals("scenario.scenario")) {
            job.setGel(WSPLGENJob.GEL_SPERRE);
        }
        else {
            job.setGel(WSPLGENJob.GEL_NOSPERRE);
        }
    }


    protected void setDist(D4EArtifact artifact, WSPLGENJob job) {
        String dist = artifact.getDataAsString("profile_distance");

        try {
            job.setDist(Double.parseDouble(dist));
        }
        catch (NumberFormatException nfe) {
            // nothing to do here
        }
    }


    protected void setLine(
        D4EArtifact artifact,
        FacetCreator facetCreator,
        File         dir,
        WSPLGENJob   job
    ) {
        DGMAccess access = new DGMAccess(artifact);
        String geoJSON   = access.getGeoJSON();

        if (geoJSON == null || geoJSON.length() == 0) {
            log.debug("No barrier features in parameterization existing.");
            return;
        }

        String srid = String.valueOf(access.getDGM().getSrid());

        String srs = "EPSG:" + srid;

        SimpleFeatureType ft = getBarriersFeatureType(
            "barriers", srs, Geometry.class);

        List<SimpleFeature> features = GeometryUtils.parseGeoJSON(geoJSON, ft);
        if (features == null || features.isEmpty()) {
            log.debug("No barrier features extracted.");
            return;
        }

        FeatureCollection[] fcs = splitLinesAndPolygons(features);

        File shapeLines = new File(dir, WSPLGEN_BARRIERS_LINES);
        File shapePolys = new File(dir, WSPLGEN_BARRIERS_POLY);

        Object[][] obj = new Object[][] {
            new Object[] { "typ", String.class }
        };

        String scenario = job.getGel();

        boolean l = GeometryUtils.writeShapefile(
            shapeLines,
            GeometryUtils.buildFeatureType(
                "lines", srs, LineString.class, obj),
            fcs[0]);

        if (l) {
            log.debug(
                "Successfully created barrier line shapefile. " +
                "Write shapefile path into WSPLGEN job.");
            createMapfile(
                artifact,
                dir,
                MapfileGenerator.MS_LAYER_PREFIX + "barriers-lines",
                WSPLGEN_BARRIERS_LINES,
                "LINE",
                srid,
                MapfileGenerator.MS_BARRIERS_PREFIX);

            if (scenario.equals(WSPLGENJob.GEL_NOSPERRE)) {
                log.debug("WSPLGEN will not use barrier features.");
            }
            else {
                job.addLin(shapeLines.getAbsolutePath());
            }
        }

        boolean p = GeometryUtils.writeShapefile(
            shapePolys,
            GeometryUtils.buildFeatureType(
                "polygons", srs, Polygon.class, obj),
            fcs[1]);


        if (p) {
            log.debug(
                "Successfully created barrier polygon shapefile. " +
                "Write shapefile path into WSPLGEN job.");
            createMapfile(
                artifact,
                dir,
                MapfileGenerator.MS_LAYER_PREFIX + "barriers-poly",
                WSPLGEN_BARRIERS_POLY,
                "POLYGON",
                srid,
                MapfileGenerator.MS_BARRIERS_PREFIX);

            if (scenario.equals(WSPLGENJob.GEL_NOSPERRE)) {
                log.debug("WSPLGEN will not use barrier features.");
            }
            else {
                job.addLin(shapePolys.getAbsolutePath());
            }
        }

        if (p || l) {
            facetCreator.createBarrierFacet();
        }
    }


    protected void setUserShape(
        D4EArtifact artifact,
        FacetCreator facetCreator,
        File         dir,
        WSPLGENJob   job
    ) {
        File archive = new File(dir, WSPLGEN_USER_RGD_SHAPE);
        boolean exists = archive.exists();
        log.debug("shp file exists: " + exists);
        if (exists) {
            job.addLin(dir + "/" + WSPLGEN_USER_RGD_SHAPE);
            facetCreator.createShapeFacet(FacetCreator.I18N_USERSHAPE,
                MapfileGenerator.MS_LAYER_PREFIX + WSPLGEN_USER_RGD,
                FLOODMAP_USERSHAPE,
                4);
        }
    }

    protected SimpleFeatureType getBarriersFeatureType(
        String name,
        String srs,
        Class  type
    ) {
        Object[][] attrs = new Object[3][];
        attrs[0] = new Object[] { "typ", String.class };
        attrs[1] = new Object[] { "elevation", Double.class };
        attrs[2] = new Object[] { "mark.selected", Integer.class };

        return GeometryUtils.buildFeatureType(name, srs, type, attrs);
    }


    protected FeatureCollection[] splitLinesAndPolygons(
        List<SimpleFeature> f
    ) {
        FeatureCollection lines    = FeatureCollections.newCollection();
        FeatureCollection polygons = FeatureCollections.newCollection();

        for (SimpleFeature feature: f) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();


            if (geom instanceof LineString) {
                geom = applyElevationAttribute(feature, geom);
                lines.add(feature);
            }
            else if (geom instanceof Polygon) {
                geom = applyElevationAttribute(feature, geom);
                polygons.add(feature);
            }
            else {
                log.warn("Feature not supported: " + geom.getClass());
            }
        }

        log.debug("Found " + lines.size() + " barrier lines.");
        log.debug("Found " + polygons.size() + " barrier polygons.");

        return new FeatureCollection[] { lines, polygons };
    }


    protected static Geometry applyElevationAttribute(
        SimpleFeature feature,
        Geometry      geom
    ) {
        log.debug("Apply elevations for: " + geom.getClass());

        List<Double> elevations = extractElevations(feature);
        int           numPoints = geom.getNumPoints();
        int        numElevation = elevations.size();

        String typ = (String) feature.getAttribute("typ");

        if (numPoints > numElevation) {
            log.warn("More vertices in Geometry than elevations given.");
        }

        Coordinate[] c = geom.getCoordinates();
        for (int i = 0; i < numPoints; i++) {
            if (i < numElevation) {
                c[i].z = elevations.get(i);
            }
            else if (typ != null && typ.equals("Graben")) {
                c[i].z = -9999d;
            }
            else {
                c[i].z = 9999d;
            }
        }

        return geom;
    }


    protected static List<Double> extractElevations(SimpleFeature feature) {
        String tmp = (String) feature.getAttribute("elevation");
        String typ = (String) feature.getAttribute("typ");

        String[] elevations = tmp == null ? null : tmp.split(" ");

        int num = elevations != null ? elevations.length : 0;

        List<Double> list = new ArrayList<Double>(num);

        for (int i = 0; i < num; i++) {
            try {
                list.add(Double.parseDouble(elevations[i]));
            }
            catch (NumberFormatException nfe) {
                log.warn("Error while parsing elevation at pos: " + i);
                if (typ != null && typ.equals("Graben")) {
                    list.add(new Double(-9999.0));
                }
                else {
                    list.add(new Double(9999.0));
                }
            }
        }

        return list;
    }


    protected void setAxis(D4EArtifact artifact, File dir, WSPLGENJob job) {
        DGMAccess access = new DGMAccess(artifact);
        String river = access.getRiverName();
        String srid  = String.valueOf(access.getDGM().getSrid());
        String srs   = "EPSG:" + srid;

        RiverAxis axis = null;
        try {
            axis = RiverAxis.getRiverAxis(river);
        }
        catch (HibernateException iae) {
            log.warn("No valid river axis found for " + river);
            return;
        }
        if (axis == null) {
            log.warn("Could not find river axis for: '" + river + "'");
            return;
        }

        SimpleFeatureType ft = GeometryUtils.buildFeatureType(
            "axis", srs, LineString.class);

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ft);
        FeatureCollection collection = FeatureCollections.newCollection();

        builder.add(axis.getGeom());
        collection.add(builder.buildFeature("0"));

        File axisShape = new File(dir, WSPLGEN_AXIS);

        boolean a = GeometryUtils.writeShapefile(
            axisShape,
            GeometryUtils.buildFeatureType("axis", srs, LineString.class),
            collection);

        if (a) {
            job.setAxis(axisShape.getAbsolutePath());
        }
    }


    protected void setPro(D4EArtifact artifact, File dir, WSPLGENJob job) {
        DGMAccess access = new DGMAccess(artifact);
        String river = access.getRiverName();
        String srid  = String.valueOf(access.getDGM().getSrid());
        String srs   = "EPSG:" + srid;

        List<CrossSectionTrack> cst =
            CrossSectionTrack.getCrossSectionTrack(river, WSPLGEN_QPS_NAME);

        log.debug("Found " + cst.size() + " CrossSectionTracks.");

        Object[][] attrs = new Object[2][];
        attrs[0] = new Object[] { "ELEVATION", Double.class };
        attrs[1] = new Object[] { "KILOMETER", Double.class };

        SimpleFeatureType ft = GeometryUtils.buildFeatureType(
            "qps", srs, LineString.class, attrs);

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ft);
        FeatureCollection collection = FeatureCollections.newCollection();

        int i = 0;
        for (CrossSectionTrack track: cst) {
            builder.reset();
            builder.add(track.getGeom());
            builder.add(track.getZ().doubleValue());
            builder.add(track.getKm().doubleValue());

            collection.add(builder.buildFeature(String.valueOf(i++)));
        }

        File qpsShape = new File(dir, WSPLGEN_QPS);

        boolean q = GeometryUtils.writeShapefile(
            qpsShape,
            GeometryUtils.buildFeatureType(
                "qps", srs, LineString.class, attrs),
            collection);

        if (q) {
            job.setPro(qpsShape.getAbsolutePath());
        }
    }


    protected void setDgm(
        D4EArtifact artifact,
        WSPLGENJob  job,
        CallContext context
    ) {
        DGMAccess access = new DGMAccess(artifact);
        DGM dgm = access.getDGM();

        if (dgm == null) {
            log.warn("Could not find specified DGM.");
            return;
        }

        File dgmPath = new File(dgm.getPath());
        if (dgmPath.isAbsolute()) {
            job.setDgm(dgm.getPath());
        }
        else {
            RiverContext fc = (RiverContext)context.globalContext();
            File prefix = new File((String)fc.get("dgm-path"));
            job.setDgm(new File(prefix, dgm.getPath()).getAbsolutePath());
        }
    }


    protected void setArea(D4EArtifact artifact, File dir, WSPLGENJob job) {
        String useFloodplain = artifact.getDataAsString("use_floodplain");
        if (!Boolean.valueOf(useFloodplain)) {
            log.debug("WSPLGEN will not use floodplain.");
            return;
        }

        DGMAccess access = new DGMAccess(artifact);
        String river = access.getRiverName();
        String srid  = String.valueOf(access.getDGM().getSrid());
        String srs   = "EPSG:" + srid;

        Floodplain plain = Floodplain.getFloodplain(river);

        if (plain == null) {
            log.debug("No flood plain for river '" + river + "'");
            return;
        }

        Polygon polygon = plain.getGeom();
        if (polygon == null) {
            log.warn("Floodplain has no geometry.");
            return;
        }

        SimpleFeatureType ft = GeometryUtils.buildFeatureType(
            "talaue", srs, Polygon.class);

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ft);
        builder.add(polygon);

        FeatureCollection collection = FeatureCollections.newCollection();
        collection.add(builder.buildFeature("0"));

        File talaueShape = new File(dir, WSPLGEN_FLOODPLAIN);

        boolean t = GeometryUtils.writeShapefile(
            talaueShape,
            GeometryUtils.buildFeatureType("talaue", srs, Polygon.class),
            collection);

        if (t) {
            job.setArea(talaueShape.getAbsolutePath());
        }
    }


    protected void setOutFile(D4EArtifact artifact, WSPLGENJob job) {
        job.setOutFile(WSPLGEN_OUTPUT_FILE);
    }


    /** Gets the Waterlevel of chosen artifact as base for flooding. */
    // Challenge equals WaterlevelSelectState#getLabel
    protected WQKms getWQKms(D4EArtifact flys, CallContext cc) {
        String wspString = flys.getDataAsString(WSP_ARTIFACT);
        if (wspString == null) {
            log.debug("getWQKms(): wspString == null");
            return null;
        }
        String[] parts = wspString.split(";");
        String otherArtifact = parts[0];

        int idx = -1;
        try {
            idx = Integer.parseInt(parts[2]);
        }
        catch (NumberFormatException nfe) { /* do nothing */ }

        D4EArtifact src = otherArtifact != null
            ? RiverUtils.getArtifact(otherArtifact, cc)
            : flys;

        log.debug("Use waterlevel provided by Artifact: " + src.identifier());

        // The state actually depends on the kind of artifact.
        // E.g. StaticWQKmsArtifact needs other state
        Object computed = src.compute(
            cc,
            //null,
            //WINFO_WSP_STATE_ID,
            ComputeType.ADVANCE,
            false);

        // Depending on the artifact and calculation we have different results
        // in place. Note that the same conditions exist in
        // WaterlevelSelectState#getLabel .

        // Regular WSP calculation
        if (computed instanceof CalculationResult) {
            CalculationResult rawData = (CalculationResult) computed;
            WQKms[] wqkms;
            if (rawData.getData() instanceof FixRealizingResult) {
                wqkms = (WQKms[])((FixRealizingResult)rawData.getData())
                    .getWQKms();
            }
            else {
                wqkms = (WQKms[]) rawData.getData();
            }
            return wqkms == null || idx == -1 || idx >= wqkms.length
                ? null
                : wqkms[idx];
        }
        else if (computed instanceof WQKms) {
            // e.g. Fixations
            WQKms wqkms = (WQKms) computed;
            return (WQKms) computed;
        }
        else if (computed == null && src instanceof StaticWKmsArtifact) {
            // Floodmarks and protection.
            WQKms wqkms = WQKms.fromWKms(
                ((StaticWKmsArtifact)src).getWKms(0), 1d);
            return wqkms;
        }

        log.warn("getWQKms cannot handle " + computed.getClass()
            + " " + src.getClass());
        return null;
    }


    protected void setWsp(
        D4EArtifact artifact,
        CallContext  context,
        File         dir,
        WSPLGENJob   job)
    {
        log.debug("FloodMapState.setWsp");

        WQKms data = getWQKms(artifact, context);

        if (data == null) {
            log.warn("No WST data found!");
            return;
        }

        WstWriter writer = new WstWriter(0, true);

        // TODO REMOVE job.setWspTag(...) This is only used until the user is
        // able to select the WSP column himself!
        boolean writeWspTag = true;

        double[] buf = new double[4];
        log.debug("Add WST column: " + data.getName());
        writer.addColumn(data.getName());

        if (writeWspTag) {
            job.setWspTag(data.getName());
            writeWspTag = false;
        }

        for (int i = 0, num = data.size(); i < num; i++) {
            data.get(i, buf);
            writer.add(buf);
        }

        FileOutputStream fout = null;

        try {
            File wspFile = new File(dir, WSPLGEN_WSP_FILE);
            fout         = new FileOutputStream(wspFile);

            writer.write(fout);

            job.setWsp(wspFile.getAbsolutePath());
        }
        catch (FileNotFoundException fnfe) {
            log.warn("Error while writing wsp file: " + fnfe.getMessage());
        }
        finally {
            if (fout != null) {
                try {
                    fout.close();
                }
                catch (IOException ioe) { /* do nothing */ }
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
