/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.vividsolutions.jts.geom.Envelope;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.map.ShapeFacet;
import org.dive4elements.river.artifacts.model.map.WMSLayerFacet;
import org.dive4elements.river.artifacts.model.map.WSPLGENLayerFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.model.CrossSectionTrack;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.GeometryUtils;
import org.dive4elements.river.utils.MapfileGenerator;


public class FacetCreator implements FacetTypes {

    public static final String I18N_WSPLGEN_RESULT    = "floodmap.uesk";
    public static final String I18N_WSPLGEN_DEFAULT   = "floodmap.uesk";
    public static final String I18N_BARRIERS          = "floodmap.barriers";
    public static final String I18N_BARRIERS_DEFAULT  = "floodmap.barriers";
    public static final String I18N_USERSHAPE         = "floodmap.usershape";
    public static final String I18N_USERSHAPE_DEFAULT = "floodmap.usershape";

    protected D4EArtifact artifact;

    protected CallContext  cc;

    protected List<Facet> facets;
    protected List<Facet> tmpFacets;

    protected String url;
    protected String hash;
    protected String stateId;


    private static Logger log = LogManager.getLogger(FacetCreator.class);


    public FacetCreator(
        D4EArtifact artifact,
        CallContext  cc,
        String       hash,
        String       sId,
        List<Facet>  facets
    ) {
        this.tmpFacets  = new ArrayList<Facet>(2);
        this.facets     = facets;
        this.artifact   = artifact;
        this.cc         = cc;
        this.hash       = hash;
        this.stateId    = sId;
    }

    // TODO We have RiverUtils and will have RiverAccess to do this
    protected String getRiver() {
        return artifact.getDataAsString("river");
    }

    protected String getUrl() {
        return RiverUtils.getUserWMSUrl();
    }

    protected String getSrid() {
        return RiverUtils.getRiverSrid(artifact);
    }

    protected Envelope getWSPLGENBounds() {
        String river = getRiver();
        RangeAccess rangeAccess = new RangeAccess(artifact);
        double kms[] = rangeAccess.getKmRange();

        log.debug("### getWSPLGENBounds");
        log.debug("###    from km: " + kms[0]);
        log.debug("###    to   km: " + kms[1]);

        CrossSectionTrack a =
            CrossSectionTrack.getCrossSectionTrack(river, kms[0]);

        CrossSectionTrack b =
            CrossSectionTrack.getCrossSectionTrack(river, kms[1]);

        if (a == null || b == null) {
            return null;
        }

        Envelope envA = a.getGeom().getEnvelopeInternal();
        Envelope envB = b.getGeom().getEnvelopeInternal();

        envA.expandToInclude(envB);
        envA = GeometryUtils.transform(envA, getSrid());

        log.debug("###    => " + envA);

        return envA;
    }

    protected Envelope getBounds() {
        return GeometryUtils.getRiverBoundary(getRiver());
    }

    public List<Facet> getFacets() {
        return tmpFacets;
    }

    public void createWSPLGENFacet() {
        String river = getRiver();
        RangeAccess rangeAccess = new RangeAccess(artifact);
        double kms[] = rangeAccess.getKmRange();

        WSPLGENLayerFacet wsplgen = new WSPLGENLayerFacet(
            0,
            FLOODMAP_WSPLGEN,
            Resources.format(
                cc.getMeta(),
                I18N_WSPLGEN_RESULT,
                I18N_WSPLGEN_DEFAULT,
                river,
                kms[0], kms[1]),
            ComputeType.ADVANCE,
            stateId,
            hash,
            getUrl());

        Envelope bounds = getWSPLGENBounds();

        if (bounds == null) {
            bounds = getBounds();
        }

        wsplgen.addLayer(
            MapfileGenerator.MS_WSPLGEN_PREFIX + artifact.identifier());
        wsplgen.setSrid(getSrid());
        wsplgen.setOriginalExtent(bounds);
        wsplgen.setExtent(bounds);

        tmpFacets.add(wsplgen);
    }

    public void createBarrierFacet() {
        WMSLayerFacet barriers = new WMSLayerFacet(
            1,
            FLOODMAP_BARRIERS,
            Resources.getMsg(
                cc.getMeta(),
                I18N_BARRIERS,
                I18N_BARRIERS_DEFAULT),
            ComputeType.ADVANCE,
            stateId,
            hash,
            getUrl());

        barriers.addLayer(
            MapfileGenerator.MS_BARRIERS_PREFIX + artifact.identifier());

        barriers.setSrid(getSrid());
        barriers.setExtent(getBounds());

        tmpFacets.add(barriers);
    }


    public void createShapeFacet(
        String desc,
        String layer,
        String type,
        int ndx) {
        WMSLayerFacet shape = new WMSLayerFacet(
            1,
            type,
            Resources.getMsg(
                cc.getMeta(),
                desc,
                I18N_USERSHAPE_DEFAULT),
            ComputeType.ADVANCE,
            stateId,
            hash,
            getUrl());

        shape.addLayer(
            layer + artifact.identifier());
        shape.setSrid(getSrid());
        shape.setExtent(getBounds());

        tmpFacets.add(shape);
    }


    public void finish() {
        facets.addAll(getFacets());
    }

    public void createExportFacet(String type) {
        ShapeFacet facet = new ShapeFacet(type, type);
        tmpFacets.add(facet);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
