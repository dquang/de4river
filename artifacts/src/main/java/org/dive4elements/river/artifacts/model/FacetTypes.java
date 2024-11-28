/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

/** 'Types' of facets. */
public interface FacetTypes {

    /** Static helper class to determine if a Facet name is of a generalized
     * 'type'. */
    public static class IS {
        public static boolean WQ_KM(String type) {
           return type.equals(DISCHARGE_LONGITUDINAL_W)
               || type.equals(LONGITUDINAL_W);
        }
        public static boolean W_KM(String type) {
            return type.equals(STATIC_WKMS)
               || type.equals(HEIGHTMARKS_POINTS)
               || WQ_KM(type);
        }
        public static boolean AREA(String type) {
            return type.equals(AREA)
                || type.equals(CROSS_SECTION_AREA)
                || type.equals(LONGITUDINAL_SECTION_AREA);
        }
        public static boolean W(String type) {
            return type.equals(LONGITUDINAL_W)
                || type.equals(DISCHARGE_LONGITUDINAL_W)
                || type.equals(DURATION_W)
                || type.equals(STATIC_WKMS)
                || type.equals(STATIC_WQKMS_W);
        }
        public static boolean Q(String type) {
            return type.equals(LONGITUDINAL_Q)
                || type.equals(DISCHARGE_LONGITUDINAL_Q)
                || type.equals(DURATION_Q)
                || type.equals(STATIC_WQKMS_Q);
        }
        public static boolean V(String type) {
            return type.equals(FLOW_VELOCITY_MAINCHANNEL)
                || type.equals(FLOW_VELOCITY_TOTALCHANNEL);
        }
        public static boolean T(String type) {
            return type.equals(FLOW_VELOCITY_TAU);
        }
        public static boolean H(String type) {
            return type.equals(MIDDLE_BED_HEIGHT_SINGLE);
        }
        public static boolean MANUALPOINTS(String type) {
            return type.endsWith("manualpoints");
        }
        public static boolean MANUALLINE(String type) {
            return type.endsWith("manualline");
        }
        public static boolean FILTERED(String type) {
            return type.endsWith("filtered");
        }
        public static boolean SQ_CURVE(String type) {
            if (type.equals(SQ_A_CURVE)
                || type.equals(SQ_B_CURVE)
                || type.equals(SQ_C_CURVE)
                || type.equals(SQ_D_CURVE)
                || type.equals(SQ_E_CURVE)
                || type.equals(SQ_F_CURVE)
                || type.equals(SQ_G_CURVE)
                || type.equals(SQ_A_OUTLIER_CURVE)
                || type.equals(SQ_B_OUTLIER_CURVE)
                || type.equals(SQ_C_OUTLIER_CURVE)
                || type.equals(SQ_D_OUTLIER_CURVE)
                || type.equals(SQ_E_OUTLIER_CURVE)
                || type.equals(SQ_F_OUTLIER_CURVE)
                || type.equals(SQ_G_OUTLIER_CURVE)
                || type.equals(SQ_A_CURVE_OV)
                || type.equals(SQ_B_CURVE_OV)
                || type.equals(SQ_C_CURVE_OV)
                || type.equals(SQ_D_CURVE_OV)
                || type.equals(SQ_E_CURVE_OV)
                || type.equals(SQ_F_CURVE_OV)
                || type.equals(SQ_G_CURVE_OV)
                || type.equals(SQ_A_OUTLIER_CURVE_OV)
                || type.equals(SQ_B_OUTLIER_CURVE_OV)
                || type.equals(SQ_C_OUTLIER_CURVE_OV)
                || type.equals(SQ_D_OUTLIER_CURVE_OV)
                || type.equals(SQ_E_OUTLIER_CURVE_OV)
                || type.equals(SQ_F_OUTLIER_CURVE_OV)
                || type.equals(SQ_G_OUTLIER_CURVE_OV)
                )
            {
                return true;
            }

            return false;
        }
        public static boolean SQ_MEASUREMENT(String type) {
            if (type.equals(SQ_A_MEASUREMENT)
                || type.equals(SQ_B_MEASUREMENT)
                || type.equals(SQ_C_MEASUREMENT)
                || type.equals(SQ_D_MEASUREMENT)
                || type.equals(SQ_E_MEASUREMENT)
                || type.equals(SQ_F_MEASUREMENT)
                || type.equals(SQ_G_MEASUREMENT)
                || type.equals(SQ_A_OUTLIER_MEASUREMENT)
                || type.equals(SQ_B_OUTLIER_MEASUREMENT)
                || type.equals(SQ_C_OUTLIER_MEASUREMENT)
                || type.equals(SQ_D_OUTLIER_MEASUREMENT)
                || type.equals(SQ_E_OUTLIER_MEASUREMENT)
                || type.equals(SQ_F_OUTLIER_MEASUREMENT)
                || type.equals(SQ_G_OUTLIER_MEASUREMENT)
                || type.equals(SQ_A_MEASUREMENT_OV)
                || type.equals(SQ_B_MEASUREMENT_OV)
                || type.equals(SQ_C_MEASUREMENT_OV)
                || type.equals(SQ_D_MEASUREMENT_OV)
                || type.equals(SQ_E_MEASUREMENT_OV)
                || type.equals(SQ_F_MEASUREMENT_OV)
                || type.equals(SQ_G_MEASUREMENT_OV)
                || type.equals(SQ_A_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_B_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_C_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_D_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_E_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_F_OUTLIER_MEASUREMENT_OV)
                || type.equals(SQ_G_OUTLIER_MEASUREMENT_OV)
                )
            {
                return true;
            }

            return false;
        }
        public static boolean SQ_OUTLIER(String type) {
            if (type.equals(SQ_A_OUTLIER)
                || type.equals(SQ_B_OUTLIER)
                || type.equals(SQ_C_OUTLIER)
                || type.equals(SQ_D_OUTLIER)
                || type.equals(SQ_E_OUTLIER)
                || type.equals(SQ_F_OUTLIER)
                || type.equals(SQ_G_OUTLIER)
                || type.equals(SQ_A_OUTLIER_OV)
                || type.equals(SQ_B_OUTLIER_OV)
                || type.equals(SQ_C_OUTLIER_OV)
                || type.equals(SQ_D_OUTLIER_OV)
                || type.equals(SQ_E_OUTLIER_OV)
                || type.equals(SQ_F_OUTLIER_OV)
                || type.equals(SQ_G_OUTLIER_OV))
            {
                return true;
            }

            return false;
        }
    };

