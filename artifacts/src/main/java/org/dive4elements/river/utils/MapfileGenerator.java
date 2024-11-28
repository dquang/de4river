/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.river.artifacts.model.LayerInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This class iterates over a bunch of directories, searches for meta
 * information coresponding to shapefiles and creates a mapfile which is used by
 * a <i>MapServer</i>.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class MapfileGenerator
{
    private static final String XPATH_MAPSERVER_TEMPLATES_PATH =
        "/artifact-database/mapserver/templates/@path";

    public static final String WSPLGEN_RESULT_SHAPE   = "wsplgen.shp";
    public static final String WSPLGEN_LINES_SHAPE    = "barrier_lines.shp";
    public static final String WSPLGEN_POLYGONS_SHAPE = "barrier_polygons.shp";
    public static final String WSPLGEN_USER_SHAPE     = "user-rgd.shp";

    public static final String WSPLGEN_LAYER_TEMPLATE = "wsplgen_layer.vm";
    public static final String SHP_LAYER_TEMPLATE = "shapefile_layer.vm";
    public static final String DB_LAYER_TEMPLATE  = "db_layer.vm";
    public static final String RIVERAXIS_LAYER_TEMPLATE = "riveraxis-layer.vm";

    public static final String MS_WSPLGEN_PREFIX   = "wsplgen-";
    public static final String MS_BARRIERS_PREFIX  = "barriers-";
    public static final String MS_LINE_PREFIX      = "lines-";
    public static final String MS_POLYGONS_PREFIX  = "polygons-";
    public static final String MS_LAYER_PREFIX     = "ms_layer-";
    public static final String MS_USERSHAPE_PREFIX = "user-";

    private static Logger log = LogManager.getLogger(MapfileGenerator.class);

    private File shapefileDirectory;

    private VelocityEngine velocityEngine;


    protected MapfileGenerator() {
    }


    /**
     * Method to check the existance of a template file.
     *
     * @param templateID The name of a template.
     * @return true, of the template exists - otherwise false.
     */
    public boolean templateExists(String templateID){
        Template template = getTemplateByName(templateID);
        return template != null;
    }


    public abstract void generate() throws Exception;


    /**
     * Returns the VelocityEngine used for the template mechanism.
     *
     * @return the velocity engine.
     */
    protected VelocityEngine getVelocityEngine() {
        if (velocityEngine == null) {
            velocityEngine = new VelocityEngine();
            try {
                setupVelocity(velocityEngine);
            }
            catch (Exception e) {
                log.error(e, e);
                return null;
            }
        }
        return velocityEngine;
    }


    /**
     * Initialize velocity.
     *
     * @param engine Velocity engine.
     * @throws Exception if an error occured while initializing velocity.
     */
    protected void setupVelocity(VelocityEngine engine)
    throws Exception
    {
        engine.setProperty(
            "input.encoding",
            "UTF-8");

        engine.setProperty(
            RuntimeConstants.RUNTIME_LOG,
            getVelocityLogfile());

        engine.setProperty(
            "resource.loader",
            "file");

        engine.setProperty(
            "file.resource.loader.path",
            RiverUtils.getXPathString(XPATH_MAPSERVER_TEMPLATES_PATH)
        );

        engine.init();
    }

    protected abstract String getVelocityLogfile();

    protected VelocityContext getVelocityContext() {
        VelocityContext context = new VelocityContext();

        try {
            context.put("SHAPEFILEPATH",
                getShapefileBaseDir().getCanonicalPath());
            context.put("CONFIGDIR",
                Config.getConfigDirectory().getCanonicalPath());
        }
        catch (FileNotFoundException fnfe) {
            // this is bad
            log.warn(fnfe, fnfe);
        }
        catch (IOException ioe) {
            // this is also bad
            log.warn(ioe, ioe);
        }

        return context;
    }


    /**
     * Returns a template specified by <i>model</i>.
     *
     * @param model The name of the template.
     * @return a template.
     */
    public Template getTemplateByName(String model) {
        if (model.indexOf(".vm") < 0) {
            model = model.concat(".vm");
        }

        try {
            VelocityEngine engine = getVelocityEngine();
            if (engine == null) {
                log.error("Error while fetching VelocityEngine.");
                return null;
            }

            return engine.getTemplate(model);
        }
        catch (Exception e) {
            log.warn(e, e);
        }

        return null;
    }


    /**
     * Returns the mapfile  template.
     *
     * @return the mapfile template.
     * @throws Exception if an error occured while reading the configuration.
     */
    protected Template getMapfileTemplateObj()
    throws Exception
    {
        String mapfileName = getMapfileTemplate();
        return getTemplateByName(mapfileName);
    }

    protected abstract String getMapfilePath();

    protected abstract String getMapfileTemplate();


    /**
     * Returns the base directory storing the shapefiles.
     *
     * @return the shapefile base directory.
     *
     * @throws FileNotFoundException if no shapefile path is found or
     * configured.
     */
    public File getShapefileBaseDir()
            throws FileNotFoundException, IOException
    {
        if (shapefileDirectory == null) {
            String path = RiverUtils.getXPathString(
                RiverUtils.XPATH_MAPFILES_PATH);

            if (path != null) {
                shapefileDirectory = new File(path);
            }

            if (shapefileDirectory == null) {
                throw new FileNotFoundException("No shapefile directory given");
            }

            if (!shapefileDirectory.exists()) {
                shapefileDirectory.mkdirs();
            }
        }

        return shapefileDirectory;
    }


    protected File[] getUserDirs()
            throws FileNotFoundException, IOException
    {
        File   baseDir      = getShapefileBaseDir();
        File[] artifactDirs = baseDir.listFiles();

        // TODO ONLY RETURN DIRECTORIES OF THE SPECIFIED USER

        return artifactDirs;
    }


    protected List<String> parseLayers(File[] dirs) {
        List<String> layers = new ArrayList<String>();

        for (File dir: dirs) {
            File[] layerFiles = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File directory, String name) {
                    return name.startsWith(MS_LAYER_PREFIX);
                }
            });

            if (layerFiles == null) {
                continue;
            }

            for (File layer: layerFiles) {
                try {
                    layers.add(layer.getCanonicalPath());
                }
                catch (IOException ioe) {
                    log.warn(ioe, ioe);
                }
            }
        }

        return layers;
    }


    /**
     * Creates a layer snippet which might be included in the mapfile.
     *
     * @param layerInfo A LayerInfo object that contains all necessary
     * information to build a Mapserver LAYER section.
     * @param layerFile The file that is written.
     * @param tpl The Velocity template which is used to create the LAYER
     * section.
     */
    public void writeLayer(
        LayerInfo layerInfo,
        File      layerFile,
        Template  tpl
    )
    throws    FileNotFoundException
    {
        if (log.isDebugEnabled()) {
            log.debug("Write layer for:");
            log.debug("   directory/file: " + layerFile.getName());
        }

        Writer writer = null;

        try {
            writer = new FileWriter(layerFile);

            VelocityContext context = getVelocityContext();
            context.put("LAYER", layerInfo);

            tpl.merge(context, writer);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
        catch (Exception e) {
            log.error(e, e);
        }
        finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            }
            catch (IOException ioe) {
                log.debug(ioe, ioe);
            }
        }
    }


    /**
     * Creates a mapfile with the layer information stored in <i>layers</i>.
     *
     * @param layers Layer information.
     */
    public void writeMapfile(List<String> layers) {
        String tmpMapName = "mapfile" + new Date().getTime();

        File mapfile = new File(getMapfilePath());

        File   tmp     = null;
        Writer writer  = null;

        try {
            tmp = new File(mapfile.getParent(), tmpMapName);
            tmp.createNewFile();

            writer = new FileWriter(tmp);

            VelocityContext context = getVelocityContext();
            context.put("LAYERS", layers);

            Template mapTemplate = getMapfileTemplateObj();
            if (mapTemplate == null) {
                log.warn("No mapfile template found.");
                return;
            }

            mapTemplate.merge(context, writer);

            // we need to create a temporary mapfile first und rename it into
            // real mapfile because we don't run into race conditions on this
            // way. (iw)
            tmp.renameTo(mapfile);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
        catch (Exception e) {
            log.error(e, e);
        }
        finally {
            try {
                if (writer != null) {
                    writer.close();
                }

                if (tmp.exists()) {
                    tmp.delete();
                }
            }
            catch (IOException ioe) {
                log.debug(ioe, ioe);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
