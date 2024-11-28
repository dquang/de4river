/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.DGMAccess;
import org.dive4elements.river.artifacts.model.LayerInfo;
import org.dive4elements.river.artifacts.model.map.WMSDBLayerFacet;
import org.dive4elements.river.artifacts.model.map.WMSLayerFacet;
import org.dive4elements.river.artifacts.model.map.WSPLGENLayerFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.FloodMapState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.velocity.Template;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.Envelope;

public class ArtifactMapfileGenerator extends MapfileGenerator {

    private static Logger log = LogManager.getLogger(
        ArtifactMapfileGenerator.class);

    private static final String XPATH_FLOODMAP_VELOCITY_LOGFILE =
        "/artifact-database/floodmap/velocity/logfile/@path";

    private static final String XPATH_FLOODMAP_MAPFILE_TEMPLATE =
        "/artifact-database/floodmap/map-template/@path";

    private static final String XPATH_FLOODMAP_MAPFILE_PATH =
        "/artifact-database/floodmap/mapfile/@path";

    public static final String FLOODMAP_UESK_KEY =
        "floodmap.uesk";

    public static final String FLOODMAP_UESK_DEF =
        "Floodmap: {0}-km {1,number,####} - {2,number,####} - {3}";

    @Override
    protected String getVelocityLogfile() {
        return RiverUtils.getXPathString(XPATH_FLOODMAP_VELOCITY_LOGFILE);
    }

    /**
     * Method which starts searching for meta information file and mapfile
     * generation.
     */
    @Override
    public void generate() throws IOException
    {
        File[] userDirs = getUserDirs();
        List<String> layers = parseLayers(userDirs);
        log.info("Found " + layers.size() + " layers for user mapfile.");

        writeMapfile(layers);
    }

    /**
     * Creates a layer file used for Mapserver's mapfile which represents the
     * floodmap.
     *
     * @param flys The D4EArtifact that owns <i>wms</i>.
     * @param wms The WMSLayerFacet that contains information for the layer.
     */
    public void createUeskLayer(
        D4EArtifact  flys,
        WSPLGENLayerFacet wms,
        String        style,
        CallContext context
    ) throws FileNotFoundException, IOException
    {
        log.debug("createUeskLayer");

        String identifier = flys.identifier();

        DGMAccess access = new DGMAccess(flys);

        LayerInfo layerinfo = new LayerInfo();
        layerinfo.setName(MS_WSPLGEN_PREFIX + identifier);
        layerinfo.setType("POLYGON");
        layerinfo.setDirectory(identifier);
        layerinfo.setData(WSPLGEN_RESULT_SHAPE);

        String river = access.getRiverName();

        double from = access.hasFrom() ? access.getFrom() : 0d;
        double to   = access.hasTo()   ? access.getTo()   : 0d;

        String title = Resources.format(
            context.getMeta(),
            FLOODMAP_UESK_KEY,
            FLOODMAP_UESK_DEF,
            river,
            from, to,
            identifier);

        layerinfo.setTitle(title);

        layerinfo.setStyle(style);
        layerinfo.setSrid(String.valueOf(access.getDGM().getSrid()));

        String name = MS_LAYER_PREFIX + wms.getName();

        Template template = getTemplateByName(WSPLGEN_LAYER_TEMPLATE);
        if (template == null) {
            log.warn("Template '" + WSPLGEN_LAYER_TEMPLATE + "' found.");
            return;
        }

        try {
            File dir = new File(getShapefileBaseDir(), identifier);
            writeLayer(layerinfo, new File(dir, name), template);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
            log.warn("Unable to write layer: " + name);
        }
    }