    /** Available diagram/chart-types.  This enum is evaluated at certain
     * places to iterate over chart-types to e.g. dynamically generate
     * Facet-names (ManualPoints for example). */
    public enum ChartType {
        FD("fix_derivate_curve"),
        LS("longitudinal_section"),
        CS("cross_section"),
        DLS("discharge_longitudinal_section"),
        CDC("computed_discharge_curve"),
        DUC("duration_curve"),
        DIC("discharge_curve"),
        RC("reference_curve"),
        RCN("reference_curve_normalized"),
        WD("wdifferences"),
        BHDY("bedheight_difference_height_year"),
        BDY("bed_difference_year"),
        FWQC("fix_wq_curve"),
        FDWC("fix_deltawt_curve"),
        FLSC("fix_longitudinal_section_curve"),
        FDC("fix_derivate_curve"),
        EWQ("extreme_wq_curve"),
        BHM("bedheight_middle"),
        BLS("bed_longitudinal_section"),
        SLS("sedimentload_ls"),
        FV("flow_velocity"),
        SQA("sq_relation_a"),
        SQB("sq_relation_b"),
        W_D("w_differences"),
        SQC("sq_relation_c"),
        SQD("sq_relation_d"),
        SQE("sq_relation_e"),
        SQF("sq_relation_f"),
        HD("historical_discharge"),
        HDWQ("historical_discharge_wq");

        private String chartTypeString;

        ChartType(String description) {
            this.chartTypeString = description;
        }

        @Override
        public String toString() {
            return chartTypeString;
        }
    }

    String AREA                        = "area";
    String CROSS_SECTION_AREA          = "cross_section.area";
    String LONGITUDINAL_SECTION_AREA   = "longitudinal_section.area";

