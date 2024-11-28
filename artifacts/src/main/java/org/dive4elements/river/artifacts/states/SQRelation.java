/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.SQRelationAccess;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.model.sq.SQCurveFacet;
import org.dive4elements.river.artifacts.model.sq.SQFractionResult;
import org.dive4elements.river.artifacts.model.sq.SQMeasurementFacet;
import org.dive4elements.river.artifacts.model.sq.SQOutlierCurveFacet;
import org.dive4elements.river.artifacts.model.sq.SQOutlierFacet;
import org.dive4elements.river.artifacts.model.sq.SQOutlierMeasurementFacet;
import org.dive4elements.river.artifacts.model.sq.SQRelationCalculation;
import org.dive4elements.river.artifacts.model.sq.SQResult;

import org.dive4elements.river.artifacts.resources.Resources;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class SQRelation extends DefaultState implements FacetTypes {

    private static Logger log = LogManager.getLogger(SQRelation.class);


    public static final String I18N_FACET_CURVE =
        "facet.sq_relation.curve";

    public static final String I18N_FACET_MEASUREMENTS =
        "facet.sq_relation.measurements";

    public static final String I18N_FACET_OUTLIERS =
        "facet.sq_relation.outliers";

    public static final String I18N_FACET_OUTLIER_CURVE =
        "facet.sq_relation.outlier.curve";

    public static final String I18N_FACET_OUTLIER_MEASUREMENT =
        "facet.sq_relation.outlier.measurement";

    public static final int CURVE_INDEX               = 0;
    public static final int MEASURREMENT_INDEX        = 1;
    public static final int OUTLIER_INDEX             = 2;
    public static final int OUTLIER_CURVE_INDEX       = 3;
    public static final int OUTLIER_MEASUREMENT_INDEX = 4;

    public static final String [][] FACET_NAMES = {
        { SQ_A_CURVE, SQ_B_CURVE, SQ_C_CURVE,
          SQ_D_CURVE, SQ_E_CURVE, SQ_F_CURVE, SQ_G_CURVE
        },
        { SQ_A_MEASUREMENT, SQ_B_MEASUREMENT, SQ_C_MEASUREMENT,
          SQ_D_MEASUREMENT, SQ_E_MEASUREMENT, SQ_F_MEASUREMENT,
          SQ_G_MEASUREMENT
        },
        { SQ_A_OUTLIER, SQ_B_OUTLIER, SQ_C_OUTLIER,
          SQ_D_OUTLIER, SQ_E_OUTLIER, SQ_F_OUTLIER, SQ_G_OUTLIER
        },
        { SQ_A_OUTLIER_CURVE, SQ_B_OUTLIER_CURVE, SQ_C_OUTLIER_CURVE,
          SQ_D_OUTLIER_CURVE, SQ_E_OUTLIER_CURVE, SQ_F_OUTLIER_CURVE,
          SQ_G_OUTLIER_CURVE
        },
        { SQ_A_OUTLIER_MEASUREMENT, SQ_B_OUTLIER_MEASUREMENT,
          SQ_C_OUTLIER_MEASUREMENT, SQ_D_OUTLIER_MEASUREMENT,
          SQ_E_OUTLIER_MEASUREMENT, SQ_F_OUTLIER_MEASUREMENT,
          SQ_G_OUTLIER_MEASUREMENT
        }
    };

    public static final String [][] OV_FACET_NAMES = {
        { SQ_A_CURVE_OV, SQ_B_CURVE_OV, SQ_C_CURVE_OV,
          SQ_D_CURVE_OV, SQ_E_CURVE_OV, SQ_F_CURVE_OV, SQ_G_CURVE_OV
        },
        { SQ_A_MEASUREMENT_OV, SQ_B_MEASUREMENT_OV, SQ_C_MEASUREMENT_OV,
          SQ_D_MEASUREMENT_OV, SQ_E_MEASUREMENT_OV, SQ_F_MEASUREMENT_OV,
          SQ_G_MEASUREMENT_OV
        },
        { SQ_A_OUTLIER_OV, SQ_B_OUTLIER_OV, SQ_C_OUTLIER_OV,
          SQ_D_OUTLIER_OV, SQ_E_OUTLIER_OV, SQ_F_OUTLIER_OV, SQ_G_OUTLIER_OV
        },
        { SQ_A_OUTLIER_CURVE_OV, SQ_B_OUTLIER_CURVE_OV, SQ_C_OUTLIER_CURVE_OV,
          SQ_D_OUTLIER_CURVE_OV, SQ_E_OUTLIER_CURVE_OV, SQ_F_OUTLIER_CURVE_OV,
          SQ_G_OUTLIER_CURVE_OV
        },
        { SQ_A_OUTLIER_MEASUREMENT_OV, SQ_B_OUTLIER_MEASUREMENT_OV,
          SQ_C_OUTLIER_MEASUREMENT_OV, SQ_D_OUTLIER_MEASUREMENT_OV,
          SQ_E_OUTLIER_MEASUREMENT_OV, SQ_F_OUTLIER_MEASUREMENT_OV,
          SQ_G_OUTLIER_MEASUREMENT_OV
        }
    };


    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "minfo",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    String name = facet.getName();

                    if (StringUtils.contains(
                        name, FACET_NAMES[CURVE_INDEX])
                    ||  StringUtils.contains(
                        name, FACET_NAMES[OUTLIER_INDEX])
                    ||  StringUtils.contains(
                        name, FACET_NAMES[MEASURREMENT_INDEX])
                    ||  StringUtils.contains(
                        name, OV_FACET_NAMES[CURVE_INDEX])
                    ||  StringUtils.contains(
                        name, OV_FACET_NAMES[OUTLIER_INDEX])
                    ||  StringUtils.contains(
                        name, OV_FACET_NAMES[MEASURREMENT_INDEX])
                    ) {
                        // TODO: Only the last should be active.
                        return Boolean.TRUE;
                    }

                    if (StringUtils.contains(
                        name, FACET_NAMES[OUTLIER_CURVE_INDEX])
                     || StringUtils.contains(
                        name, FACET_NAMES[OUTLIER_MEASUREMENT_INDEX])
                     || StringUtils.contains(
                         name, OV_FACET_NAMES[OUTLIER_CURVE_INDEX])
                     || StringUtils.contains(
                         name, OV_FACET_NAMES[OUTLIER_MEASUREMENT_INDEX])
                    ) {
                        return Boolean.FALSE;
                    }

                    return null;
                }
            });
    }


    public SQRelation() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("SQRelation.computeAdvance");

        SQRelationAccess sqAccess = new SQRelationAccess(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : new SQRelationCalculation(sqAccess).calculate();

        if (facets == null) {
            return res;
        }

        SQResult [] sqr = (SQResult [])res.getData();
        if (sqr == null) {
            return res;
        }

        createFacets(context, facets, sqAccess, sqr, hash);

        Facet csv = new DataFacet(
            CSV, "CSV data", ComputeType.ADVANCE, hash, id);

        Facet pdf = new DataFacet(
            PDF, "PDF data", ComputeType.ADVANCE, hash, id);

        facets.add(csv);
        facets.add(pdf);

        return res;
    }


    protected void createFacets(
        CallContext context,
        List<Facet> container,
        SQRelationAccess access,
        SQResult[]  sqr,
        String      hash
    ) {
        boolean debug = log.isDebugEnabled();

        CallMeta meta    = context.getMeta();
        String   stateId = getID();
        for (int res = 0, n = sqr.length; res < n; res++) {

            for (int i = 0; i < SQResult.NUMBER_FRACTIONS; i++) {
                SQFractionResult result = sqr[res].getFraction(i);

                if (result == null) {
                    log.warn("Fraction at index " + i + " is empty!");
                    continue;
                }

                container.add(new SQCurveFacet(
                    res,
                    i,
                    getFractionFacetname(CURVE_INDEX, i),
                    Resources.getMsg(
                        meta,
                        I18N_FACET_CURVE,
                        new Object[] {
                            access.getPeriod().getFrom(),
                            access.getPeriod().getTo()
                            }
                    ),
                    hash,
                    stateId
                ));

                container.add(new SQCurveFacet(
                    res,
                    i,
                    getFractionOverviewFacetname(CURVE_INDEX, i),
                    sqr[res].getFractionName(i) + " - " +
                    Resources.getMsg(
                        meta,
                        I18N_FACET_CURVE,
                        new Object[] {
                            access.getPeriod().getFrom(),
                            access.getPeriod().getTo()
                            }
                    ),
                    hash,
                    stateId
                ));

                for (int j = 0, C = result.numIterations()-1; j < C; j++) {

                    Object [] round = new Object [] { j + 1 };

                    int index = res;
                    index     = index << 16;
                    index     = index + j;

                    if (debug) {
                        log.debug("new outliers facet (index=" +index+ ")");
                        log.debug("   result index = " + res);
                        log.debug("   fraction idx = " + i);
                        log.debug("   iteration    = " + j);
                    }

                    container.add(new SQOutlierFacet(
                        index,
                        i,
                        getFractionFacetname(OUTLIER_INDEX, i),
                            Resources.getMsg(
                                meta,
                                I18N_FACET_OUTLIERS,
                                round
                            ),
                        hash,
                        stateId
                    ));
                    container.add(new SQOutlierFacet(
                        index,
                        i,
                        getFractionOverviewFacetname(OUTLIER_INDEX, i),
                        sqr[res].getFractionName(i) + " - " +
                        Resources.getMsg(
                            meta,
                            I18N_FACET_OUTLIERS,
                            round
                        ),
                        hash,
                        stateId
                    ));

                    container.add(new SQOutlierCurveFacet(
                        index,
                        i,
                        getFractionFacetname(OUTLIER_CURVE_INDEX, i),
                        Resources.getMsg(
                            meta,
                            I18N_FACET_OUTLIER_CURVE,
                            round
                        ),
                        hash,
                        stateId
                    ));
                    container.add(new SQOutlierCurveFacet(
                        index,
                        i,
                        getFractionOverviewFacetname(OUTLIER_CURVE_INDEX, i),
                        sqr[res].getFractionName(i) + " - " +
                        Resources.getMsg(
                            meta,
                            I18N_FACET_OUTLIER_CURVE,
                            round
                        ),
                        hash,
                        stateId
                    ));

                    container.add(new SQOutlierMeasurementFacet(
                        index,
                        i,
                        getFractionFacetname(OUTLIER_MEASUREMENT_INDEX, i),
                        Resources.getMsg(
                            meta,
                            I18N_FACET_OUTLIER_MEASUREMENT,
                            round
                        ),
                        hash,
                        stateId
                    ));
                    container.add(new SQOutlierMeasurementFacet(
                        index,
                        i,
                        getFractionOverviewFacetname(
                            OUTLIER_MEASUREMENT_INDEX, i),
                        sqr[res].getFractionName(i) + " - " +
                        Resources.getMsg(
                            meta,
                            I18N_FACET_OUTLIER_MEASUREMENT,
                            round
                        ),
                        hash,
                        stateId
                    ));
                } // for all outliers

                container.add(new SQMeasurementFacet(
                    res,
                    i,
                    getFractionFacetname(MEASURREMENT_INDEX, i),
                    Resources.getMsg(
                        meta,
                        I18N_FACET_MEASUREMENTS,
                        new Object[] {
                            access.getPeriod().getFrom(),
                            access.getPeriod().getTo()
                            }
                    ),
                    hash,
                    stateId
                ));
                container.add(new SQMeasurementFacet(
                    res,
                    i,
                    getFractionOverviewFacetname(MEASURREMENT_INDEX, i),
                    sqr[res].getFractionName(i) + " - " +
                    Resources.getMsg(
                        meta,
                        I18N_FACET_MEASUREMENTS,
                        new Object[] {
                            access.getPeriod().getFrom(),
                            access.getPeriod().getTo()
                            }
                    ),
                    hash,
                    stateId
                ));
            } // for all fractions
        } // for all results
    }

    protected static String getFractionFacetname(int type, int idx) {
        if (log.isDebugEnabled()) {
            log.debug("getFractionFacetname(): " + type + " | " + idx);
        }
        type %= FACET_NAMES.length;
        return FACET_NAMES[type][idx % FACET_NAMES[type].length];
    }

    protected static String getFractionOverviewFacetname(int type, int idx) {
        if (log.isDebugEnabled()) {
            log.debug("getFractionOverviewFacetname(): " + type + " | " + idx);
        }
        type %= OV_FACET_NAMES.length;
        return OV_FACET_NAMES[type][idx % OV_FACET_NAMES[type].length];
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
