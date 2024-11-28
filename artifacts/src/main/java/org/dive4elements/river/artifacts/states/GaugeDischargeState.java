/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.ChartArtifact;
import org.dive4elements.river.artifacts.GaugeDischargeArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.GaugeDischargeFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.CalculationResult;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.TimeInterval;

/**
 * The only state for an GaugeDischargeState (River and km known).
 */
public class GaugeDischargeState
extends      DefaultState
implements   FacetTypes
{
    /** Developer-centric description of facet. */
    public static final String I18N_DESCRIPTION = "facet.discharge.curve";

    /** The log that is used in this state. */
    private static final Logger log =
        LogManager.getLogger(GaugeDischargeState.class);


    /**
     * Create i18ned name for gaugedischargeFacet.
     * @param artifact The artifact which has information about the gauge.
     * @param meta used for i18n.
     * @return localized name for gaugedischargefacet.
     */
    protected String createFacetName(GaugeDischargeArtifact artifact,
        CallMeta meta) {

        if (artifact.getFacetWishName() != null) {
            /* We let the Artifact overwrite our name as this allows
             * injecting the facet name from the Datacage */
            return artifact.getFacetWishName();
        }

        Gauge gauge = artifact.getGauge();
        TimeInterval validity = gauge.fetchMasterDischargeTable()
            .getTimeInterval();
        Date stopTime = validity.getStopTime();
        String name = Resources.getMsg(
            meta,
            "chart.discharge.curve.model"
            + (stopTime != null ? "" : ".nostop"),
            new Object[] {gauge.getName(),
                          validity.getStartTime(),
                          stopTime
                }
        );

        return name;
    }


    /**
     * Add an GaugeDischargeFacet to list of Facets.
     *
     * @param artifact Ignored.
     * @param hash Ignored.
     * @param context Ignored.
     * @param meta CallMeta to be used for internationalization.
     * @param facets List to add Facet to.
     *
     * @return null.
     */
    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        log.debug("GaugeDischargeState.computeInit()");

        GaugeDischargeFacet facet = new GaugeDischargeFacet(
            0,
            DISCHARGE_CURVE,
            createFacetName((GaugeDischargeArtifact) artifact, meta));

        facets.add(facet);

        return null;
    }


    /**
     * 'Calculate' Discharge at Gauge.
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        if (artifact instanceof GaugeDischargeArtifact) {
            log.debug("GaugeDischargeState.computeAdvance()");
            GaugeDischargeArtifact dischargeArtifact =
                (GaugeDischargeArtifact)artifact;

            CalculationResult res;


            if (old instanceof CalculationResult) {
                res = (CalculationResult) old;
            }
            else {
                res = dischargeArtifact.getDischargeCurveData();
            }

            WQKms[] wqkms = (WQKms[]) res.getData();

            if (wqkms != null && facets != null) {
                log.debug(
                    "GaugeDischargeState.computeAdvance(): create facets");

                GaugeDischargeFacet facet = new GaugeDischargeFacet(
                    0,
                    DISCHARGE_CURVE,
                    createFacetName(dischargeArtifact, context.getMeta()));

                facets.add(facet);

                if (res.getReport().hasProblems()) {
                    facets.add(new ReportFacet());
                }
            }
            else {
                if (wqkms == null)
                    log.debug(
                        "GaugeDischargeState.computeAdvance(): wqkms 0");
                else
                    log.debug(
                        "GaugeDischargeState.computeAdvance(): facets 0");
            }

            return res;
        }
        else if (artifact instanceof ChartArtifact) {
            ChartArtifact chart = (ChartArtifact)artifact;
            facets.add(new EmptyFacet());
            return null;
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
