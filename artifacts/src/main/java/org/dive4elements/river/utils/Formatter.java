/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Locale;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.resources.Resources;


/** Helper to access static i18n Formatters. */
public final class Formatter {

    // KMS IN ERROR REPORTS.
    public static final int CALCULATION_REPORT_KM_MIN_DIGITS = 1;
    public static final int CALCULATION_REPORT_KM_MAX_DIGITS = 3;

    // WATERLEVEL FORMATTER CONSTANTS
    public static final int WATERLEVEL_KM_MIN_DIGITS = 3;
    public static final int WATERLEVEL_KM_MAX_DIGITS = 3;
    public static final int WATERLEVEL_W_MIN_DIGITS  = 0;
    public static final int WATERLEVEL_W_MAX_DIGITS  = 2;
    public static final int WATERLEVEL_Q_MIN_DIGITS  = 0;
    public static final int WATERLEVEL_Q_MAX_DIGITS  = 2;


    // COMPUTED DISCHARGE CURVE FORMATTER CONSTANTS
    public static final int COMPUTED_DISCHARGE_W_MIN_DIGITS  = 2;
    public static final int COMPUTED_DISCHARGE_W_MAX_DIGITS  = 2;
    public static final int COMPUTED_DISCHARGE_Q_MIN_DIGITS  = 0;
    public static final int COMPUTED_DISCHARGE_Q_MAX_DIGITS  = 2;


    // HISTORICAL DISCHARGE CURVE FORMATTER CONSTANTS
    public static final int HISTORICAL_DISCHARGE_W_MIN_DIGITS = 0;
    public static final int HISTORICAL_DISCHARGE_W_MAX_DIGITS = 2;
    public static final int HISTORICAL_DISCHARGE_Q_MIN_DIGITS = 0;
    public static final int HISTORICAL_DISCHARGE_Q_MAX_DIGITS = 2;


    // DURATION CURVE FORMATTER CONSTANTS
    public static final int DURATION_W_MIN_DIGITS = 0;
    public static final int DURATION_W_MAX_DIGITS = 2;
    public static final int DURATION_Q_MIN_DIGITS = 0;
    public static final int DURATION_Q_MAX_DIGITS = 1;
    public static final int DURATION_D_MIN_DIGITS = 0;
    public static final int DURATION_D_MAX_DIGITS = 0;


    // FLOW VELOCITY FORMATTER CONSTANTS
    public static final int FLOW_VELOCITY_KM_MIN_DIGITS     = 3;
    public static final int FLOW_VELOCITY_KM_MAX_DIGITS     = 3;
    public static final int FLOW_VELOCITY_VALUES_MIN_DIGITS = 2;
    public static final int FLOW_VELOCITY_VALUES_MAX_DIGITS = 2;
    public static final int FLOW_VELOCITY_Q_MIN_DIGITS      = 0;
    public static final int FLOW_VELOCITY_Q_MAX_DIGITS      = 2;


    // MIDDLE BED HEIGHT FORMATTER CONSTANTS
    public static final int MIDDLE_BED_HEIGHT_KM_MIN_DIGITS             = 3;
    public static final int MIDDLE_BED_HEIGHT_KM_MAX_DIGITS             = 3;
    public static final int MIDDLE_BED_HEIGHT_HEIGHT_MIN_DIGITS         = 3;
    public static final int MIDDLE_BED_HEIGHT_HEIGHT_MAX_DIGITS         = 3;
    public static final int MIDDLE_BED_HEIGHT_UNCERT_MIN_DIGITS         = 3;
    public static final int MIDDLE_BED_HEIGHT_UNCERT_MAX_DIGITS         = 3;
    public static final int MIDDLE_BED_HEIGHT_DATAGAP_MIN_DIGITS        = 2;
    public static final int MIDDLE_BED_HEIGHT_DATAGAP_MAX_DIGITS        = 2;
    public static final int MIDDLE_BED_HEIGHT_SOUNDING_WIDTH_MIN_DIGITS = 0;
    public static final int MIDDLE_BED_HEIGHT_SOUNDING_WIDTH_MAX_DIGITS = 0;
    public static final int MIDDLE_BED_HEIGHT_WIDTH_MIN_DIGITS          = 3;
    public static final int MIDDLE_BED_HEIGHT_WIDTH_MAX_DIGITS          = 3;

