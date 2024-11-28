/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.river.artifacts.model.NamedObjectImpl;
import org.dive4elements.river.utils.GeometryUtils;


public class HWS
extends NamedObjectImpl
{

    public enum TYPE {LINE, POINT};

    private Geometry geom;
    private String id;
    private int kind;
    private int official;
    private String fedState;
    private String description;
    private TYPE type;

    public HWS() {
        this.geom = null;
    }

    public HWS(String name) {
        super(name);
        this.geom = null;
    }

    public HWS(
        String name,
        Geometry geom,
        String id,
        int kind,
        int official,
        String fedState,
        String description,
        TYPE type
    ) {
        super(name);
        this.geom = geom;
        this.id = id;
        this.kind = kind;
        this.official = official;
        this.fedState = fedState;
        this.description = description;
        this.type = type;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public boolean isOfficial() {
        return official == 1;
    }

    public void setOfficial(boolean o) {
        this.official = o ? 1 : 0;
    }

    public String getFedState() {
        return fedState;
    }

    public void setFedState(String fedState) {
        this.fedState = fedState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public SimpleFeatureType getFeatureType() {
        int srid = this.geom.getSRID();
        String srs = "EPSG:" + srid;

        Object[][] attrs = new Object[5][];
        attrs[0] = new Object[] { "name", String.class };
        attrs[1] = new Object[] { "description", String.class };
        attrs[2] = new Object[] { "TYP", String.class };
        attrs[3] = new Object[] { "fed_state", String.class };
        attrs[4] = new Object[] { "official", Integer.class };
        SimpleFeatureType ft =
            GeometryUtils.buildFeatureType(
                "hws", srs, this.geom.getClass(), attrs);
        return ft;
    }

    public SimpleFeature getFeature() {
        SimpleFeatureType ft = getFeatureType();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(ft);
        featureBuilder.add(this.geom);
        featureBuilder.add(this.name);
        featureBuilder.add(this.description);
        if (this.kind == 1) {
            featureBuilder.add("Rohr 1");
        }
        else if (this.kind == 2) {
            featureBuilder.add("Damm");
        }
        else if (this.kind == 3) {
            featureBuilder.add("Graben");
        }
        else {
            featureBuilder.add("");
        }
        featureBuilder.add(this.fedState);
        featureBuilder.add(this.official);

        return featureBuilder.buildFeature(null);
    }
}