    /**
     * Creates a layer file used for Mapserver's mapfile which represents the
     * shape files uploaded by the user.
     *
     * @param flys The D4EArtifact that owns <i>wms</i>.
     * @param wms The WMSLayerFacet that contains information for the layer.
     */
    public void createUserShapeLayer(D4EArtifact flys, WMSLayerFacet wms)
        throws FileNotFoundException, IOException
    {
        log.debug("createUserShapeLayer");

        String uuid = flys.identifier();
        File   dir  = new File(getShapefileBaseDir(), uuid);
        File   test = new File(dir, WSPLGEN_USER_SHAPE);

        if (!test.exists() || !test.canRead()) {
            log.debug("No user layer existing.");
            return;
        }

        File userShape = new File(dir, WSPLGEN_USER_SHAPE);
        ShpFiles sf = new ShpFiles(userShape);
        ShapefileReader sfr = new ShapefileReader(sf, true, false, null);
        ShapefileHeader sfh = sfr.getHeader();

        String group      = MS_USERSHAPE_PREFIX + uuid;
        String groupTitle = "I18N_USER_SHAPE_TITLE";

        LayerInfo info = new LayerInfo();
        info.setName(MS_LAYER_PREFIX + FloodMapState.WSPLGEN_USER_RGD + uuid);
        if (sfh.getShapeType().isLineType()) {
            info.setType("LINE");
        }
        else if (sfh.getShapeType().isPolygonType()) {
            info.setType("POLYGON");
        }
        else {
            return;
        }
        info.setDirectory(uuid);
        info.setData(WSPLGEN_USER_SHAPE);
        info.setTitle("I18N_USER_SHAPE");
        info.setGroup(group);
        info.setGroupTitle(groupTitle);
        info.setSrid(wms.getSrid());

        //String nameUser = MS_LAYER_PREFIX + wms.getName();
        // TODO: This rewrites the user-rgd mapfile fragment generated by
        // HWSBarrierState. Otherwise we would have to fragments with same
        // layer name. Should be refactored...
        String nameUser = MS_LAYER_PREFIX + "user-rgd";

        Template tpl = getTemplateByName(SHP_LAYER_TEMPLATE);
        if (tpl == null) {
            log.warn("Template '" + SHP_LAYER_TEMPLATE + "' found.");
            return;
        }

        try {
            writeLayer(info, new File(dir, nameUser), tpl);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
            log.warn("Unable to write layer: " + nameUser);
        }

    }


    /**
     * Creates a layer file used for Mapserver's mapfile which represents
     * geometries from database.
     *
     * @param flys The D4EArtifact that owns <i>wms</i>.
     * @param wms The WMSLayerFacet that contains information for the layer.
     */
    public void createDatabaseLayer(
            D4EArtifact    flys,
            WMSDBLayerFacet wms,
            String          style
            )
        throws FileNotFoundException, IOException
    {
        log.debug("createDatabaseLayer");

        LayerInfo layerinfo = new LayerInfo();
        layerinfo.setName(wms.getName() + "-" + flys.identifier());
        layerinfo.setType(wms.getGeometryType());
        layerinfo.setFilter(wms.getFilter());
        layerinfo.setData(wms.getData());
        layerinfo.setTitle(wms.getDescription());
        layerinfo.setStyle(style);

        Envelope env = wms.getExtent();
        if (env != null) {
            if (env.getArea() <= 0) {
                /* For MapServer min and max must not be equal. EXTENT has no
                   effect on Layerzoom, thus expand arbitrarily by 1. */
                env.expandBy(1d);
            }
            layerinfo.setExtent(GeometryUtils.jtsBoundsToOLBounds(env));
        } else {
            log.error("Layer without extent. Probably no geometry at all.");
            layerinfo.setExtent("0 0 1 1");
        }
        layerinfo.setConnection(wms.getConnection());
        layerinfo.setConnectionType(wms.getConnectionType());
        layerinfo.setLabelItem(wms.getLabelItem());
        layerinfo.setSrid(wms.getSrid());

        String name = MS_LAYER_PREFIX + wms.getName();

        Template template = getTemplateByName(DB_LAYER_TEMPLATE);
        if (template == null) {
            log.warn("Template '" + DB_LAYER_TEMPLATE + "' found.");
            return;
        }

        try {
            File dir = new File(getShapefileBaseDir(), flys.identifier());
            writeLayer(layerinfo, new File(dir, name), template);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
            log.warn("Unable to write layer: " + name);
        }
    }

    @Override
    protected String getMapfilePath() {
        return RiverUtils.getXPathString(RiverUtils.XPATH_MAPFILES_PATH)
            + "/" + RiverUtils.getXPathString(XPATH_FLOODMAP_MAPFILE_PATH);
    }

    @Override
    protected String getMapfileTemplate() {
        return RiverUtils.getXPathString(XPATH_FLOODMAP_MAPFILE_TEMPLATE);
    }
}
