DROP table hws;
DROP sequence HWS_ID_SEQ;
DROP table lines;
DROP sequence LINES_ID_SEQ;
DROP table catchment;
DROP sequence CATCHMENT_ID_SEQ;

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

CREATE TABLE sectie_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sectie_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO sectie_kinds (id, name) VALUES (1, 'Flussschlauch');
INSERT INTO sectie_kinds (id, name) VALUES (2, 'Uferbank');
INSERT INTO sectie_kinds (id, name) VALUES (3, 'Überflutungsbereich');

CREATE TABLE sobek_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sobek_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO sobek_kinds (id, name) VALUES (1, 'Stromführend');
INSERT INTO sobek_kinds (id, name) VALUES (2, 'Stromspeichernd');

CREATE TABLE boundary_kinds (
    id int PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO boundary_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO boundary_kinds (id, name) VALUES (1, 'BfG');
INSERT INTO boundary_kinds (id, name) VALUES (2, 'Land');
INSERT INTO boundary_kinds (id, name) VALUES (3, 'Sonstige');

--Hydrologie/HW-Schutzanlagen/*Linien.shp
CREATE SEQUENCE HWS_LINES_ID_SEQ;
CREATE TABLE hws_lines (
    id int PRIMARY KEY NOT NULL,
    ogr_fid int,
    kind_id int REFERENCES hws_kinds(id) DEFAULT 2,
    fed_state_id int REFERENCES fed_states(id),
    river_id int REFERENCES rivers(id),
    name VARCHAR(256),
    path VARCHAR(256),
    offical INT DEFAULT 0,
    agency VARCHAR(256),
    range VARCHAR(256),
    shore_side INT DEFAULT 0,
    source VARCHAR(256),
    status_date TIMESTAMP,
    description VARCHAR(256)
);
SELECT AddGeometryColumn('hws_lines', 'geom', 31467, 'LINESTRING', 3);
-- TODO: dike_km_from dike_km_to, are they geometries?

ALTER TABLE hws_lines ALTER COLUMN id SET DEFAULT NEXTVAL('HWS_LINES_ID_SEQ');

--Hydrologie/HW-Schutzanlagen/*Punkte.shp
CREATE SEQUENCE HWS_POINTS_ID_SEQ;
CREATE TABLE hws_points (
    id int PRIMARY KEY NOT NULL,
    ogr_fid int,
    kind_id int REFERENCES hws_kinds(id) DEFAULT 2,
    fed_state_id int REFERENCES fed_states(id),
    river_id int REFERENCES rivers(id),
    name VARCHAR,
    path VARCHAR,
    offical INT DEFAULT 0,
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

ALTER TABLE hydr_boundaries_poly ADD COLUMN sectie INT REFERENCES sectie_kinds(id);
ALTER TABLE hydr_boundaries_poly ADD COLUMN sobek INT REFERENCES sobek_kinds(id);
ALTER TABLE hydr_boundaries_poly ADD FOREIGN KEY (kind) REFERENCES boundary_kinds(id);
ALTER TABLE hydr_boundaries ADD COLUMN sectie INT REFERENCES sectie_kinds(id);
ALTER TABLE hydr_boundaries ADD COLUMN sobek INT REFERENCES sobek_kinds(id);
ALTER TABLE hydr_boundaries ADD FOREIGN KEY (kind) REFERENCES boundary_kinds(id);
ALTER TABLE dem ADD COLUMN srid INT NOT NULL;
ALTER TABLE dem ALTER COLUMN year_from DROP NOT NULL;
ALTER TABLE dem ALTER COLUMN year_to DROP NOT NULL;
ALTER TABLE dem ALTER COLUMN projection DROP NOT NULL;
ALTER TABLE dem ALTER COLUMN path SET NOT NULL;

COMMIT;

