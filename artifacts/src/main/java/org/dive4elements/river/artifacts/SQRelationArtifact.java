/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.model.sq.StaticSQRelation;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.StaticState;
import org.dive4elements.river.artifacts.states.sq.SQStaticState;


public class SQRelationArtifact
extends StaticD4EArtifact
{
    private static final Logger log =
        LogManager.getLogger(SQRelationArtifact.class);

    public static final String XPATH_RIVER = "/art:action/art:river/@art:name";
    public static final String XPATH_STATION =
        "/art:action/art:measurement_station/@art:number";
    public static final String NAME = "sqrelationdatacage";
    public static final String STATIC_STATE_NAME = "state.sqrelation.static";
    public static final String UIPROVIDER = "static_sqrelation";
    public static final String SQ_RELATION_OUT_A = "sq_relation_a";
    public static final String SQ_RELATION_OUT_B = "sq_relation_b";
    public static final String SQ_RELATION_OUT_C = "sq_relation_c";
    public static final String SQ_RELATION_OUT_D = "sq_relation_d";
    public static final String SQ_RELATION_OUT_E = "sq_relation_e";
    public static final String SQ_RELATION_OUT_F = "sq_relation_f";


    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callmeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("SQRelationArtifact.setup()");

        String code = getDatacageIDValue(data);

        log.debug("SQRelationDCArtifact.setup Id: " + code);

        if (code != null && !code.isEmpty()) {
            /* Case that we were instantiated from the datacage */
            addStringData("ids", code);
            super.setup(
                identifier, factory, context, callmeta, data, loadFacets);
            return;
        }

        String river = XMLUtils.xpathString(
            data,
            XPATH_RIVER,
            ArtifactNamespaceContext.INSTANCE);
        String station = XMLUtils.xpathString(
            data,
            XPATH_STATION,
            ArtifactNamespaceContext.INSTANCE);

        addData(
            "river",
            new DefaultStateData(
                "river",
                Resources.getMsg(callmeta, "static.sq.river", "Rivername"),
                "String",
                river));
        addData(
            "station",
            new DefaultStateData(
                "station",
                Resources.getMsg(callmeta, "static.sq.station", "Station"),
                "String",
                station));
        super.setup(identifier, factory, context, callmeta, data, loadFacets);
        // When we are in this case we did not come from the datacage
        // e.g. had an ID string set. So we also did not have a template
        // set and initialize is not called. So we have to do this ourself.
        initialize(this, context, callmeta);
    }

    @Override
    protected void initialize(
        Artifact artifact,
        Object   context,
        CallMeta callMeta
    ) {
        StaticState state = new SQStaticState(STATIC_STATE_NAME);

        List<Facet> fs = new ArrayList<Facet>();
        state.computeInit(this, hash(), context, callMeta, fs);
        log.debug("Init static state computed facets");
        for (Facet face: fs) {
            log.debug("Got a facet with name: " + face.getName());
        }

        if (hasParameter(StaticSQRelation.Parameter.A, fs)) {
            DefaultOutput outputA = new DefaultOutput(
                SQ_RELATION_OUT_A,
                "output.static.sqrelation.a",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputA);
        }
        if (hasParameter(StaticSQRelation.Parameter.B, fs)) {
            DefaultOutput outputB = new DefaultOutput(
                SQ_RELATION_OUT_B,
                "output.static.sqrelation.b",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputB);
        }
        if (hasParameter(StaticSQRelation.Parameter.C, fs)) {
            DefaultOutput outputC = new DefaultOutput(
                SQ_RELATION_OUT_C,
                "output.static.sqrelation.c",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputC);
        }
        if (hasParameter(StaticSQRelation.Parameter.D, fs)) {
            DefaultOutput outputD = new DefaultOutput(
                SQ_RELATION_OUT_D,
                "output.static.sqrelation.d",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputD);
        }
        if (hasParameter(StaticSQRelation.Parameter.E, fs)) {
            DefaultOutput outputE = new DefaultOutput(
                SQ_RELATION_OUT_E,
                "output.static.sqrelation.e",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputE);
        }
        if (hasParameter(StaticSQRelation.Parameter.F, fs)) {
            DefaultOutput outputF = new DefaultOutput(
                SQ_RELATION_OUT_F,
                "output.static.sqrelation.f",
                "image/png",
                fs,
                "chart");
            state.addOutput(outputF);
        }
        addFacets(STATIC_STATE_NAME, fs);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private boolean hasParameter(
        StaticSQRelation.Parameter p,
        List<Facet> fs
    ) {
        for (Facet f : fs) {
            if (f.getName().equals("sq_" +
                p.toString().toLowerCase() + "_curve")) {
                return true;
            }
        }
        return false;
    }
}
