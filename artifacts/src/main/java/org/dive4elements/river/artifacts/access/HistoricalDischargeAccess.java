/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import java.util.Date;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.Timerange;


public class HistoricalDischargeAccess extends RiverAccess {

    public static enum EvaluationMode {
        W(0), Q(1);

        private final int mode;

        EvaluationMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    public static final String DATA_EVALUATION_TIME = "year_range";
    public static final String DATA_EVALUATION_MODE = "historical_mode";
    public static final String DATA_INPUT_VALUES    = "historical_values";
    public static final String DATA_REFERENCE_GAUGE = "reference_gauge";

    private Timerange evaluationTimerange;
    private EvaluationMode evaluationMode;

    private double[] qs;
    private double[] ws;

    private Long officialGaugeNumber;

    public HistoricalDischargeAccess(D4EArtifact artifact) {
        super(artifact);
    }

    /**
     * This method returns the evaluation mode. The evaluation mode W is set, if
     * the <b>DATA_EVALUATION_MODE</b> is 0. Otherwise, the evaluation mode Q is
     * set.
     *
     * @return EvaluationMode.W if the parameter <i>historical_mode</i> is set
     *         to 0, otherwise EvaluationMode.Q.
     */
    public EvaluationMode getEvaluationMode() {
        if (evaluationMode == null) {
            int mode = getInteger(DATA_EVALUATION_MODE);
            evaluationMode = mode == 0 ? EvaluationMode.W : EvaluationMode.Q;
        }

        return evaluationMode;
    }


    /**
     * This method returns the time range specified by <i>year_range</i>
     * parameter. This parameter has to be a string that consists of two long
     * values (time millis since 1970) separated by a ';'.
     *
     * @return the evaluation time range specified by <i>year_range</i>.
     */
    public Timerange getEvaluationTimerange() {
        if (evaluationTimerange == null) {
            long[] startend = getLongArray(DATA_EVALUATION_TIME);

            if (startend != null && startend.length > 1) {
                Date start = new Date(startend[0]);
                Date end = new Date(startend[1]);

                evaluationTimerange = new Timerange(start, end);
                evaluationTimerange.sort();
            }
        }

        return evaluationTimerange;
    }

    /**
     * This method returns the input Q values if the evaluation mode Q is set.
     * Otherwise, this method will return a double array of length 0. The values
     * returned by this method are extracted from string parameter
     * <i>historical_values</i>.
     *
     * @return the input Q values or a double array of length 0.
     */
    public double[] getQs() {
        if (qs == null) {
            if (getEvaluationMode() == EvaluationMode.Q) {
                qs = getDoubleArray(DATA_INPUT_VALUES);
            }
            else {
                qs = new double[0];
            }
        }

        return qs;
    }

    /**
     * This method returns the input W values if the evaluation mode W is set.
     * Otherwise, this method will return a double array of length 0. The values
     * returned by this method are extracted from string parameter
     * <i>historical_values</i>.
     *
     * @return the input W values or a double array of length 0.
     */
    public double[] getWs() {
        if (ws == null) {
            if (getEvaluationMode() == EvaluationMode.W) {
                ws = getDoubleArray(DATA_INPUT_VALUES);
            }
            else {
                ws = new double[0];
            }
        }

        return ws;
    }

    public Long getOfficialGaugeNumber() {
        if (officialGaugeNumber == null) {
            officialGaugeNumber = getLong(DATA_REFERENCE_GAUGE);
        }
        return officialGaugeNumber;
    }
}
