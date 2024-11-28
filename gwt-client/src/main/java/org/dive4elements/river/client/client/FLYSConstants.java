/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface FLYSConstants extends ConstantsWithLookup {

    String static_sqrelation();

    String add();

    String unexpected_exception();

    String title();

    String fullname();

    String user();

    String guest();

    String projects();

    String open_project();

    String delete_project();

    String rename_project();

    String clone_project();

    String copy_of ();

    String manage_projects();

    String favorite_tooltip();

    String favorite_tooltip_meaning();

    String projectlist_creationTime();

    String projectlist_title();

    String projectlist_favorite();

    String really_delete();

    String project_name_too_long();

    String logout();

    String switch_language();

    String info();

    String warning();

    String warning_language();

    String warning_no_wsp_selected();

    String warning_cannot_parse_date();

    String no_projects();

    String load_projects();

    String empty_table();

    String empty_filter();

    String date_format();

    String datetime_format();

    String new_project();

    String new_calculation();

    String module_selection();

    String river_selection();

    String winfo();

    String minfo();

    String map();

    String new_map();

    String new_chart();

    String diagram();

    String axes();

    String legend();

    String wms_legend();

    String chart_title();

    String chart_subtitle();

    String grid();

    String antialiasing();

    String axis_name();

    String chart_start();

    String chart_end();

    String x_axis();

    String y1_axis();

    String y2_axis();

    String y3_axis();

    String legend_name();

    String show_legend();

    String aggregation_threshold();

    String scale();

    String databasket();

    String databasket_loading();

    String theme_top();

    String theme_up();

    String theme_down();

    String theme_bottom();

    String zoom_all();

    String zoom_in();

    String zoom_out();

    String zoom_back();

    String properties_ico();

    String pan();

    String askThemeRemove();

    String fix();

    String fixanalysis();

    String next();

    String river_km();

    String uesk_profile_distance();

    String location_distance_state();

    String distance_state();

    String waterlevel_ground_state();

    String location();

    String locations();

    String single_location();

    String distance();

    String unitFrom();

    String unitTo();

    String dpLabelFrom();

    String dpUnitFrom();

    String dpLabelTo();

    String dpUnitTo();

    String dpLabelStep();

    String dpUnitStep();

    String wgLabelFrom();

    String wgUnitFrom();

    String wgLabelTo();

    String wgUnitTo();

    String wgLabelStep();

    String wgUnitStep();

    String unitWidth();

    String unitFromInM();

    String unitToInM();

    String unitDiffInM();

    String unitLocation();

    String wrongFormat();

    String toShouldNotBeNegative();

    String atLeastOneValue();

    String missingInput();

    String too_many_values ();

    String from();

    String to();

    String riverside();

    String calcTableTitle();

    String helperPanelTitle();

    String gaugePanelTitle();

    String measurementStationPanelTitle();

    String wqTitle();

    String wqadaptedTitle();

    String noMainValueAtGauge();

    String wqHistorical();

    String unitWNN();

    String wqWFree();

    String wqW();

    String wqQ();

    String wqQatGauge();

    String wqQGauge();

    String wqSingle();

    String wqRange();

    String unitWSingle();

    String unitWFree();

    String unitWFrom();

    String unitWTo();

    String unitWStep();

    String unitQSingle();

    String unitQFrom();

    String unitQTo();

    String unitQStep();

    String main_channel();

    String total_channel();

    String footerHome();

    String footerContact();

    String footerImpressum();

    String projectListMin();

    String projectListAdd();

    String buttonNext();

    String imageBack();

    String imageSave();

    String search();

    String properties();

    String activateTheme();

    String deactivateTheme();

    String removeTheme();

    String manageThemes();

    String label_ok();

    String label_cancel();

    String cancelCalculationLabel();

    String calculationCanceled();

    String flysLogo();

    String bfgLogo();

    String bfgLogoSmall();

    String downloadPNG();

    String downloadPDF();

    String downloadSVG();

    String downloadCSV();

    String downloadAT();

    String downloadWST();

    String loadingImg();

    String cancelCalculation();

    String markerRed();

    String markerGreen();

    String riverMap();

    String range();

    String description();

    String resultCount();

    String bfg_id();

    String start_year();

    String end_year();

    String period();

    String gauge_class();

    String eventselect();

    String events();

    String kmchart();

    String addPointsTooltip();

    String addWSPTooltip();

    String downloadPNGTooltip();

    String downloadPDFTooltip();

    String downloadSVGTooltip();

    String downloadCSVTooltip();

    String zoomToMaxExtentTooltip();

    String zoomOutTooltip();

    String historyBackTooltip();

    String panControlTooltip();

    String zoomboxTooltip();

    String chartPropertiesTooltip();

    String year();

    String sedimentload_ls();

    // Gauges

    String gauge_mnq();

    String gauge_mq();

    String gauge_mhq();

    String gauge_hq5();

    // Elevation window

    String ele_window_title();

    String ele_window_label();

    String ele_window_x_col();

    String ele_window_y_col();

    String ele_window_z_col();

    String ele_window_ok_button();

    String ele_window_cancel_button();

    String ele_window_format_error();

    String ele_window_save_error();

    String ele_window_geometry_error();

    // OUTPUT TYPES

    String discharge_curve_gaugeless();

    String discharge_curve();

    String gauge_discharge_curve();

    String computed_discharge_curve();

    String computed_discharge_curves();

    String longitudinal_section();

    String duration_curve();

    String reference_curve();

    String reference_curves();

    String reference_curve_normalized();

    String reference_endpoint();

    String reference_startpoint();

    String name();

    String type();

    String starttime();

    String stoptime();

    String wq_table_w();

    String wq_waterlevel_label();

    String wq_table_q();

    String wq_value_w();

    String wq_value_q();

    String discharge_longitudinal_section();

    String floodmap();

    String cross_section();

    String cross_sections();

    String w_differences();

    String historical_discharge();

    String historical_discharge_wq();

    String extreme_wq_curve();

    String showextramark();

    String fix_wq_curve();

    String fix_deltawt_curve();

    String fix_longitudinal_section_curve();

    String fix_derivate_curve();

    String fix_vollmer_wq_curve();

    // EXPORTS

    String waterlevel_export();

    String waterlevel_report();

    String computed_dischargecurve_at_export();

    String discharge_longitudinal_section_export();

    String discharge_longitudinal_section_report();

    String computed_dischargecurve_export();

    String computed_dischargecurve_report();

    String durationcurve_export();

    String durationcurve_report();

    String dataexport();

    String reference_curve_export();

    String w_differences_export();

    String historical_discharge_export();

    String csv();

    String wst();

    String at();

    String pdf();

    String chart_themepanel_header_themes();

    String chart_themepanel_header_actions();

    String chart_themepanel_synchron();

    String chart_themepanel_asynchron();

    String chart_themepanel_set_master();

    String chart_themepanel_new_area();

    String chart_themepanel_area_under();

    String chart_themepanel_area_over();

    String chart_themepanel_area_between();

    String against_x_axis();

    String discharge();

    String flow_velocity();

    String flow_velocities();

    String flow_velocity_export();

    String bedheight_middle();

    String bedheight_middle_export();

    String bed_longitudinal_section();

    String bed_longitudinal_section_export();

    String sq_relation_a();

    String sq_relation_b();

    String sq_relation_c();

    String sq_relation_d();

    String sq_relation_e();

    String sq_relation_f();

    String sq_relation_g();

    String sq_relation_export();

    String sq_relations();

    String exportATTooltip();

    String load_diameter();

    String bed_diameter();

    String soundings();

    String soundings_width();

    String porosities();

    String bed_difference_year();

    String bed_difference_epoch();

    String bed_difference_height_year();

    String bedheight_difference_export();

    String fix_waterlevel_export();

    // ERRORS

    String error_years_wrong();

    String error_read_minmax_values();

    String error_validate_range();

    String error_validate_date_range();

    String error_validate_date_range_invalid();

    String error_validate_lower_range();

    String error_validate_upper_range();

    String error_validate_positive();

    String error_create_artifact();

    String error_describe_artifact();

    String error_feed_data();

    String error_advance_artifact();

    String error_add_artifact();

    String error_remove_artifact();

    String error_create_collection();

    String error_describe_collection();

    String error_no_rivers_found();

    String error_no_gaugeoverviewinfo_found();

    String error_no_such_user();

    String error_no_users();

    String error_no_waterlevel_pair_selected();

    String error_same_waterlevels_in_pair();

    String error_not_logged_in();

    String error_load_parameterization();

    String error_feed_no_data();

    String error_feed_from_out_of_range();

    String error_feed_to_out_of_range();

    String error_feed_from_bigger_to();

    String error_feed_invalid_wq_mode();

    String error_feed_number_format_float();

    String error_feed_invalid_calculation_mode();

    String error_feed_no_calculation_mode();

    String error_feed_no_such_river();

    String error_feed_no_river_selected();

    String error_feed_no_wq_mode_selected();

    String error_feed_q_values_invalid();

    String error_feed_w_values_invalid();

    String error_no_meta_data_found();

    String error_chart_info_service();

    String error_invalid_double_value();

    String error_load_artifact();

    String error_no_calc_result();

    String error_no_theme_styles_found();

    String error_no_feature_selected();

    String error_no_map_config();

    String error_no_map_output_type();

    String error_no_module_found();

    String warning_use_first_feature();

    String warning_select_two_values();

    String error_no_valid_gfi_url();

    String error_gfi_req_failed();

    String error_gfi_parsing_failed();

    String error_gc_req_failed();

    String error_gc_doc_not_valid();

    String error_malformed_url();

    String error_no_dgm_selected();

    String error_invalid_dgm_selected();

    String error_bad_dgm_range();

    String error_bad_dgm_river();

    String error_dialog_not_valid();

    String error_invalid_date();

    String error_wrong_date();

    String bottom_edge();

    String top_edge();

    String error_same_location();

    String error_contains_same_location();

    String error_update_collection_attribute();

    String error_values_needed();

    // MAP RELATED STRINGS

    String digitize();

    String pipe1();

    String pipe2();

    String ditch();

    String dam();

    String ring_dike();

    String selectFeature();

    String removeFeature();

    String getFeatureInfo();

    String getFeatureInfoTooltip();

    String getFeatureInfoWindowTitle();

    String addWMS();

    String printMapSettings();

    String addWMSTooltip();

    String adjustElevation();

    String adjustElevationTooltip();

    String measureLine();

    String measurePolygon();

    String step();

    String calculationStarted();

    String zoomMaxExtent();

    String zoomIn();

    String zoomOut();

    String zoomLayer();

    String moveMap();

    String digitizeObjects();

    String selectObject();

    String removeObject();

    String measureDistance();

    String measureArea();

    String map_themepanel_header_style();

    String zoomToLayer();

    String requireDGM();

    String upload_file();

    String shape_file_upload();

    // data cage

    String historical_discharges();

    String waterlevels();

    String waterlevels_discharge();

    String waterlevels_fix();

    String waterlevels_fix_vollmer();

    String waterlevels_ls();

    String beddifferences();

    String bedheight_differences();

    String middle_bedheights();

    String vollmer_waterlevels();

    String old_calculations();

    String officiallines();

    String datacageAdd();

    String fixations();

    String flood_protections();

    String columns();

    String basedata();

    String heightmarks();

    String annotation();

    String annotations();

    String all_annotations();

    String flowvelocitymeasurement();

    String flowvelocitymodel();

    String bed_quality();

    String additionals();

    String differences();

    String kilometrage();

    String riveraxis();

    String km();

    String qps();

    String hws();

    String catchments();

    String catchment_wms();

    String floodplain();

    String lines();

    String buildings();

    String fixpoints();

    String uesk();

    String calculations();

    String current();

    String bfg();

    String land();

    String potential();

    String rastermap();

    String background();

    String discharge_tables_chart();

    String discharge_table_nn();

    String discharge_table_gauge();

    String mainvalue();

    String mainvalues();

    String wmainvalue();

    String qmainvalue();

    String show_mainvalues();

    String dems();

    String hydrboundaries();

    String gaugelocations();

    String single();

    String epoch();

    String bedheights();

    String morph_width();

    String datacage();

    String datacage_add_pair();

    String delta_w();

    String delta_w_cm();

    String delta_w_cma();

    String wlevel();

    String sedimentloads();

    String sources_sinks();

    String years();

    String epochs();

    String off_epochs();

    String densities();

    String sediment_load();

    String sediment_load_ls();

    String measurement_stations();

    String coarse();

    String fine_middle();

    String sand();

    String susp_sand();

    String susp_sand_bed();

    String suspended_sediment();

    String total();

    String bed_load();

    String suspended_load();

    // Capabilities Information Panel

    String addwmsInputTitle();

    String addwmsInfoTitle();

    String addwmsLayerTitle();

    String addwmsBack();

    String addwmsContinue();

    String addwmsCancel();

    String addwmsInvalidURL();

    String capabilitiesHint();

    String capabilitiesTitle();

    String capabilitiesURL();

    String capabilitiesAccessConstraints();

    String capabilitiesFees();

    String capabilitiesContactInformation();

    String capabilitiesEmail();

    String capabilitiesPhone();

    String chart();

    String export();

    String width();

    String height();

    String visibility();

    String upper();

    String lower();

    String fixation();

    String font_size();

    String label();

    String subtitle();

    String display_grid();

    String display_logo();

    String logo_placeh();

    String logo_placev();

    String top();

    String bottom();

    String center();

    String left();

    String right();

    String none();

    String notselected();

    String linetype();

    String textstyle();

    String linecolor();

    String showhorizontalline();

    String showverticalline();

    String horizontal();

    String vertical();

    String textcolor();

    String textsize();

    String font();

    String showborder();

    String showpoints();

    String showbackground();

    String textshowbg();

    String backgroundcolor();

    String bandwidthcolor();

    String textbgcolor();

    String textorientation();

    String linesize();

    String pointsize();

    String bandwidth();

    String pointcolor();

    String showlines();

    String showlinelabel();

    String showpointlabel();

    String labelfontsize();

    String labelfontcolor();

    String labelfontface();

    String labelfontstyle();

    String labelbgcolor();

    String labelshowbg();

    String showwidth();

    String showlevel();

    String showminimum();

    String showmaximum();

    String transparent();

    String transparency();

    String showarea();

    String showarealabel();

    String showmiddleheight();

    String fillcolor();

    String wsplgen_cat1();

    String wsplgen_cat2();

    String wsplgen_cat3();

    String wsplgen_cat4();

    String wsplgen_cat5();

    String areabgcolor();

    String areashowborder();

    String areashowbg();

    String areabordercolor();

    String areatransparency();

    String attribution();

    // Manual Points editor

    String addpoints();

    String pointname();

    String removepoint();

    String newpoint();

    String standby();

    String points();

    String editpoints();

    String manual_date_points_y();

    // Manual WaterLine (WSP) Editor.

    String addWSPButton();

    String addWSP();

    String selection();

    String fix_deltawt_export();

    String select();

    String add_date();

    String fix_parameters_export();

    String fix_parameters();

    String sq_overview();

    // Gauge Overview Info

    String gauge_zero();

    String gauge_q_unit();

    String gauge_info_link();

    String gauge_river_info_link();

    String gauge_river_url();

    String gauge_url();

    String gauge_curve_link();

    String gauge_discharge_curve_at_export();

    // Measurement Station Info

    String measurement_station_type();

    String measurement_station_operator();

    String measurement_station_comment();

    String measurement_station_start_time();

    String measurement_station_url();

    String measurement_station_info_link();

    String measurement_station_gauge_name();

    String discharge_timeranges();

    String discharge_chart();

    String projectlist_close();

    String startcolor();

    String endcolor();

    String numclasses();

    String welcome();

    String welcome_open_or_create();

    String official();

    String inofficial();

    String custom_lines();

    String hws_lines();

    String hws_points();

    String hws_fed_unknown();

    String jetties();

    String route_data();

    String other();

    String axis();

    String bfg_model();

    String federal();

    String areas();

    String sobek_areas();

    String sobek_flooded();

    String measurements();

    String floodmarks();

    String pegel_had_measurement_points();

    String gauge_points();

    String gauge_names();

    String gauge_level();

    String gauge_tendency();

    String printTooltip();

    String print();

    String printWindowTitle();

    String mapfish_data_range();

    String mapfish_data_subtitle();

    String mapfish_data_strech();

    String mapfish_data_institution();

    String mapfish_data_source();

    String mapfish_data_creator();

    String mapfish_data_dateplace();

    String mapfish_data_river();

    String mapTitle();

    String mapSubtitle();

    String mapRange();

    String mapStretch();

    String mapCreator();

    String mapInstitution();

    String mapSource();

    String mapDate();

    String mapLogo();

    String wmsURLMenuItem();

    String wmsURLBoxTitle();

    String requireTheme();

    String PATH();

    String DESCRIPTION();

    String KM();

    String Z();

    String LOCATION();

    String HPGP();

    String DIFF();

    String SOURCE();

    String WATERBODY();

    String FEDSTATE_KM();

    String official_regulation();

    String historical_discharge_curves();

    String current_gauge();

    String suggested_label();

    String negative_values_not_allowed_for_to();

    String analyzed_range();

    String minfo_type();

    String river();

    String ld_locations();

    String gauge_name();

    String reference_gauge();

    String station();

    String station_name();

    String lower_time();

    String upper_time();

    String no_data_for_year();

    String error_no_sedimentloadinfo_found();

    String error_no_sedimentloadinfo_data();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
