BEGIN;

CREATE TABLE axis_kinds(
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO axis_kinds(id, name) VALUES (0, 'Unbekannt');
INSERT INTO axis_kinds(id, name) VALUES (1, 'aktuelle Achse');
INSERT INTO axis_kinds(id, name) VALUES (2, 'Sonstige');

-- Geodaesie/Flussachse+km/achse
CREATE SEQUENCE RIVER_AXES_ID_SEQ;
CREATE TABLE river_axes (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  int REFERENCES axis_kinds(id) NOT NULL DEFAULT 0,
    name     VARCHAR(64),
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('river_axes', 'geom', 31467, 'MULTILINESTRING', 2);
ALTER TABLE river_axes ALTER COLUMN id SET DEFAULT NEXTVAL('RIVER_AXES_ID_SEQ');


-- TODO: TestMe.
-- Geodaesie/Flussachse+km/km.shp
CREATE SEQUENCE RIVER_AXES_KM_ID_SEQ;
CREATE TABLE river_axes_km (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    km       FLOAT8 NOT NULL,
    fedstate_km FLOAT8,
    name     VARCHAR(64),
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('river_axes_km', 'geom', 31467, 'POINT', 2);
ALTER TABLE river_axes_km ALTER COLUMN id SET DEFAULT NEXTVAL('RIVER_AXES_KM_ID_SEQ');


--Geodaesie/Querprofile/QP-Spuren/qps.shp
CREATE TABLE cross_section_track_kinds(
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO cross_section_track_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO cross_section_track_kinds(id, name) VALUES (1, 'aktuelle Querprofilspuren');

CREATE SEQUENCE CROSS_SECTION_TRACKS_ID_SEQ;
CREATE TABLE cross_section_tracks (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  int REFERENCES cross_section_track_kinds(id) NOT NULL DEFAULT 0,
    km       FLOAT8 NOT NULL,
    z        FLOAT8 NOT NULL DEFAULT 0,
    name     VARCHAR(64),
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('cross_section_tracks', 'geom', 31467, 'LINESTRING', 2);
ALTER TABLE cross_section_tracks ALTER COLUMN id SET DEFAULT NEXTVAL('CROSS_SECTION_TRACKS_ID_SEQ');

CREATE TABLE building_kinds(
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO building_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO building_kinds(id, name) VALUES (1, 'Brücken');
INSERT INTO building_kinds(id, name) VALUES (2, 'Wehre');
INSERT INTO building_kinds(id, name) VALUES (3, 'Pegel');

-- Geodaesie/Bauwerke
CREATE SEQUENCE BUILDINGS_ID_SEQ;
CREATE TABLE buildings (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    description VARCHAR(256), -- Name taken from attributes,
    name     VARCHAR(256), -- The layername
    km       FLOAT8,
    kind_id  int REFERENCES building_kinds(id) NOT NULL DEFAULT 0,
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('buildings', 'geom', 31467, 'LINESTRING', 2);
ALTER TABLE buildings ALTER COLUMN id SET DEFAULT NEXTVAL('BUILDINGS_ID_SEQ');


-- Geodaesie/Festpunkte/Festpunkte.shp
CREATE SEQUENCE FIXPOINTS_ID_SEQ;
CREATE TABLE fixpoints (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    x        FLOAT8,
    y        FLOAT8,
    km       FLOAT8 NOT NULL,
    HPGP     VARCHAR(64),
    name     VARCHAR(64),
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('fixpoints', 'geom', 31467, 'POINT', 2);
ALTER TABLE fixpoints ALTER COLUMN id SET DEFAULT NEXTVAL('FIXPOINTS_ID_SEQ');


-- Hydrologie/Hydr. Grenzen/talaue.shp
CREATE TABLE floodplain_kinds(
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO floodplain_kinds(id, name) VALUES (0, 'Sonstige');
INSERT INTO floodplain_kinds(id, name) VALUES (1, 'aktuelle Talaue');

CREATE SEQUENCE FLOODPLAIN_ID_SEQ;
CREATE TABLE floodplain (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    kind_id  int REFERENCES floodplain_kinds(id) NOT NULL DEFAULT 0,
    name     VARCHAR(64),
    path     VARCHAR(256)
);
SELECT AddGeometryColumn('floodplain', 'geom', 31467, 'POLYGON', 2);
ALTER TABLE floodplain ALTER COLUMN id SET DEFAULT NEXTVAL('FLOODPLAIN_ID_SEQ');


-- Geodaesie/Hoehenmodelle/*
CREATE SEQUENCE DEM_ID_SEQ;
CREATE TABLE dem (
    id       int PRIMARY KEY NOT NULL,
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    name             VARCHAR(64),
    range_id         INT REFERENCES ranges(id) ON DELETE CASCADE,
    time_interval_id INT REFERENCES time_intervals(id),
    projection       VARCHAR(32),
    srid	     int NOT NULL,
    elevation_state  VARCHAR(32),
    format           VARCHAR(32),
    border_break     BOOLEAN NOT NULL DEFAULT FALSE,
    resolution       VARCHAR(16),
    description      VARCHAR(256),
    path             VARCHAR(256) NOT NULL
);
ALTER TABLE dem ALTER COLUMN id SET DEFAULT NEXTVAL('DEM_ID_SEQ');


-- Static lookup tables for Hochwasserschutzanlagen
CREATE TABLE hws_kinds (
    id int PRIMARY KEY NOT NULL,
    kind VARCHAR(64) NOT NULL
);
INSERT INTO hws_kinds (id, kind) VALUES (1, 'Durchlass');
INSERT INTO hws_kinds (id, kind) VALUES (2, 'Damm');
INSERT INTO hws_kinds (id, kind) VALUES (3, 'Graben');

CREATE TABLE fed_states (
    id int PRIMARY KEY NOT NULL,
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

--Hydrologie/HW-Schutzanlagen/*Linien.shp
CREATE SEQUENCE HWS_LINES_ID_SEQ;
CREATE TABLE hws_lines (
    id int PRIMARY KEY NOT NULL,
    ogr_fid int,
    kind_id int REFERENCES hws_kinds(id) DEFAULT 2,
    fed_state_id int REFERENCES fed_states(id),
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR(256),
    path VARCHAR(256),
    official INT DEFAULT 0 NOT NULL CHECK(official IN(0,1)),
    agency VARCHAR(256),
    range VARCHAR(256),
    shore_side INT DEFAULT 0,
    source VARCHAR(256),
    status_date TIMESTAMP,
    description VARCHAR(256)
);
SELECT AddGeometryColumn('hws_lines', 'geom', 31467, 'MULTILINESTRING', 3);
-- TODO: dike_km_from dike_km_to, are they geometries?

ALTER TABLE hws_lines ALTER COLUMN id SET DEFAULT NEXTVAL('HWS_LINES_ID_SEQ');

--Hydrologie/HW-Schutzanlagen/*Punkte.shp
CREATE SEQUENCE HWS_POINTS_ID_SEQ;
CREATE TABLE hws_points (
    id int PRIMARY KEY NOT NULL,
    ogr_fid int,
    kind_id int REFERENCES hws_kinds(id) DEFAULT 2,
    fed_state_id int REFERENCES fed_states(id),
    river_id int REFERENCES rivers(id) ON DELETE CASCADE,
    name VARCHAR,
    path VARCHAR,
    official INT DEFAULT 0,
    agency VARCHAR,
    range VARCHAR,
    shore_side INT DEFAULT 0,
    source VARCHAR,
    status_date VARCHAR,
    description VARCHAR,
    freeboard FLOAT8,
    dike_km FLOAT8,
    z FLOAT8,
    z_target FLOAT8,
    rated_level FLOAT8
);
SELECT AddGeometryColumn('hws_points', 'geom', 31467, 'POINT', 2);

ALTER TABLE hws_points ALTER COLUMN id SET DEFAULT NEXTVAL('HWS_POINTS_ID_SEQ');

--
--Hydrologie/UeSG
CREATE TABLE floodmap_kinds (
    id 	     int PRIMARY KEY NOT NULL,
    name     varchar(64) NOT NULL
);
INSERT INTO floodmap_kinds VALUES (200, 'Messung');
INSERT INTO floodmap_kinds VALUES (111, 'Berechnung-Aktuell-BfG');
INSERT INTO floodmap_kinds VALUES (112, 'Berechnung-Aktuell-Bundesländer');
INSERT INTO floodmap_kinds VALUES (121, 'Berechnung-Potenziell-BfG');
INSERT INTO floodmap_kinds VALUES (122, 'Berechnung-Potenziell-Bundesländer');

CREATE SEQUENCE FLOODMAPS_ID_SEQ;
CREATE TABLE floodmaps (
    id         int PRIMARY KEY NOT NULL,
    river_id   int REFERENCES rivers(id) ON DELETE CASCADE,
    name       varchar(64) NOT NULL,
    kind       int NOT NULL REFERENCES floodmap_kinds(id),
    diff       FLOAT8,
    count      int,
    area       FLOAT8,
    perimeter  FLOAT8,
    waterbody  varchar(64),
    path     VARCHAR(256),
    source   varchar(64)
);
SELECT AddGeometryColumn('floodmaps', 'geom', 31467, 'MULTIPOLYGON', 2);
ALTER TABLE floodmaps ALTER COLUMN id SET DEFAULT NEXTVAL('FLOODMAPS_ID_SEQ');

CREATE TABLE sectie_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sectie_kinds (id, name) VALUES (0, 'nicht berücksichtigt');
INSERT INTO sectie_kinds (id, name) VALUES (1, 'Hauptgerinne');
INSERT INTO sectie_kinds (id, name) VALUES (2, 'Uferbereich');
INSERT INTO sectie_kinds (id, name) VALUES (3, 'Vorland');

CREATE TABLE sobek_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sobek_kinds (id, name) VALUES (0, 'nicht berücksichtigt');
INSERT INTO sobek_kinds (id, name) VALUES (1, 'durchströmt');
INSERT INTO sobek_kinds (id, name) VALUES (2, 'nicht durchströmt');

CREATE TABLE boundary_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO boundary_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO boundary_kinds (id, name) VALUES (1, 'BfG');
INSERT INTO boundary_kinds (id, name) VALUES (2, 'Land');
INSERT INTO boundary_kinds (id, name) VALUES (3, 'Sonstige');

CREATE SEQUENCE HYDR_BOUNDARIES_ID_SEQ;
CREATE TABLE hydr_boundaries (
    id         int PRIMARY KEY NOT NULL,
    river_id   int REFERENCES rivers(id) ON DELETE CASCADE,
    name       VARCHAR(255),
    kind       int REFERENCES boundary_kinds(id) NOT NULL DEFAULT 0,
    sectie     int REFERENCES sectie_kinds(id),
    sobek      int REFERENCES sobek_kinds(id),
    path       VARCHAR(256)
);
SELECT AddGeometryColumn('hydr_boundaries','geom',31467,'MULTILINESTRING',3);
ALTER TABLE hydr_boundaries ALTER COLUMN id SET DEFAULT NEXTVAL('HYDR_BOUNDARIES_ID_SEQ');


CREATE SEQUENCE HYDR_BOUNDARIES_POLY_ID_SEQ;
CREATE TABLE hydr_boundaries_poly (
    id         int PRIMARY KEY NOT NULL,
    river_id   int REFERENCES rivers(id) ON DELETE CASCADE,
    name       VARCHAR(255),
    kind       int REFERENCES boundary_kinds(id) NOT NULL DEFAULT 0,
    sectie     int REFERENCES sectie_kinds(id),
    sobek      int REFERENCES sobek_kinds(id),
    path       VARCHAR(256)
);
SELECT AddGeometryColumn('hydr_boundaries_poly','geom',31467,'MULTIPOLYGON',3);
ALTER TABLE hydr_boundaries_poly ALTER COLUMN id SET DEFAULT NEXTVAL('HYDR_BOUNDARIES_POLY_ID_SEQ');


CREATE TABLE jetty_kinds(
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);
INSERT INTO jetty_kinds VALUES (0, 'Buhnenkopf');
INSERT INTO jetty_kinds VALUES (1, 'Buhnenfuß');
INSERT INTO jetty_kinds VALUES (2, 'Buhnenwurzel');

CREATE SEQUENCE JETTIES_ID_SEQ;
CREATE TABLE jetties (
    id         int PRIMARY KEY NOT NULL,
    river_id   int REFERENCES rivers(id) ON DELETE CASCADE,
    path       VARCHAR(256),
    kind_id    int REFERENCES jetty_kinds(id),
    km         FLOAT8,
    z	       FLOAT8
);
SELECT AddGeometryColumn('jetties','geom',31467,'POINT',2);
ALTER TABLE jetties ALTER COLUMN id SET DEFAULT NEXTVAL('JETTIES_ID_SEQ');

CREATE SEQUENCE FLOOD_MARKS_ID_SEQ;
CREATE TABLE flood_marks (
    id         int PRIMARY KEY NOT NULL,
    river_id   int REFERENCES rivers(id) ON DELETE CASCADE,
    path       VARCHAR(256),
    km         FLOAT8,
    z	       FLOAT8,
    location   VARCHAR(64),
    year       int
);
SELECT AddGeometryColumn('flood_marks','geom',31467,'POINT',2);
ALTER TABLE flood_marks ALTER COLUMN id SET DEFAULT NEXTVAL('FLOOD_MARKS_ID_SEQ');

COMMIT;
