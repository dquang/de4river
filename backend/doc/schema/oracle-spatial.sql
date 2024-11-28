--WHENEVER SQLERROR EXIT;

CREATE TABLE axis_kinds(
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO axis_kinds(id, name) VALUES (0, 'Unbekannt');
INSERT INTO axis_kinds(id, name) VALUES (1, 'aktuelle Achse');
INSERT INTO axis_kinds(id, name) VALUES (2, 'Sonstige');

-- Geodaesie/Flussachse+km/achse
CREATE SEQUENCE RIVER_AXES_ID_SEQ;
CREATE TABLE river_axes(
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  NUMBER(38) DEFAULT 0 NOT NULL REFERENCES axis_kinds(id),
    name     VARCHAR(64),
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('river_axes', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER river_axes_trigger BEFORE INSERT ON river_axes FOR each ROW
    BEGIN
        SELECT RIVER_AXES_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX river_axes_spatial_idx ON river_axes(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');


-- Geodaesie/Flussachse+km/km.shp
CREATE SEQUENCE RIVER_AXES_KM_ID_SEQ;
CREATE TABLE river_axes_km(
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    km NUMBER(7,3) NOT NULL,
    fedstate_km NUMBER(7,3),
    name     VARCHAR(64),
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('river_axes_km', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001)), 31467);
CREATE OR REPLACE TRIGGER river_axes_km_trigger BEFORE INSERT ON river_axes_km FOR each ROW
    BEGIN
        SELECT river_axes_km_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX river_axes_km_spatial_idx ON river_axes_km(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=point');


--Geodaesie/Querprofile/QP-Spuren/qps.shp
CREATE TABLE cross_section_track_kinds(
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO cross_section_track_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO cross_section_track_kinds(id, name) VALUES (1, 'aktuelle Querprofilspuren');

CREATE SEQUENCE CROSS_SECTION_TRACKS_ID_SEQ;
CREATE TABLE cross_section_tracks (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  NUMBER(38) DEFAULT 0 NOT NULL REFERENCES cross_section_track_kinds(id),
    km       NUMBER(38,12) NOT NULL,
    z        NUMBER(38,12) DEFAULT 0 NOT NULL,
    name     VARCHAR(64),
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('cross_section_tracks', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER cross_section_tracks_trigger BEFORE INSERT ON cross_section_tracks FOR each ROW
    BEGIN
        SELECT CROSS_SECTION_TRACKS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX CrossSectionTracks_spatial_idx ON cross_section_tracks(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');


CREATE TABLE building_kinds(
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO building_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO building_kinds(id, name) VALUES (1, 'Brücken');
INSERT INTO building_kinds(id, name) VALUES (2, 'Wehre');
INSERT INTO building_kinds(id, name) VALUES (3, 'Pegel');

-- Geodaesie/Bauwerke
CREATE SEQUENCE BUILDINGS_ID_SEQ;
CREATE TABLE buildings(
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id NUMBER(38) DEFAULT 0 NOT NULL REFERENCES building_kinds(id),
    km NUMBER(38,11),
    name VARCHAR2(255), -- The layername
    description VARCHAR(256), -- Name taken from attributes
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('buildings', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER buildings_trigger BEFORE INSERT ON buildings FOR each ROW
    BEGIN
        SELECT BUILDINGS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX buildings_spatial_idx ON buildings(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');


-- Geodaesie/Festpunkte/Festpunkte.shp
CREATE SEQUENCE FIXPOINTS_ID_SEQ;
CREATE TABLE fixpoints (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    x NUMBER(38,11),
    y NUMBER(38,11),
    km NUMBER(38,11) NOT NULL,
    HPGP VARCHAR(64),
    name VARCHAR(64),
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('fixpoints', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001)), 31467);
CREATE OR REPLACE TRIGGER fixpoints_trigger BEFORE INSERT ON fixpoints FOR each ROW
    BEGIN
        SELECT FIXPOINTS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX fixpoints_spatial_idx ON fixpoints(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POINT');


-- Hydrologie/Hydr. Grenzen/talaue.shp
CREATE TABLE floodplain_kinds(
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO floodplain_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO floodplain_kinds(id, name) VALUES (1, 'aktuelle Talaue');

CREATE SEQUENCE FLOODPLAIN_ID_SEQ;
CREATE TABLE floodplain(
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  NUMBER(38) DEFAULT 0 NOT NULL REFERENCES floodplain_kinds(id),
    name     VARCHAR(64),
    path     VARCHAR(256),
    ID NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('floodplain', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER floodplain_trigger BEFORE INSERT ON floodplain FOR each ROW
    BEGIN
        SELECT FLOODPLAIN_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
--CREATE INDEX floodplain_spatial_idx ON floodplain(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POLYGON');


-- Geodaesie/Hoehenmodelle/*
CREATE SEQUENCE DEM_ID_SEQ;
CREATE TABLE dem (
    ID               NUMBER PRIMARY KEY NOT NULL,
    river_id         NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name             VARCHAR(64),
    range_id         NUMBER(38) REFERENCES ranges(id) ON DELETE CASCADE,
    time_interval_id NUMBER(38) REFERENCES time_intervals(id),
    projection       VARCHAR(32),
    elevation_state  VARCHAR(32),
    srid             NUMBER NOT NULL,
    format           VARCHAR(32),
    border_break     NUMBER(1) DEFAULT 0 NOT NULL,
    resolution       VARCHAR(16),
    description      VARCHAR(256),
    path             VARCHAR(256) NOT NULL
);
CREATE OR REPLACE TRIGGER dem_trigger BEFORE INSERT ON dem FOR each ROW
    BEGIN
        SELECT DEM_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

--Static lookup tables for Hochwasserschutzanlagen
CREATE TABLE hws_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    kind VARCHAR(64) NOT NULL
);
INSERT INTO hws_kinds (id, kind) VALUES (1, 'Durchlass');
INSERT INTO hws_kinds (id, kind) VALUES (2, 'Damm');
INSERT INTO hws_kinds (id, kind) VALUES (3, 'Graben');

CREATE TABLE fed_states (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(23) NOT NULL
);
INSERT INTO fed_states (id, name) VALUES (1, 'Bayern');
INSERT INTO fed_states (id, name) VALUES (2, 'Hessen');
INSERT INTO fed_states (id, name) VALUES (3, 'Niedersachsen');
INSERT INTO fed_states (id, name) VALUES (4, 'Nordrhein-Westfalen');
INSERT INTO fed_states (id, name) VALUES (5, 'Rheinland-Pfalz');
INSERT INTO fed_states (id, name) VALUES (6, 'Saarland');
INSERT INTO fed_states (id, name) VALUES (7, 'Schleswig-Holstein');
INSERT INTO fed_states (id, name) VALUES (8, 'Brandenburg');
INSERT INTO fed_states (id, name) VALUES (9, 'Mecklenburg-Vorpommern');
INSERT INTO fed_states (id, name) VALUES (10, 'Thüringen');
INSERT INTO fed_states (id, name) VALUES (11, 'Baden-Württemberg');
INSERT INTO fed_states (id, name) VALUES (12, 'Sachsen-Anhalt');
INSERT INTO fed_states (id, name) VALUES (13, 'Sachsen');
INSERT INTO fed_states (id, name) VALUES (14, 'Berlin');
INSERT INTO fed_states (id, name) VALUES (15, 'Bremen');
INSERT INTO fed_states (id, name) VALUES (16, 'Hamburg');

--Hydrologie/HW-Schutzanlagen/hws.shp
-- HWS-Lines
CREATE SEQUENCE HWS_LINES_ID_SEQ;
CREATE TABLE hws_lines (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    kind_id NUMBER(2) DEFAULT 2 REFERENCES hws_kinds(id),
    fed_state_id NUMBER(2) REFERENCES fed_states(id),
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(256),
    path VARCHAR(256),
    official NUMBER(1) DEFAULT 0 NOT NULL CHECK(official IN(0,1)),
    agency VARCHAR(256),
    range VARCHAR(256),
    shore_side NUMBER DEFAULT 0,
    source VARCHAR(256),
    status_date TIMESTAMP,
    description VARCHAR(256),
    id NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('hws_lines', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER hws_lines_trigger BEFORE INSERT ON hws_lines FOR each ROW
    BEGIN
        SELECT HWS_LINES_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

CREATE TABLE sectie_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sectie_kinds (id, name) VALUES (0, 'nicht berücksichtigt');
INSERT INTO sectie_kinds (id, name) VALUES (1, 'Hauptgerinne');
INSERT INTO sectie_kinds (id, name) VALUES (2, 'Uferbereich');
INSERT INTO sectie_kinds (id, name) VALUES (3, 'Vorland');

CREATE TABLE sobek_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sobek_kinds (id, name) VALUES (0, 'nicht berücksichtigt');
INSERT INTO sobek_kinds (id, name) VALUES (1, 'durchströmt');
INSERT INTO sobek_kinds (id, name) VALUES (2, 'nicht durchströmt');

CREATE TABLE boundary_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO boundary_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO boundary_kinds (id, name) VALUES (1, 'BfG');
INSERT INTO boundary_kinds (id, name) VALUES (2, 'Land');
INSERT INTO boundary_kinds (id, name) VALUES (3, 'Sonstige');

-- HWS Points
CREATE SEQUENCE HWS_POINTS_ID_SEQ;
CREATE TABLE hws_points (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    kind_id NUMBER DEFAULT 2 REFERENCES hws_kinds(id),
    fed_state_id NUMBER REFERENCES fed_states(id),
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(256),
    path VARCHAR(256),
    official NUMBER DEFAULT 0,
    agency VARCHAR(256),
    range VARCHAR(256),
    shore_side NUMBER DEFAULT 0,
    source VARCHAR(256),
    status_date VARCHAR(256),
    description VARCHAR(256),
    freeboard NUMBER(19,5),
    dike_km NUMBER(19,5),
    z NUMBER(19,5),
    z_target NUMBER(19,5),
    rated_level NUMBER(19,5),
    id NUMBER PRIMARY KEY NOT NULL
);

INSERT INTO USER_SDO_GEOM_METADATA VALUES ('hws_points', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);

CREATE OR REPLACE TRIGGER hws_points_trigger BEFORE INSERT ON hws_points FOR each ROW
    BEGIN
        SELECT HWS_POINTS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

--Hydrologie/UeSG
CREATE TABLE floodmap_kinds (
    id 	     NUMBER PRIMARY KEY NOT NULL,
    name     varchar(64) NOT NULL
);
INSERT INTO floodmap_kinds VALUES (200, 'Messung');
INSERT INTO floodmap_kinds VALUES (111, 'Berechnung-Aktuell-BfG');
INSERT INTO floodmap_kinds VALUES (112, 'Berechnung-Aktuell-Bundesländer');
INSERT INTO floodmap_kinds VALUES (121, 'Berechnung-Potenziell-BfG');
INSERT INTO floodmap_kinds VALUES (122, 'Berechnung-Potenziell-Bundesländer');

CREATE SEQUENCE FLOODMAPS_ID_SEQ;
CREATE TABLE floodmaps (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    kind NUMBER NOT NULL REFERENCES floodmap_kinds(id),
    diff NUMBER(19,5),
    count NUMBER(38),
    area NUMBER(19,5),
    perimeter NUMBER(19,5),
    waterbody  VARCHAR(64),
    path     VARCHAR(256),
    source   varchar(64),
    id NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('floodmaps', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER floodmaps_trigger BEFORE INSERT ON floodmaps FOR each ROW
    BEGIN
        SELECT FLOODMAPS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

--Hydrologie/Hydr.Grenzen/Linien
CREATE SEQUENCE HYDR_BOUNDARIES_ID_SEQ;
CREATE TABLE hydr_boundaries (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(255),
    kind   NUMBER(38) DEFAULT 0 NOT NULL REFERENCES boundary_kinds(id),
    sectie NUMBER(38) REFERENCES sectie_kinds(id),
    sobek  NUMBER(38) REFERENCES sobek_kinds(id),
    path     VARCHAR(256),
    id NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('hydr_boundaries', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER hydr_boundaries_trigger BEFORE INSERT ON hydr_boundaries FOR each ROW
    BEGIN
        SELECT HYDR_BOUNDARIES_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

CREATE SEQUENCE HYDR_BOUNDARIES_POLY_ID_SEQ;
CREATE TABLE hydr_boundaries_poly (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    river_id NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(255),
    kind   NUMBER(38) DEFAULT 0 NOT NULL REFERENCES boundary_kinds(id),
    sectie NUMBER(38) REFERENCES sectie_kinds(id),
    sobek  NUMBER(38) REFERENCES sobek_kinds(id),
    path     VARCHAR(256),
    id NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('hydr_boundaries_poly', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE OR REPLACE TRIGGER hydr_boundaries_poly_trigger BEFORE INSERT ON hydr_boundaries_poly FOR each ROW
    BEGIN
        SELECT HYDR_BOUNDARIES_POLY_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

-- Hydrologie/Streckendaten/
CREATE TABLE jetty_kinds(
    id 	     NUMBER PRIMARY KEY NOT NULL,
    name     VARCHAR(64)
);
INSERT INTO jetty_kinds VALUES (0, 'Buhnenkopf');
INSERT INTO jetty_kinds VALUES (1, 'Buhnenfuß');
INSERT INTO jetty_kinds VALUES (2, 'Buhnenwurzel');

CREATE SEQUENCE JETTIES_ID_SEQ;
CREATE TABLE jetties (
    OGR_FID     NUMBER(38),
    GEOM        MDSYS.SDO_GEOMETRY,
    id          NUMBER PRIMARY KEY NOT NULL,
    river_id 	NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    path       	VARCHAR(256),
    kind_id    	NUMBER(38) REFERENCES jetty_kinds(id),
    km 		NUMBER(7,3),
    z        NUMBER(38,12)
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('jetties', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001)), 31467);
CREATE OR REPLACE TRIGGER jetties_trigger BEFORE INSERT ON jetties FOR EACH ROW
    BEGIN
        SELECT JETTIES_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/

CREATE SEQUENCE FLOOD_MARKS_ID_SEQ;
CREATE TABLE flood_marks (
    OGR_FID     NUMBER(38),
    GEOM        MDSYS.SDO_GEOMETRY,
    id          NUMBER PRIMARY KEY NOT NULL,
    river_id 	NUMBER(38) REFERENCES rivers(id) ON DELETE CASCADE,
    path       	VARCHAR(256),
    km 		NUMBER(7,3),
    z           NUMBER(38,12),
    location    VARCHAR(64),
    year        NUMBER(38,0)
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('flood_marks', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001)), 31467);
CREATE OR REPLACE TRIGGER flood_marks_trigger BEFORE INSERT ON flood_marks FOR EACH ROW
    BEGIN
        SELECT FLOOD_MARKS_ID_SEQ.nextval INTO :new.id FROM dual;
    END;
/