    String FLOODMAP_WSPLGEN            = "floodmap.wsplgen";
    String FLOODMAP_BARRIERS           = "floodmap.barriers";
    String FLOODMAP_USERSHAPE          = "floodmap.usershape";
    String FLOODMAP_RIVERAXIS          = "floodmap.riveraxis";
    @Deprecated
    String FLOODMAP_WMSBACKGROUND      = "floodmap.wmsbackground";
    String FLOODMAP_KMS                = "floodmap.kms";
    String FLOODMAP_QPS                = "floodmap.qps";
    String FLOODMAP_HWS_LINES          = "floodmap.hws_lines";
    String FLOODMAP_HWS_POINTS         = "floodmap.hws_points";
    String FLOODMAP_HYDR_BOUNDARY      = "floodmap.hydr_boundaries";
    String FLOODMAP_HYDR_BOUNDARY_POLY = "floodmap.hydr_boundaries_poly";
    String FLOODMAP_CATCHMENT          = "floodmap.catchment";
    String FLOODMAP_FLOODPLAIN         = "floodmap.floodplain";
    String FLOODMAP_LINES              = "floodmap.lines";
    String FLOODMAP_BUILDINGS          = "floodmap.buildings";
    String FLOODMAP_FIXPOINTS          = "floodmap.fixpoints";
    String FLOODMAP_FLOODMARKS         = "floodmap.floodmarks";
    String FLOODMAP_FLOODMAPS          = "floodmap.floodmaps";
    String FLOODMAP_GAUGE_LOCATION     = "floodmap.gauge_location";
    String FLOODMAP_EXTERNAL_WMS       = "floodmap.externalwms";
    String FLOODMAP_JETTIES            = "floodmap.jetties";

    String DISCHARGE_LONGITUDINAL_W = "discharge_longitudinal_section.w";
    String DISCHARGE_LONGITUDINAL_Q = "discharge_longitudinal_section.q";
    String DISCHARGE_LONGITUDINAL_Q_INFOLD =
        "discharge_longitudinal_section.q.infolding";
    String DISCHARGE_LONGITUDINAL_Q_INFOLD_CUT =
        "discharge_longitudinal_section.q.cutting";
    String DISCHARGE_LONGITUDINAL_C = "discharge_longitudinal_section.c";

    String LONGITUDINAL_W = "longitudinal_section.w";
    String LONGITUDINAL_Q = "longitudinal_section.q";
    String LONGITUDINAL_ANNOTATION   = "longitudinal_section.annotations";
    String LONGITUDINAL_MANUALPOINTS = "longitudinal_section.manualpoints";

    String W_DIFFERENCES = "w_differences";
    String W_DIFFERENCES_FILTERED = "w_differences.filtered";

    String COMPUTED_DISCHARGE_Q = "computed_discharge_curve.q";

    String MAINVALUES_Q = "mainvalues.q";
    String MAINVALUES_W = "mainvalues.w";

    String CROSS_SECTION = "cross_section";
    String CROSS_SECTION_WATER_LINE = "cross_section_water_line";

    String HYK = "hyk";

    String DISCHARGE_CURVE = "discharge_curve.curve";
    String GAUGE_DISCHARGE_CURVE = "gauge_discharge_curve";
    String GAUGE_DISCHARGE_CURVE_AT_EXPORT = "gauge_discharge_curve_at_export";

    String DURATION_W = "duration_curve.w";
    String DURATION_Q = "duration_curve.q";

    String MANUALPOINTS = "manualpoints";
    String MANUALLINE = "manualline";

    String QSECTOR        = "qsectors";

    String STATIC_DELTA_W    = "other.delta_w";
    String STATIC_DELTA_W_CMA= "other.delta_w_cma";
    String STATIC_WQ         = "other.wq";
    String STATIC_WQ_ANNOTATIONS = "other.wq.annotations";
    String STATIC_WKMS       = "other.wkms";
    String STATIC_WKMS_MARKS = "other.wkms.marks";
    String STATIC_WQKMS      = "other.wqkms";
    String STATIC_WQKMS_W    = "other.wqkms.w";
    String STATIC_WQKMS_Q    = "other.wqkms.q";
    String STATIC_WKMS_INTERPOL = "other.wkms.interpol";
    String STATIC_W_INTERPOL = "other.w.interpol";

    String HEIGHTMARKS_POINTS = "heightmarks_points";

    String CSV = "csv";
    String WST = "wst";
    String AT  = "at";
    String PDF = "pdf";

    String REPORT = "report";

