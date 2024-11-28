/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.MapAccess;
import org.dive4elements.river.artifacts.model.map.HWS;
import org.dive4elements.river.artifacts.model.map.HWSContainer;
import org.dive4elements.river.artifacts.model.map.HWSFactory;
import org.dive4elements.river.utils.GeometryUtils;
import org.dive4elements.river.utils.MapfileGenerator;
import org.dive4elements.river.utils.RiverUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.w3c.dom.Element;

public class HWSBarriersState
extends DefaultState
{

    /** The log that is used in this class.*/
    private static Logger log = LogManager.getLogger(HWSBarriersState.class);
    private static final String HWS_SHAPEFILE_LINES = "hws-lines.shp";
    private static final String HWS_SHAPEFILE_POINTS = "hws-points.shp";


    @Override
    protected String getUIProvider() {
        return "map_digitize";
    }


    @Override
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        creator.addAttr(itemElement, "label", "", true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String hash,
        CallContext context,
        List<Facet> facets,
        Object old
    ) {
        File artifactDir = getDirectory(artifact);

        if (artifactDir == null) {
            log.error("Could not create directory for HWS shapefile!");
            return null;
        }

        MapAccess access = new MapAccess(artifact);
        String river = access.getRiverName();
        HWSContainer hwsLines = HWSFactory.getHWSLines(river);
        HWSContainer hwsPoints = HWSFactory.getHWSPoints(river);
        List<String> selected = access.getHWS();

        List<HWS> selectedLines = hwsLines.getHws(selected);
        List<HWS> selectedPoints = hwsPoints.getHws(selected);

        FeatureCollection collectionLines = FeatureCollections.newCollection();
        SimpleFeatureType lineType = null;
        for (HWS h : selectedLines) {
            lineType = h.getFeatureType();
            collectionLines.add(h.getFeature());
        }
        boolean successLines = false;
        if (lineType != null && collectionLines.size() > 0) {
            File shapeLines = new File(artifactDir, HWS_SHAPEFILE_LINES);
            successLines = GeometryUtils.writeShapefile(
                shapeLines, lineType, collectionLines);
        }

        FeatureCollection collectionPoints =
            FeatureCollections.newCollection();
        SimpleFeatureType pointType = null;
        for (HWS h : selectedPoints) {
            pointType = h.getFeatureType();
            collectionPoints.add(h.getFeature());
        }
        boolean successPoints = false;
        if (pointType != null && collectionPoints.size() > 0) {
            File shapePoints = new File(artifactDir, HWS_SHAPEFILE_POINTS);
            successPoints =GeometryUtils.writeShapefile(
                shapePoints, pointType, collectionPoints);
        }

        if (successLines) {
            FloodMapState.createMapfile(
                artifact,
                artifactDir,
                MapfileGenerator.MS_LAYER_PREFIX + "hws-lines",
                HWS_SHAPEFILE_LINES,
                "LINE",
                "31467",
                "hws");
        }
        if (successPoints) {
            FloodMapState.createMapfile(
                artifact,
                artifactDir,
                MapfileGenerator.MS_LAYER_PREFIX + "hws-points",
                HWS_SHAPEFILE_POINTS,
                "POINT",
                "31467",
                "hws");
        }

        String userRgd = artifact.getDataAsString("uesk.user-rgd");
        if (!userRgd.equals("none")) {
            if (extractUserShp(artifactDir)) {
                try {
                    ShapefileDataStore store = new ShapefileDataStore(
                    new File(artifactDir.getCanonicalPath() +
                        "/" + FloodMapState.WSPLGEN_USER_RGD_SHAPE)
                            .toURI().toURL());
                    GeometryDescriptor desc =
                        store.getSchema().getGeometryDescriptor();
                    String type = desc.getType().getName().toString();
                    String proj =
                        desc.getCoordinateReferenceSystem().
                            getCoordinateSystem().toString();
                    int pos1 = proj.indexOf("EPSG\",\"");
                    int pos2 = proj.indexOf("\"]]");
                    String epsg = "";
                    if (pos1 >= 0 && pos2 >= 0) {
                        epsg =
                            proj.substring(proj.indexOf("EPSG\",\"") + 7,
                                proj.indexOf("\"]]"));
                    }
                    else {
                        log.warn("Could not read EPSG code from shapefile.");
                        return null;
                    }
                    if (type.contains("Line")) {
                        type = "LINE";
                    }
                    else if (type.contains("Poly")) {
                        type = "POLYGON";
                    }
                    else {
                        type = "POINT";
                    }
                    FloodMapState.createMapfile(
                        artifact,
                        artifactDir,
                        MapfileGenerator.MS_LAYER_PREFIX
                        + FloodMapState.WSPLGEN_USER_RGD,
                        FloodMapState.WSPLGEN_USER_RGD_SHAPE,
                        type,
                        epsg,
                        FloodMapState.WSPLGEN_USER_RGD);
                }
                catch (IOException e) {
                    log.warn("No mapfile for user-rgd created!");
                }
            }
        }
        return null;
    }

    private boolean extractUserShp(File dir) {
        File archive = new File(dir, FloodMapState.WSPLGEN_USER_RGD_ZIP);
        boolean exists = archive.exists();
        log.debug("Zip file exists: " + exists);
        if (exists) {
            try {
                File tmpDir = new File(dir, "usr_tmp");
                FileTools.extractArchive(archive, tmpDir);
                moveFiles(tmpDir, dir);
                return true;
            }
            catch (IOException ioe) {
                log.warn("Zip archive " + dir + "/"
                    + FloodMapState.WSPLGEN_USER_RGD_ZIP
                    + " could not be extracted.");
                return false;
            }
        }
        return false;
    }

    private void moveFiles(File source, final File target)
    throws IOException
    {
        if (!source.exists()) {
            return;
        }
        if (!target.exists()) {
            target.mkdir();
        }
        FileTools.walkTree(source, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                if (!file.isDirectory()) {
                    String name = file.getName();
                    String suffix = "";
                    int pos = name.lastIndexOf('.');
                    if (pos > 0 && pos < name.length() - 1) {
                        suffix = name.substring(pos + 1);
                    }
                    else {
                        return true;
                    }
                    try {
                        FileTools.copyFile(
                            file,
                            new File(target,
                                FloodMapState.WSPLGEN_USER_RGD
                                + "." + suffix));
                    }
                    catch (IOException ioe) {
                        log.warn("Error while copying file " + file.getName());
                        return true;
                    }
                }
                return true;
            }
        });

        FileTools.deleteRecursive(source);
    }


    @Override
    public void endOfLife(Artifact artifact, Object callContext) {
        super.endOfLife(artifact, callContext);
        log.info("ScenarioSelect.endOfLife: " + artifact.identifier());

        D4EArtifact flys = (D4EArtifact) artifact;
        removeDirectory(flys);
    }


    /**
     * Removes directory and all its content where the required data and the
     * results of WSPLGEN are stored. Should be called in endOfLife().
     */
    // FIXME: I've seen this code somewhere else...
    protected void removeDirectory(D4EArtifact artifact) {
        String shapePath = RiverUtils.getXPathString(
            RiverUtils.XPATH_MAPFILES_PATH);

        File artifactDir = new File(shapePath, artifact.identifier());

        if (artifactDir.exists()) {
            log.debug("Delete directory: " + artifactDir.getAbsolutePath());
            boolean success = FileTools.deleteRecursive(artifactDir);
            if (!success) {
                log.warn("could not remove dir '" + artifactDir + "'");
            }
        }
        else {
            log.debug("There is no directory to remove.");
        }
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

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