    public static final int FIX_DELTA_W_KM_MIN_DIGITS = 3;
    public static final int FIX_DELTA_W_KM_MAX_DIGITS = 3;
    public static final int FIX_DELTA_W_DELTA_W_MIN_DIGITS = 3;
    public static final int FIX_DELTA_W_DELTA_W_MAX_DIGITS = 3;
    public static final int FIX_DELTA_W_DELTA_Q_MIN_DIGITS  = 0;
    public static final int FIX_DELTA_W_DELTA_Q_MAX_DIGITS  = 2;

    public static final int VARIANCE_MIN_DIGITS = 3;
    public static final int VARIANCE_MAX_DIGITS = 3;

    // SQ Relation
    public static final int SQ_RELATION_KM_MIN_DIGITS = 2;
    public static final int SQ_RELATION_KM_MAX_DIGITS = 2;
    public static final int SQ_RELATION_A_MAX_DIGITS  = 2;
    public static final int SQ_RELATION_A_MIN_DIGITS  = 2;
    public static final int SQ_RELATION_B_MAX_DIGITS  = 3;
    public static final int SQ_RELATION_B_MIN_DIGITS  = 3;

    // OTHER
    public static final int CSV_DIAGRAM_DATA_MAX_DIGITS  = 3;
    public static final int CSV_DIAGRAM_DATA_MIN_DIGITS  = 3;

    /**
     * Creates a localized NumberFormatter with given range of decimal digits.
     * @param m CallMeta to find the locale.
     * @param min minimum number of decimal ("fraction") digits.
     * @param max maximum number of decimal ("fraction") digits.
     * @return A NumberFormat. Use #format(NUMBER) to get String representation
     *         of NUMBER.
     */
    public static NumberFormat getFormatter(CallMeta m, int min, int max){
        Locale       locale = Resources.getLocale(m);
        NumberFormat nf     = NumberFormat.getInstance(locale);

        nf.setMaximumFractionDigits(max);
        nf.setMinimumFractionDigits(min);

        return nf;
    }

    public static NumberFormat getFormatter(CallContext c, int min, int max){
        return getFormatter(c.getMeta(), min, max);
    }


    /**
     * Returns a number formatter with no max or min digits set.
     *
     * @param c The CallContext.
     *
     * @return a number formatter.
     */
    public static NumberFormat getRawFormatter(CallContext c) {
        Locale locale = Resources.getLocale(c.getMeta());
        return NumberFormat.getInstance(locale);
    }

