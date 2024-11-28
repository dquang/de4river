/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.river.model.RiverAxis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.hibernate.HibernateException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class GeometryUtils {

    private static final Logger log = LogManager.getLogger(GeometryUtils.class);

    public static final String PREFIX_EPSG  = "EPSG:";

    public static final String DEFAULT_EPSG = "EPSG:31467";

    private GeometryUtils() {
    }

    public static Envelope getRiverBoundary(String rivername) {
        try {
            RiverAxis axis = RiverAxis.getRiverAxis(rivername);
            if (axis != null) {
                return axis.getGeom().getEnvelopeInternal();
            }
        }
        catch(HibernateException iae) {
            log.warn("Exception, no valid river axis found for " + rivername);
            return null;
        }
        log.warn("No valid river axis found for " + rivername);

        return null;
    }

    public static String getRiverBounds(String rivername) {
        Envelope env = getRiverBoundary(rivername);

        if (env != null) {
            return jtsBoundsToOLBounds(env);
        }

        return null;
    }

    /**
     * Returns boundary of Envelope <i>env</i> in OpenLayers representation.
     *
     * @param env The envelope of a geometry.
     *
     * @return the OpenLayers boundary of <i>env</i>.
     */
    public static String jtsBoundsToOLBounds(Envelope env) {
        StringBuilder buf = new StringBuilder();
        buf.append(env.getMinX()); buf.append(' ');
        buf.append(env.getMinY()); buf.append(' ');
        buf.append(env.getMaxX()); buf.append(' ');
        buf.append(env.getMaxY());
        return buf.toString();
    }

    public static String createOLBounds(Geometry a, Geometry b) {
        Coordinate[] ca = a.getCoordinates();
        Coordinate[] cb = b.getCoordinates();

        double lowerX = Double.MAX_VALUE;
        double lowerY = Double.MAX_VALUE;
        double upperX = -Double.MAX_VALUE;
        double upperY = -Double.MAX_VALUE;

        for (Coordinate c: ca) {
            lowerX = lowerX < c.x ? lowerX : c.x;
            lowerY = lowerY < c.y ? lowerY : c.y;

            upperX = upperX > c.x ? upperX : c.x;
            upperY = upperY > c.y ? upperY : c.y;
        }

        for (Coordinate c: cb) {
            lowerX = lowerX < c.x ? lowerX : c.x;
            lowerY = lowerY < c.y ? lowerY : c.y;

            upperX = upperX > c.x ? upperX : c.x;
            upperY = upperY > c.y ? upperY : c.y;
        }

        return "" + lowerX + " " + lowerY + " " + upperX + " " + upperY;
    }

    public static SimpleFeatureType buildFeatureType(
        String name, String srs, Class<?> geometryType)
    {
        return buildFeatureType(name, srs, geometryType, null);
    }

    /**
     * Creates a new SimpleFeatureType using a SimpleFeatureTypeBuilder.
     *
     * @param name The name of the FeatureType.
     * @param srs The SRS (e.g. "EPSG:31466").
     * @param geometryType The geometry type's class (e.g. Polygon.class).
     * @param attrs Optional. Object with attribute-name/attribute-class pairs
     * where index 0 specifies the name as string and index 1 the
     * ype as class.
     *
     * @return a new SimpleFeatureType.
     */
    public static SimpleFeatureType buildFeatureType(String name, String srs,
        Class<?> geometryType, Object[][] attrs) {
        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            CoordinateReferenceSystem crs    = CRS.decode(srs);

            builder.setName("flys");
            builder.setNamespaceURI("http://www.intevation.de/");
            builder.setCRS(crs);
            builder.setSRS(srs);

            builder.add("geometry", geometryType, crs);

            if (attrs != null) {
                for (Object[] attr: attrs) {
                    builder.add((String) attr[0], (Class<?>) attr[1]);
                }
            }

            return builder.buildFeatureType();
        }
        catch (NoSuchAuthorityCodeException nsae) {
        }
        catch (FactoryException fe) {
        }

        return null;
    }

    public static List<SimpleFeature> parseGeoJSON(
        String geojson, SimpleFeatureType ft
    ) {
        List<SimpleFeature> collection = new ArrayList<SimpleFeature>();

        try {
            FeatureJSON fjson = new FeatureJSON();
            fjson.setFeatureType(ft);

            FeatureIterator<SimpleFeature> iterator =
                fjson.streamFeatureCollection(geojson);

            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        catch (IOException ioe) {
            // TODO handle exception
        }

        return collection;
    }


    /**
     * This method returns the {@link CoordinateReferenceSystem} by the
     * {@link String} <i>epsg</i>.
     *
     * @param epsg An EPSG code like <b>EPSG:31466</b>
     *
     * @return the {@link CoordinateReferenceSystem} specified by <i>epsg</i>.
     */
    public static CoordinateReferenceSystem getCoordinateReferenceSystem(
        String epsg
    ) {
        if (epsg == null) {
            log.warn("cannot create CoordinateReferenceSystem with null");
            return null;
        }

        if (!epsg.startsWith(PREFIX_EPSG)) {
            epsg = PREFIX_EPSG + epsg;
        }

        try {
            return CRS.decode(epsg);
        }
        catch (FactoryException fe) {
            log.error(
                "unable to get CoordinateReferenceSystem for: " + epsg,
                fe);
        }

        return null;
    }


    public static Envelope transform(Envelope orig, String targetSrs) {
        return transform(orig, targetSrs, DEFAULT_EPSG);
    }


    public static Envelope transform(
        Envelope orig,
        String   targetSrs,
        String   origSrs
    ) {
        if (targetSrs == null || orig == null || origSrs == null) {
            log.warn("unable to transform envelope: empty parameters");
            return orig;
        }

        log.debug("Transform envlope to '" + targetSrs + "'");
        try {
            CoordinateReferenceSystem sourceCRS =
                getCoordinateReferenceSystem(origSrs);

            CoordinateReferenceSystem targetCRS =
                getCoordinateReferenceSystem(targetSrs);

            if (sourceCRS != null && targetCRS != null) {
                ReferencedEnvelope tmpEnv =
                    new ReferencedEnvelope(orig, CRS.decode(origSrs));

                Envelope target = tmpEnv.transform(targetCRS, false);

                if (log.isDebugEnabled()) {
                    log.debug("   orig envelope       : " + orig);
                    log.debug("   transformed envelope: " + target);
                }

                return target;
            }
        }
        catch (NoSuchAuthorityCodeException nsae) {
            log.error("Cannot get CoordinateReferenceSystem!", nsae);
        }
        catch (FactoryException fe) {
            log.error("Cannot get CoordinateReferenceSystem!", fe);
        }
        catch (TransformException te) {
            log.error("Cannot transform envelope from source "
                + origSrs + " to target srs " + targetSrs);
        }

        return null;
    }


    public static boolean writeShapefile(File shape,
        SimpleFeatureType featureType, FeatureCollection<?, ?> collection) {
        return writeShapefile(shape, featureType, collection,
            featureType.getCoordinateReferenceSystem());
    }


    public static boolean writeShapefile(File shape,
        SimpleFeatureType featureType, FeatureCollection<?, ?> collection,
        CoordinateReferenceSystem crs) {
        if (collection.isEmpty()) {
            log.warn("Shapefile is not written - no features given!");
            return false;
        }

        Transaction   transaction = null;

        try {
            MathTransform transform = CRS.findMathTransform(
                CRS.decode(DEFAULT_EPSG), crs);

            Map<String, Serializable> params =
                new HashMap<String, Serializable>();

            params.put("url", shape.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            DataStoreFactorySpi dataStoreFactory =
                new ShapefileDataStoreFactory();

            ShapefileDataStore newDataStore =
                (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(featureType);

            transaction = new DefaultTransaction("create");

            String typeName = newDataStore.getTypeNames()[0];

            FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                newDataStore.getFeatureWriter(typeName, transaction);

            SimpleFeatureIterator iterator =
                (SimpleFeatureIterator)collection.features();

            while (iterator.hasNext()){
                SimpleFeature feature = iterator.next();
                SimpleFeature copy    = writer.next();

                copy.setAttributes(feature.getAttributes());

                Geometry orig        = (Geometry) feature.getDefaultGeometry();
                Geometry reprojected = JTS.transform(orig, transform);

                copy.setDefaultGeometry(reprojected);
                writer.write();
            }

            transaction.commit();

            return true;
        }
        catch (MalformedURLException mue) {
            log.error("Unable to prepare shapefile: " + mue.getMessage());
        }
        catch (IOException ioe) {
            log.error("Unable to write shapefile: " + ioe.getMessage());
        }
        catch (NoSuchAuthorityCodeException nsae) {
            log.error("Cannot get CoordinateReferenceSystem for '"
                + DEFAULT_EPSG + "'");
        }
        catch (FactoryException fe) {
            log.error("Cannot get CoordinateReferenceSystem for '"
                + DEFAULT_EPSG + "'");
        }
        catch (TransformException te) {
            log.error("Was not able to transform geometry!", te);
        }
        finally {
            if (transaction != null) {
                try {
                    transaction.close();
                }
                catch (IOException ioe) { /* do nothing */ }
            }
        }

        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
