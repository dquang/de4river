DROP TRIGGER river_axes_trigger;
DROP TABLE river_axes PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'RIVER_AXES';
DROP SEQUENCE RIVER_AXES_ID_SEQ;

DROP TRIGGER river_axes_km_trigger;
DROP TABLE river_axes_km PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'RIVER_AXES_KM';
DROP SEQUENCE RIVER_AXES_KM_ID_SEQ;

DROP TRIGGER cross_section_tracks_trigger;
DROP TABLE cross_section_tracks PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'CROSS_SECTION_TRACKS';
DROP SEQUENCE CROSS_SECTION_TRACKS_ID_SEQ;

DROP TRIGGER buildings_trigger;
DROP TABLE buildings PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BUILDINGS';
DROP SEQUENCE BUILDINGS_ID_SEQ;

DROP TRIGGER fixpoints_trigger;
DROP TABLE fixpoints PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'FIXPOINTS';
DROP SEQUENCE FIXPOINTS_ID_SEQ;

DROP TRIGGER floodplain_trigger;
DROP TABLE floodplain PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'FLOODPLAIN';
DROP SEQUENCE FLOODPLAIN_ID_SEQ;

DROP TRIGGER dem_trigger;
DROP TABLE dem PURGE;
DROP SEQUENCE DEM_ID_SEQ;

DROP TRIGGER hws_lines_trigger;
DROP TABLE hws_lines PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'HWS_LINES';
DROP SEQUENCE HWS_LINES_ID_SEQ;

DROP TRIGGER hws_points_trigger;
DROP TABLE hws_points PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'HWS_POINTS';
DROP SEQUENCE HWS_POINTS_ID_SEQ;

DROP TRIGGER floodmaps_trigger;
DROP TABLE floodmaps PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'FLOODMAPS';
DROP SEQUENCE FLOODMAPS_ID_SEQ;

DROP TRIGGER hydr_boundaries_trigger;
DROP TABLE hydr_boundaries PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'HYDR_BOUNDARIES';
DROP SEQUENCE HYDR_BOUNDARIES_ID_SEQ;

DROP TRIGGER hydr_boundaries_poly_trigger;
DROP TABLE hydr_boundaries_poly PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'HYDR_BOUNDARIES_POLY';
DROP SEQUENCE HYDR_BOUNDARIES_POLY_ID_SEQ;

DROP TRIGGER jetties_trigger;
DROP TABLE jetties PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'JETTIES';
DROP SEQUENCE JETTIES_ID_SEQ;

DROP TRIGGER flood_marks_trigger;
DROP TABLE flood_marks PURGE;
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'FLOOD_MARKS';
DROP SEQUENCE FLOOD_MARKS_ID_SEQ;

DROP TABLE hws_kinds PURGE;
DROP TABLE sectie_kinds PURGE;
DROP TABLE sobek_kinds PURGE;
DROP TABLE fed_states PURGE;
DROP TABLE axis_kinds PURGE;
DROP TABLE boundary_kinds PURGE;
DROP TABLE cross_section_track_kinds PURGE;
DROP TABLE floodplain_kinds PURGE;
DROP TABLE floodmap_kinds PURGE;
DROP TABLE building_kinds PURGE;
DROP TABLE jetty_kinds PURGE;
