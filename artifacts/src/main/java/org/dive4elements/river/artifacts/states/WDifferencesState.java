/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.ChartArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.FixationArtifact;
import org.dive4elements.river.artifacts.MINFOArtifact;
import org.dive4elements.river.artifacts.StaticWKmsArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.math.WKmsOperation;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.DifferenceCurveFacet;
import org.dive4elements.river.artifacts.model.DifferenceCurveFilterFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.artifacts.model.fixings.FixRealizingResult;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.backend.utils.StringUtil;

/** State of a WINFOArtifact to get differences of data of other artifacts. */
public class WDifferencesState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(WDifferencesState.class);

    private static final String I18N_DIFFERENCES_FACET_NAME =
        "facet.w_differences";
    private static final String I18N_DIFFERENCES_FACET_NAME_RAW =
        "facet.w_differences.raw";

    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "winfo",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    String name = facet.getName();

                    if (name.equals(FacetTypes.W_DIFFERENCES)) {
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
            });
    }

    /** Specify to display nothing (this is kind of a "final" state). */
    @Override
    protected String getUIProvider() {
        return "noinput";
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;
        if (artifact instanceof ChartArtifact) {
            return true;
        }

        StateData data = flys.getData("diffids");

        if (data == null) {
            throw new IllegalArgumentException("diffids is empty");
        }

        // TODO: Also validate format.

        return true;
    }


    /**
     * Access the data (wkms) of an artifact, coded in mingle.
     */
    public WKms getWKms(
        String mingle,
        CallContext context,
        double from,
        double to
    ) {
        log.debug("WDifferencesState.getWKms " + mingle);
        String[] def  = mingle.split(";");
        String   uuid = def[0];
        String   name = def[1];
        int      idx  = Integer.parseInt(def[2]);
        D4EArtifact d4eArtifact = RiverUtils.getArtifact(
            uuid,
            context);

        if (d4eArtifact == null) {
            log.warn("One of the artifacts (1) for diff calculation "
                + "could not be loaded");
            return null;
        }

        WKms retval = null;
        if (d4eArtifact instanceof StaticWKmsArtifact) {
            StaticWKmsArtifact staticWKms = (StaticWKmsArtifact) d4eArtifact;
            log.debug("WDifferencesState obtain data from StaticWKms");
            WKms wkms = staticWKms.getWKms(idx, from, to);
            if (wkms == null) {
                log.error("No WKms from Static artifact for this range.");
                return new WQKms();
            }
            return wkms; /* No need for additional km filtering */
        } else if (d4eArtifact instanceof WINFOArtifact) {
            log.debug("Get WKms from WINFOArtifact");
            WINFOArtifact flys = (WINFOArtifact) d4eArtifact;

            WKms[] wkms = (WKms[]) flys.getWaterlevelData(context).
                                              getData();
            if (wkms == null || wkms.length == 0) {
                log.warn("no waterlevels in artifact");
            }
            else if (wkms.length < idx+1) {
                log.warn("Not enough waterlevels in artifact.");
                retval = new WQKms();
            }
            retval = wkms[idx];
        } else if (d4eArtifact instanceof MINFOArtifact) {
            log.warn("Get WKms from MINFOArtifact not implemented!");
//            CalculationResult r = (CalculationResult)
//                d4eArtifact.compute(context, ComputeType.ADVANCE, false);
        } else if (d4eArtifact instanceof FixationArtifact) {
            log.debug ("Get WKms from FixationArtifact.");
            CalculationResult r = (CalculationResult)
                d4eArtifact.compute(context, ComputeType.ADVANCE, false);
            FixRealizingResult frR = (FixRealizingResult) r.getData();
            retval = frR.getWQKms()[idx];
        }


        if (retval == null) {
            log.error(
                "Do not know how to handle (getWKms) minuend/subtrahend");
        } else if (!Double.isNaN(from) && !Double.isNaN(to)) {
            /* Filter out only relevant data points for calulation results.*/
            log.debug("Before filter: " + retval.size());
            retval = retval.filteredKms(from, to);
            log.debug("After filter: " + retval.size());
        }

        return retval;
    }


    /**
     * Return CalculationResult with Array of WKms that are difference of
     * Waterlevels. Add respective facets (DifferencesCurveFacet, DataFacet).
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        if (artifact instanceof ChartArtifact) {
            ChartArtifact chart = (ChartArtifact)artifact;
            facets.add(new EmptyFacet());
            return null;
        }
        WINFOArtifact winfo = (WINFOArtifact) artifact;
        String id = getID();
        RangeAccess rangeAccess = new RangeAccess(artifact);
        double from = rangeAccess.getFrom(true);
        double to = rangeAccess.getTo(true);
        // Load the Artifacts/facets that we want to subtract and display.
        // Expected format is:
        //[42537f1e-3522-42ef-8968-635b03d8e9c6;longitudinal_section.w;0]#[...]
        String diffids = winfo.getDataAsString("diffids");
        log.debug("WDifferencesState has: " + diffids);
        String datas[] = diffids.split("#");

        log.debug("Difference from: " + from + " to: " + to);
        /* Check if we need to obtain the data in a different order */
        // Validate the Data-Strings.
        for (String s: datas) {
            if (!WaterlevelSelectState.isValueValid(s)) {
                // TODO: escalate.
            }
        }

        if (datas.length < 2) {
            // TODO crash with style
        }

        List<WKms> wkmss = new ArrayList<WKms>();

        for(int i = 0; i < datas.length; i+=2) {
            // e.g.:
            // 42537f1e-3522-42ef-8968-635b03d8e9c6;longitudinal_section.w;1
            WKms minuendWKms = getWKms(StringUtil.unbracket(datas[i+0]),
                context, from, to);
            WKms subtrahendWKms = getWKms(StringUtil.unbracket(datas[i+1]),
                context, from, to);

            String facetName = "diff ()";
            String minName = "min";
            String subName = "sub";

            if (minuendWKms != null && subtrahendWKms != null) {
                minName = StringUtil.wWrap(minuendWKms.getName());
                subName = StringUtil.wWrap(subtrahendWKms.getName());
                facetName = minName + " - " + subName;
                WKms wkms = WKmsOperation.SUBTRACTION.operate(minuendWKms,
                     subtrahendWKms);
                wkms.setName(facetName);
                wkmss.add(wkms);
                log.debug("WKMSSubtraction happened");
            }
            if (facets != null) {
                facets.add(new DifferenceCurveFacet(
                    i/2,
                    W_DIFFERENCES,
                    Resources.getMsg(
                        context.getMeta(),
                        I18N_DIFFERENCES_FACET_NAME_RAW,
                        facetName,
                        new Object[] { minName, subName }),
                    ComputeType.ADVANCE,
                    id,
                    hash));
                facets.add(new DifferenceCurveFilterFacet(i/2,
                    W_DIFFERENCES_FILTERED,
                    Resources.getMsg(
                        context.getMeta(),
                        I18N_DIFFERENCES_FACET_NAME,
                        facetName,
                        new Object[] { minName, subName }),
                    ComputeType.ADVANCE,
                    id,
                    hash));
            }
        }

        if (facets != null) {
            facets.add(new DataFacet(CSV, "CSV data"));
            facets.add(new DataFacet(PDF, "PDF data"));
            log.debug("Adding facets in WDifferencesState.");
        }
        else {
            log.debug("Not adding facets in WDifferencesState.");
        }

        // TODO Evaluate whether null is okay as reports.
        WKms[] diffs = wkmss.toArray(new WKms[wkmss.size()]);
        CalculationResult result = new CalculationResult(diffs, null);
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
