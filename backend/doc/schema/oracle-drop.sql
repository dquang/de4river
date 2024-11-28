ALTER TABLE annotations DROP CONSTRAINT cAnnotationsRanges;
ALTER TABLE annotations DROP CONSTRAINT cAnnotationsEdges;
ALTER TABLE annotations DROP CONSTRAINT cAnnotationsPositions;
ALTER TABLE annotations DROP CONSTRAINT cAnnotationsAttributes;
ALTER TABLE annotations DROP CONSTRAINT cAnnotationsTypes;
ALTER TABLE cross_section_lines DROP CONSTRAINT cQPSLinesCrossSections;
ALTER TABLE cross_section_points DROP CONSTRAINT cQPSPointsCrossSectionLines;
ALTER TABLE cross_sections DROP CONSTRAINT cCrossSectionsRivers;
ALTER TABLE cross_sections DROP CONSTRAINT cCrossSectionsTimeIntervals;
ALTER TABLE discharge_tables DROP CONSTRAINT cDischargeTablesTime_intervals;
ALTER TABLE discharge_tables DROP CONSTRAINT cDischargeTablesGauges;
ALTER TABLE gauges DROP CONSTRAINT cGaugesRivers;
ALTER TABLE gauges DROP CONSTRAINT cGaugesRanges;
ALTER TABLE hyk_entries DROP CONSTRAINT cHykEntriesHyks;
ALTER TABLE hyk_flow_zones DROP CONSTRAINT cHykFlowZonesHykFormations;
ALTER TABLE hyk_flow_zones DROP CONSTRAINT cHykFlowZonesHykFlowZoneTypes;
ALTER TABLE hyks DROP CONSTRAINT cHyksRivers;
ALTER TABLE hyk_formations DROP CONSTRAINT cHykFormationsHykEntries;
ALTER TABLE main_values DROP CONSTRAINT cMainValuesTimeIntervals;
ALTER TABLE main_values DROP CONSTRAINT cMainValuesGauges;
ALTER TABLE main_values DROP CONSTRAINT cMainValuesNamedMainValues;
ALTER TABLE named_main_values DROP CONSTRAINT cNamedMainValuesMainValueTypes;
ALTER TABLE official_lines DROP CONSTRAINT cOffLinesNamedMainValues;
ALTER TABLE official_lines DROP CONSTRAINT cOffLinesWstColumns;
ALTER TABLE ranges DROP CONSTRAINT cRangesRivers;
ALTER TABLE rivers DROP CONSTRAINT cRiversUnits;
ALTER TABLE rivers DROP CONSTRAINT cRiversSeddbNames;
ALTER TABLE wst_column_q_ranges DROP CONSTRAINT cWstColumnQRangesWstColums;
ALTER TABLE wst_column_q_ranges DROP CONSTRAINT cWstColumnQRangesWstQRanges;
ALTER TABLE wst_column_values DROP CONSTRAINT cWstColumnValuesWstColumns;
ALTER TABLE wst_columns DROP CONSTRAINT cWstColumnsTime_intervals;
ALTER TABLE wst_columns DROP CONSTRAINT cWstColumnsWsts;
ALTER TABLE wst_q_ranges DROP CONSTRAINT cWstQRangesRanges;
ALTER TABLE wsts DROP CONSTRAINT cWstsRivers;
ALTER TABLE wsts DROP CONSTRAINT cWstsWstKinds;
DROP TABLE annotation_types PURGE;
DROP TABLE annotations PURGE;
DROP TABLE attributes PURGE;
DROP TABLE cross_section_lines PURGE;
DROP TABLE cross_section_points PURGE;
DROP TABLE cross_sections PURGE;
DROP TABLE discharge_table_values PURGE;
DROP TABLE discharge_tables PURGE;
DROP TABLE edges PURGE;
DROP TABLE gauges PURGE;
DROP TABLE hyk_entries PURGE;
DROP TABLE hyk_flow_zone_types PURGE;
DROP TABLE hyk_flow_zones PURGE;
DROP TABLE hyk_formations PURGE;
DROP TABLE hyks PURGE;
DROP TABLE main_value_types PURGE;
DROP TABLE main_values PURGE;
DROP TABLE named_main_values PURGE;
DROP TABLE positions PURGE;
DROP TABLE ranges PURGE;
DROP TABLE rivers PURGE;
DROP TABLE time_intervals PURGE;
DROP TABLE units PURGE;
DROP TABLE wst_column_q_ranges PURGE;
DROP TABLE wst_column_values PURGE;
DROP TABLE wst_columns PURGE;
DROP TABLE wst_q_ranges PURGE;
DROP TABLE official_lines PURGE;
DROP TABLE wsts PURGE;
DROP TABLE wst_kinds PURGE;
DROP TABLE seddb_names PURGE;
DROP SEQUENCE ANNOTATION_TYPES_ID_SEQ;
DROP SEQUENCE ANNOTATIONS_ID_SEQ;
DROP SEQUENCE ATTRIBUTES_ID_SEQ;
DROP SEQUENCE CROSS_SECTION_LINES_ID_SEQ;
DROP SEQUENCE CROSS_SECTION_POINTS_ID_SEQ;
DROP SEQUENCE CROSS_SECTIONS_ID_SEQ;
DROP SEQUENCE DISCHARGE_TABLE_VALUES_ID_SEQ;
DROP SEQUENCE DISCHARGE_TABLES_ID_SEQ;
DROP SEQUENCE EDGES_ID_SEQ;
DROP SEQUENCE GAUGES_ID_SEQ;
DROP SEQUENCE HYK_ENTRIES_ID_SEQ;
DROP SEQUENCE HYK_FLOW_ZONE_TYPES_ID_SEQ;
DROP SEQUENCE HYK_FLOW_ZONES_ID_SEQ;
DROP SEQUENCE HYK_FORMATIONS_ID_SEQ;
DROP SEQUENCE HYKS_ID_SEQ;
DROP SEQUENCE MAIN_VALUE_TYPES_ID_SEQ;
DROP SEQUENCE MAIN_VALUES_ID_SEQ;
DROP SEQUENCE NAMED_MAIN_VALUES_ID_SEQ;
DROP SEQUENCE POSITIONS_ID_SEQ;
DROP SEQUENCE RANGES_ID_SEQ;
DROP SEQUENCE RIVERS_ID_SEQ;
DROP SEQUENCE TIME_INTERVALS_ID_SEQ;
DROP SEQUENCE UNITS_ID_SEQ;
DROP SEQUENCE WST_COLUMN_Q_RANGES_ID_SEQ;
DROP SEQUENCE WST_COLUMN_VALUES_ID_SEQ;
DROP SEQUENCE WST_COLUMNS_ID_SEQ;
DROP SEQUENCE WST_Q_RANGES_ID_SEQ;
DROP SEQUENCE OFFICIAL_LINES_ID_SEQ;
DROP SEQUENCE WSTS_ID_SEQ;
DROP VIEW wst_value_table;
DROP VIEW wst_w_values ;
DROP VIEW wst_q_values;
DROP VIEW wst_ranges;
