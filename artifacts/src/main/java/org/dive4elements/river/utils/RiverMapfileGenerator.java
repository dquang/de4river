/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;

import org.dive4elements.river.artifacts.model.LayerInfo;
import org.dive4elements.river.artifacts.model.RiverFactory;

import org.dive4elements.river.model.River;
import org.dive4elements.river.model.RiverAxis;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.velocity.Template;
import org.hibernate.HibernateException;

public class RiverMapfileGenerator extends MapfileGenerator {

    private static final String XPATH_RIVERMAP_VELOCITY_LOGFILE =
        "/artifact-database/rivermap/velocity/logfile/@path";

    private static final String XPATH_RIVERMAP_MAPFILE_PATH =
        "/artifact-database/rivermap/mapfile/@path";

    private static final String XPATH_RIVERMAP_MAPFILE_TEMPLATE =
        "/artifact-database/rivermap/map-template/@path";

    public static final Pattern DB_URL_PATTERN =
        Pattern.compile("(.*)\\/\\/(.*):([0-9]+)\\/([a-zA-Z]+)");

    public static final Pattern DB_PSQL_URL_PATTERN =
        Pattern.compile("(.*)\\/\\/(.*):([0-9]+)\\/([a-zA-Z0-9]+)");

    private static Logger log = LogManager.getLogger(RiverMapfileGenerator.class);

    /**
     * Generate river axis mapfile.
     */
    @Override
    public void generate() {
        log.debug("generate()");

        List<River>  rivers     = RiverFactory.getRivers();
        List<String> riverFiles = new ArrayList<String>();

        for (River river : rivers) {
            RiverAxis riverAxis = null;
            try {
                riverAxis = RiverAxis.getRiverAxis(river.getName());
            }
            catch (HibernateException iae) {
                log.error("No valid riveraxis found for " + river.getName());
                continue;
            }

            if (riverAxis == null) {
                log.warn("River " + river.getName() + " has no river axis!");
                continue;
            }
            if (riverAxis.getGeom() == null) {
                log.warn("River " + river.getName() +
                    " has no riveraxis geometry!");
                continue;
            }
            MultiLineString geom = riverAxis.getGeom();
            Envelope extent = geom.getEnvelopeInternal();

            createRiverAxisLayer(
                    river.getName(),
                    river.getId(),
                    Integer.toString(geom.getSRID()),
                    extent.getMinX() + " " +
                    extent.getMinY() + " " +
                    extent.getMaxX() + " " +
                    extent.getMaxY());

            riverFiles.add("river-" + river.getName() + ".map");
        }
        writeMapfile(riverFiles);
    }

    protected void createRiverAxisLayer(
        String riverName,
        int riverID,
        String srid,
        String extend
    ) {
        LayerInfo layerInfo = new LayerInfo();
        layerInfo.setName(riverName);
        layerInfo.setConnection(MapUtils.getConnection());
        layerInfo.setConnectionType(MapUtils.getConnectionType());
        layerInfo.setSrid(srid);
        layerInfo.setExtent(extend);
        layerInfo.setType("line");
        // FIXME: Use templates for that
        if (RiverUtils.isUsingOracle()) {
            layerInfo.setData("geom FROM river_axes USING SRID " + srid);
        } else {
            layerInfo.setData("geom FROM river_axes");
        }
        layerInfo.setFilter("river_id = " + riverID + " and kind_id = 1");
        layerInfo.setTitle(riverName + " RiverAxis");

        Template template = getTemplateByName("riveraxis-layer.vm");
        if (template == null) {
            log.warn("Template riveraxis-layer.vm not found.");
            return;
        }

        try {
            File layerFile = new File(
                getShapefileBaseDir(),
                "river-" + riverName + ".map");
            writeLayer(layerInfo, layerFile, template);
        }
        catch (IOException e) {
            log.warn(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected String getVelocityLogfile() {
        return RiverUtils.getXPathString(XPATH_RIVERMAP_VELOCITY_LOGFILE);
    }

    @Override
    protected String getMapfilePath() {
        return RiverUtils.getXPathString(RiverUtils.XPATH_MAPFILES_PATH)
            + "/" + RiverUtils.getXPathString(XPATH_RIVERMAP_MAPFILE_PATH);
    }

    @Override
    protected String getMapfileTemplate() {
        return RiverUtils.getXPathString(XPATH_RIVERMAP_MAPFILE_TEMPLATE);
    }
}