    /**
     * Returns a formatter in engineering notation.
     */
    public static NumberFormat getEngFormatter(CallContext c) {
        NumberFormat nf = getRawFormatter(c);
        if (nf instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat)nf;
            df.applyPattern("##0.#####E0");
        }
        return nf;
    }

    /**
     * Returns a number formatter that uses an exponent after max digits.
     */
    public static NumberFormat getScientificFormater(
        CallContext c,
        int min,
        int max
    ) {
        NumberFormat nf = getRawFormatter(c);
        if (nf instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat)nf;
            df.applyPattern("0.0E0");
            df.setMaximumFractionDigits(max);
            df.setMinimumFractionDigits(min);
        }
        return nf;
    }


    /**
     * Returns a date formatter with SHORT style.
     */
    public static DateFormat getShortDateFormat(CallContext cc) {
        Locale locale = Resources.getLocale(cc.getMeta());
        return DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }


    /**
     * Returns a date formatter with MEDIUM style.
     */
    public static DateFormat getMediumDateFormat(CallContext cc) {
        Locale locale = Resources.getLocale(cc.getMeta());
        return DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    }


    /**
     * Returns the number formatter for kilometer values in waterlevel exports.
     *
     * @return the number formatter for kilometer values.
     */
    public static NumberFormat getWaterlevelKM(CallContext context) {
        return getFormatter(
                context,
                WATERLEVEL_KM_MIN_DIGITS,
                WATERLEVEL_KM_MAX_DIGITS);
    }

    /**
     * Returns the number formatter for data exported from diagram (not from
     * calculation.
     *
     * @return the number formatter for csv data from diagra.
     */
    public static NumberFormat getCSVFormatter(CallContext context) {
        return getFormatter(
                context,
                CSV_DIAGRAM_DATA_MIN_DIGITS,
                CSV_DIAGRAM_DATA_MAX_DIGITS);
    }

    public static NumberFormat getWaterlevelW(CallMeta meta) {
        return getFormatter(
                meta,
                WATERLEVEL_W_MIN_DIGITS,
                WATERLEVEL_W_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for W values in waterlevel exports.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getWaterlevelW(CallContext context) {
        return getFormatter(
                context,
                WATERLEVEL_W_MIN_DIGITS,
                WATERLEVEL_W_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for Q values in waterlevel exports.
     *
     * @return the number formatter for Q values.
     */
    public static NumberFormat getWaterlevelQ(CallContext context) {
        return getFormatter(
                context,
                WATERLEVEL_Q_MIN_DIGITS,
                WATERLEVEL_Q_MAX_DIGITS);
    }


    public static NumberFormat getWaterlevelQ(CallMeta meta) {
        return getFormatter(
                meta,
                WATERLEVEL_Q_MIN_DIGITS,
                WATERLEVEL_Q_MAX_DIGITS);
    }

    /**
     * Returns the number formatter for W values in exports of computed
     * discharge curves.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getComputedDischargeW(CallContext context) {
        return getFormatter(
                context,
                COMPUTED_DISCHARGE_W_MIN_DIGITS,
                COMPUTED_DISCHARGE_W_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for Q values in exports of computed
     * discharge curves.
     *
     * @return the number formatter for Q values.
     */
    public static NumberFormat getComputedDischargeQ(CallContext context) {
        return getFormatter(
                context,
                COMPUTED_DISCHARGE_Q_MIN_DIGITS,
                COMPUTED_DISCHARGE_Q_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for W values in exports of historical
     * discharge curves.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getHistoricalDischargeW(CallContext context) {
        return getFormatter(
                context,
                HISTORICAL_DISCHARGE_W_MIN_DIGITS,
                HISTORICAL_DISCHARGE_W_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for Q values in exports of historical
     * discharge curves.
     *
     * @return the number formatter for Q values.
     */
    public static NumberFormat getHistoricalDischargeQ(CallContext context) {
        return getFormatter(
                context,
                HISTORICAL_DISCHARGE_Q_MIN_DIGITS,
                HISTORICAL_DISCHARGE_Q_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for W values in duration curve exports.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getDurationW(CallContext context) {
        return getFormatter(
                context,
                DURATION_W_MIN_DIGITS,
                DURATION_W_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for Q values in duration curve exports.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getDurationQ(CallContext context) {
        return getFormatter(
                context,
                DURATION_Q_MIN_DIGITS,
                DURATION_Q_MAX_DIGITS);
    }


    /**
     * Returns the number formatter for D values in duration curve exports.
     *
     * @return the number formatter for W values.
     */
    public static NumberFormat getDurationD(CallContext context) {
        return getFormatter(
                context,
                DURATION_D_MIN_DIGITS,
                DURATION_D_MAX_DIGITS);
    }

    public static NumberFormat getCalculationKm(CallMeta meta) {
        return getFormatter(
                meta,
                CALCULATION_REPORT_KM_MIN_DIGITS,
                CALCULATION_REPORT_KM_MAX_DIGITS);
    }


    public static NumberFormat getFlowVelocityKM(CallContext context) {
        return getFormatter(
                context,
                FLOW_VELOCITY_KM_MIN_DIGITS,
                FLOW_VELOCITY_KM_MAX_DIGITS);
    }


    public static NumberFormat getFlowVelocityValues(CallContext context) {
        return getFormatter(
                context,
                FLOW_VELOCITY_VALUES_MIN_DIGITS,
                FLOW_VELOCITY_VALUES_MAX_DIGITS);
    }


    public static NumberFormat getFlowVelocityQ(CallContext context) {
        return getFormatter(
                context,
                FLOW_VELOCITY_Q_MIN_DIGITS,
                FLOW_VELOCITY_Q_MAX_DIGITS);
    }


    public static NumberFormat getMiddleBedHeightKM(CallContext context) {
        return getFormatter(
                context,
                MIDDLE_BED_HEIGHT_KM_MIN_DIGITS,
                MIDDLE_BED_HEIGHT_KM_MAX_DIGITS);
    }


    public static NumberFormat getMiddleBedHeightHeight(CallContext context) {
        return getFormatter(
                context,
                MIDDLE_BED_HEIGHT_HEIGHT_MIN_DIGITS,
                MIDDLE_BED_HEIGHT_HEIGHT_MAX_DIGITS);
    }


    public static NumberFormat getMiddleBedHeightUncert(CallContext context) {
        return getFormatter(
                context,
                MIDDLE_BED_HEIGHT_UNCERT_MIN_DIGITS,
                MIDDLE_BED_HEIGHT_UNCERT_MAX_DIGITS);
    }


    public static NumberFormat getMiddleBedHeightDataGap(CallContext context) {
        return getFormatter(
                context,
                MIDDLE_BED_HEIGHT_DATAGAP_MIN_DIGITS,
                MIDDLE_BED_HEIGHT_DATAGAP_MAX_DIGITS);
    }


    public static NumberFormat getMiddleBedHeightSounding(
        CallContext context
    ) {
        return getFormatter(
                context,
                MIDDLE_BED_HEIGHT_SOUNDING_WIDTH_MIN_DIGITS,
                MIDDLE_BED_HEIGHT_SOUNDING_WIDTH_MAX_DIGITS);
    }


    public static NumberFormat getFixDeltaWKM(CallContext context) {
        return getFormatter(
                context,
                FIX_DELTA_W_KM_MIN_DIGITS,
                FIX_DELTA_W_KM_MAX_DIGITS);
    }

    public static NumberFormat getFixDeltaWDeltaW(CallContext context) {
        return getFormatter(
                context,
                FIX_DELTA_W_DELTA_W_MIN_DIGITS,
                FIX_DELTA_W_DELTA_W_MAX_DIGITS);
    }

    public static NumberFormat getFixDeltaWQ(CallContext context) {
        return getFormatter(
                context,
                FIX_DELTA_W_DELTA_Q_MIN_DIGITS,
                FIX_DELTA_W_DELTA_Q_MAX_DIGITS);
    }

    public static NumberFormat getFixDeltaWW(CallContext context) {
        return getFormatter(
                context,
                FIX_DELTA_W_DELTA_W_MIN_DIGITS,
                FIX_DELTA_W_DELTA_W_MAX_DIGITS);
    }

    public static NumberFormat getVariance(CallContext context) {
        return getFormatter(
                context,
                VARIANCE_MIN_DIGITS,
                VARIANCE_MAX_DIGITS);
    }

    public static NumberFormat getSQRelationA(CallContext context) {
        return getScientificFormater(
                context,
                SQ_RELATION_A_MIN_DIGITS,
                SQ_RELATION_A_MAX_DIGITS);
    }

    public static NumberFormat getSQRelationB(CallContext context) {
        return getFormatter(
                context,
                SQ_RELATION_B_MIN_DIGITS,
                SQ_RELATION_B_MAX_DIGITS);
    }

    public static NumberFormat getSQRelationKM(CallContext context) {
        return getFormatter(
                context,
                SQ_RELATION_KM_MIN_DIGITS,
                SQ_RELATION_KM_MAX_DIGITS);
    }

    public static NumberFormat getMeterFormat(CallContext context) {
        return getFormatter(
                context,
                0,
                2);

    }

    public static DateFormat getDateFormatter(CallMeta m, String pattern) {
        Locale locale = Resources.getLocale(m);
        return new SimpleDateFormat(pattern, locale);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
