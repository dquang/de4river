DROP TRIGGER hws_trigger;
DROP TABLE hws;
DROP SEQUENCE HWS_ID_SEQ;

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

-- HWS-Lines
CREATE SEQUENCE HWS_LINES_ID_SEQ;
CREATE TABLE hws_lines (
    OGR_FID NUMBER(38),
    GEOM MDSYS.SDO_GEOMETRY,
    kind_id NUMBER(2) DEFAULT 2 REFERENCES hws_kinds(id),
    fed_state_id NUMBER(2) REFERENCES fed_states(id),
    river_id NUMBER(38) REFERENCES rivers(id),
    name VARCHAR(256),
    path VARCHAR(256),
    official NUMBER DEFAULT 0,
    agency VARCHAR(256),
    range VARCHAR(256),
    shore_side NUMBER DEFAULT 0,
    source VARCHAR(256),
    status_date TIMESTAMP,
    description VARCHAR(256),
    id NUMBER PRIMARY KEY NOT NULL
);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('hws_lines', 'GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',3282450,3912240,0.001),MDSYS.SDO_DIM_ELEMENT('Y',5248260,6100130,0.001),MDSYS.SDO_DIM_ELEMENT('Z',-100000,100000,0.002)), 31467);
CREATE INDEX hws_lines_spatial_idx ON hws_lines(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');

CREATE OR REPLACE TRIGGER hws_lines_trigger BEFORE INSERT ON hws_lines FOR each ROW
    BEGIN
        SELECT HWS_LINES_ID_SEQ.nextval INTO :new.id FROM dual;
    END;

-- HWS Points lookup tables
CREATE TABLE sectie_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sectie_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO sectie_kinds (id, name) VALUES (1, 'Flussschlauch');
INSERT INTO sectie_kinds (id, name) VALUES (2, 'Uferbank');
INSERT INTO sectie_kinds (id, name) VALUES (3, 'Überflutungsbereich');

CREATE TABLE sobek_kinds (
    id NUMBER PRIMARY KEY NOT NULL,
    name VARCHAR(64) NOT NULL
);
INSERT INTO sobek_kinds (id, name) VALUES (0, 'Unbekannt');
INSERT INTO sobek_kinds (id, name) VALUES (1, 'Stromführend');
INSERT INTO sobek_kinds (id, name) VALUES (2, 'Stromspeichernd');

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
    ogr_fid NUMBER,
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

-- Altrications
ALTER TABLE dem ADD srid NUMBER NOT NULL;
ALTER TABLE hydr_boundaries_poly ADD sectie NUMBER REFERENCES sectie_kinds(id);
ALTER TABLE hydr_boundaries_poly ADD sobek NUMBER REFERENCES sobek_kinds(id);
ALTER TABLE hydr_boundaries ADD sectie NUMBER REFERENCES sectie_kinds(id);
ALTER TABLE hydr_boundaries ADD sobek NUMBER REFERENCES sobek_kinds(id);
ALTER TABLE hydr_boundaries ADD kind NUMBER REFERENCES boundary_kinds(id);
ALTER TABLE hydr_boundaries_poly ADD kind NUMBER REFERENCES boundary_kinds(id);