    String HISTORICAL_DISCHARGE_Q      = "historical_discharge.historicalq";
    String HISTORICAL_DISCHARGE_Q_DIFF =
        "historical_discharge.historicalq.diff";
    String HISTORICAL_DISCHARGE_W      = "historical_discharge.historicalw";
    String HISTORICAL_DISCHARGE_W_DIFF =
        "historical_discharge.historicalw.diff";
    String HISTORICAL_DISCHARGE_WQ_Q   = "historical_discharge.wq.q";
    String HISTORICAL_DISCHARGE_WQ_W   = "historical_discharge.wq.w";

    String REFERENCE_CURVE            = "reference_curve";
    String REFERENCE_CURVE_NORMALIZED = "reference_curve_normalized";

    String FLOW_VELOCITY_MAINCHANNEL           = "flow_velocity.mainchannel";
    String FLOW_VELOCITY_TOTALCHANNEL          = "flow_velocity.totalchannel";
    /** Also called SHEAR_STRESS. */
    String FLOW_VELOCITY_TAU                   = "flow_velocity.tau";
    String FLOW_VELOCITY_MAINCHANNEL_FILTERED  =
        "flow_velocity.mainchannel.filtered";
    String FLOW_VELOCITY_TOTALCHANNEL_FILTERED =
        "flow_velocity.totalchannel.filtered";
    String FLOW_VELOCITY_TAU_FILTERED          = "flow_velocity.tau.filtered";
    String FLOW_VELOCITY_ANNOTATION            = "flow_velocity.annotation";
    String FLOW_VELOCITY_MEASUREMENT           = "flow_velocity.measurement";
    String FLOW_VELOCITY_DISCHARGE             = "flow_velocity.discharge";
    String FLOW_VELOCITY_WATERLEVEL            = "flow_velocity.waterlevel";

    String MIDDLE_BED_HEIGHT_SINGLE     = "bedheight_middle.single";
    String MIDDLE_BED_HEIGHT_ANNOTATION = "bedheight_middle.annotation";

    String BED_QUALITY_DATA_FACET = "bed_longitudinal_section";
    String BED_QUALITY_POROSITY_TOPLAYER =
        BED_QUALITY_DATA_FACET + ".porosity.toplayer";
    String BED_QUALITY_POROSITY_SUBLAYER =
        BED_QUALITY_DATA_FACET + ".porosity.sublayer";
    String BED_QUALITY_BED_DIAMETER_TOPLAYER =
        BED_QUALITY_DATA_FACET + ".diameter.toplayer";
    String BED_QUALITY_BED_DIAMETER_SUBLAYER =
        BED_QUALITY_DATA_FACET + ".diameter.sublayer";
    String BED_QUALITY_SEDIMENT_DENSITY_TOPLAYER =
        BED_QUALITY_DATA_FACET + ".density.toplayer";
    String BED_QUALITY_SEDIMENT_DENSITY_SUBLAYER =
        BED_QUALITY_DATA_FACET + ".density.sublayer";
    String BED_QUALITY_BEDLOAD_DIAMETER =
        BED_QUALITY_DATA_FACET + ".diameter.bedload";
    String BED_DIAMETER_DATA_TOP =
        BED_QUALITY_DATA_FACET + ".diameter.toplayer.data";
    String BED_DIAMETER_DATA_SUB =
        BED_QUALITY_DATA_FACET + ".diameter.sublayer.data";
    String BEDLOAD_DIAMETER_DATA =
        BED_QUALITY_DATA_FACET + ".diameter.bedload.data";
    String POROSITY = "porosity";

    String BED_DIFFERENCE_YEAR = "bedheight_difference.year";
    String BED_DIFFERENCE_YEAR_FILTERED = "bedheight_difference.year.filtered";
    String BED_DIFFERENCE_HEIGHT_YEAR = "bedheight_difference.height_year";
    String BED_DIFFERENCE_HEIGHT_YEAR_FILTERED =
        "bedheight_difference.height_year.filtered";
    String BED_DIFFERENCE_YEAR_HEIGHT1 = "bedheight_difference.year.height1";
    String BED_DIFFERENCE_YEAR_HEIGHT2 = "bedheight_difference.year.height2";
    String BED_DIFFERENCE_YEAR_HEIGHT1_FILTERED =
        "bedheight_difference.year.height1.filtered";
    String BED_DIFFERENCE_YEAR_HEIGHT2_FILTERED =
        "bedheight_difference.year.height2.filtered";

    String MORPHOLOGIC_WIDTH = "morph-width";

    String SEDIMENT_DENSITY = "sediment.density";

    String SQ_OVERVIEW = "sq_overview";

    String SQ_A_CURVE       = "sq_a_curve";
    String SQ_A_MEASUREMENT = "sq_a_measurement";
    String SQ_A_OUTLIER     = "sq_a_outlier";
    String SQ_A_OUTLIER_CURVE = "sq_a_outlier_curve";
    String SQ_A_OUTLIER_MEASUREMENT = "sq_a_outlier_measurement";

    String SQ_B_CURVE       = "sq_b_curve";
    String SQ_B_MEASUREMENT = "sq_b_measurement";
    String SQ_B_OUTLIER     = "sq_b_outlier";
    String SQ_B_OUTLIER_CURVE  = "sq_b_outlier_curve";
    String SQ_B_OUTLIER_MEASUREMENT  = "sq_b_outlier_measurement";

    String SQ_C_CURVE       = "sq_c_curve";
    String SQ_C_MEASUREMENT = "sq_c_measurement";
    String SQ_C_OUTLIER     = "sq_c_outlier";
    String SQ_C_OUTLIER_CURVE = "sq_c_outlier_curve";
    String SQ_C_OUTLIER_MEASUREMENT = "sq_c_outlier_measurement";

    String SQ_D_CURVE       = "sq_d_curve";
    String SQ_D_MEASUREMENT = "sq_d_measurement";
    String SQ_D_OUTLIER     = "sq_d_outlier";
    String SQ_D_OUTLIER_CURVE = "sq_d_outlier_curve";
    String SQ_D_OUTLIER_MEASUREMENT = "sq_d_outlier_measurement";

    String SQ_E_CURVE       = "sq_e_curve";
    String SQ_E_MEASUREMENT = "sq_e_measurement";
    String SQ_E_OUTLIER     = "sq_e_outlier";
    String SQ_E_OUTLIER_CURVE = "sq_e_outlier_curve";
    String SQ_E_OUTLIER_MEASUREMENT = "sq_e_outlier_curve_measurement";

    String SQ_F_CURVE       = "sq_f_curve";
    String SQ_F_MEASUREMENT = "sq_f_measurement";
    String SQ_F_OUTLIER     = "sq_f_outlier";
    String SQ_F_OUTLIER_CURVE = "sq_f_outlier_curve";
    String SQ_F_OUTLIER_MEASUREMENT = "sq_f_outlier_measurement";

    String SQ_G_CURVE       = "sq_g_curve";
    String SQ_G_MEASUREMENT = "sq_g_measurement";
    String SQ_G_OUTLIER     = "sq_g_outlier";
    String SQ_G_OUTLIER_CURVE = "sq_g_outlier_curve";
    String SQ_G_OUTLIER_MEASUREMENT = "sq_g_outlier_measurement";

    String SQ_A_CURVE_OV       = "sq_a_curve_overview";
    String SQ_A_MEASUREMENT_OV = "sq_a_measurement_overview";
    String SQ_A_OUTLIER_OV     = "sq_a_outlier_overview";
    String SQ_A_OUTLIER_CURVE_OV = "sq_a_outlier_curve_overview";
    String SQ_A_OUTLIER_MEASUREMENT_OV = "sq_a_outlier_measurement_overview";

    String SQ_B_CURVE_OV       = "sq_b_curve_overview";
    String SQ_B_MEASUREMENT_OV = "sq_b_measurement_overview";
    String SQ_B_OUTLIER_OV     = "sq_b_outlier_overview";
    String SQ_B_OUTLIER_CURVE_OV  = "sq_b_outlier_curve_overview";
    String SQ_B_OUTLIER_MEASUREMENT_OV  = "sq_b_outlier_measurement_overview";

    String SQ_C_CURVE_OV       = "sq_c_curve_overview";
    String SQ_C_MEASUREMENT_OV = "sq_c_measurement_overview";
    String SQ_C_OUTLIER_OV     = "sq_c_outlier_overview";
    String SQ_C_OUTLIER_CURVE_OV = "sq_c_outlier_curve_overview";
    String SQ_C_OUTLIER_MEASUREMENT_OV = "sq_c_outlier_measurement_overview";

    String SQ_D_CURVE_OV       = "sq_d_curve_overview";
    String SQ_D_MEASUREMENT_OV = "sq_d_measurement_overview";
    String SQ_D_OUTLIER_OV     = "sq_d_outlier_overview";
    String SQ_D_OUTLIER_CURVE_OV = "sq_d_outlier_curve_overview";
    String SQ_D_OUTLIER_MEASUREMENT_OV = "sq_d_outlier_measurement_overview";

    String SQ_E_CURVE_OV       = "sq_e_curve_overview";
    String SQ_E_MEASUREMENT_OV = "sq_e_measurement_overview";
    String SQ_E_OUTLIER_OV     = "sq_e_outlier_overview";
    String SQ_E_OUTLIER_CURVE_OV = "sq_e_outlier_curve_overview";
    String SQ_E_OUTLIER_MEASUREMENT_OV =
        "sq_e_outlier_curve_measurement_overview";

    String SQ_F_CURVE_OV       = "sq_f_curve_overview";
    String SQ_F_MEASUREMENT_OV = "sq_f_measurement_overview";
    String SQ_F_OUTLIER_OV     = "sq_f_outlier_overview";
    String SQ_F_OUTLIER_CURVE_OV = "sq_f_outlier_curve_overview";
    String SQ_F_OUTLIER_MEASUREMENT_OV = "sq_f_outlier_measurement_overview";

    String SQ_G_CURVE_OV       = "sq_g_curve_overview";
    String SQ_G_MEASUREMENT_OV = "sq_g_measurement_overview";
    String SQ_G_OUTLIER_OV     = "sq_g_outlier_overview";
    String SQ_G_OUTLIER_CURVE_OV = "sq_g_outlier_curve_overview";
    String SQ_G_OUTLIER_MEASUREMENT_OV = "sq_g_outlier_measurement_overview";

    String RELATIVE_POINT = "relativepoint";

    String FIX_ANALYSIS_EVENTS_DWT = "fix_analysis_events_dwt";
    String FIX_ANALYSIS_EVENTS_LS = "fix_analysis_events_ls";
    String FIX_ANALYSIS_EVENTS_WQ = "fix_analysis_events_wq";

    String FIX_EVENTS = "fix_events_wqkms";

    String FIX_REFERENCE_EVENTS_DWT = "fix_reference_events_dwt";
    String FIX_REFERENCE_EVENTS_LS = "fix_reference_events_ls";
    String FIX_REFERENCE_EVENTS_WQ = "fix_reference_events_wq";
    String FIX_REFERENCE_PERIOD_DWT = "fix_reference_period_dwt";

    // Note that AVERAGE_DWT will get a postfix (e.g. ..._dwt_1)
    String FIX_SECTOR_AVERAGE_DWT = "fix_sector_average_dwt";
    String FIX_SECTOR_AVERAGE_LS = "fix_sector_average_ls";
    String FIX_SECTOR_AVERAGE_WQ = "fix_sector_average_wq";
    String FIX_SECTOR_AVERAGE_LS_DEVIATION = "fix_sector_average_ls_deviation";

    String FIX_WQ_CURVE = "fix_wq_curve";
    String FIX_WQ_LS = "fix_wq_ls";
    String FIX_OUTLIER = "fix_outlier";

    String FIX_ANALYSIS_PERIODS_DWT = "fix_analysis_periods_dwt";
    String FIX_ANALYSIS_PERIODS_LS = "fix_analysis_periods_ls";
    String FIX_ANALYSIS_PERIODS_WQ = "fix_analysis_periods_wq";

    String FIX_DERIVATE_CURVE = "fix_derivate_curve";

    String FIX_DEVIATION_DWT = "fix_deviation_dwt";
    String FIX_DEVIATION_LS = "fix_deviation_ls";

    String FIX_PARAMETERS = "fix_parameters";

    String STATIC_BEDHEIGHT = "static_bedheight";

    String BEDHEIGHT                = "bedheight";
    String BEDHEIGHT_SOUNDING_WIDTH = "bedheight_sounding_width";
    String BEDHEIGHT_WIDTH          = "bedheight_width";

    String EXTREME_WQ_CURVE = "extreme_wq_curve";

    String EXTREME_WQ_CURVE_BASE = "extreme_wq_curve_base";
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
