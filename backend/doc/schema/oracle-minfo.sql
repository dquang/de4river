SET AUTOCOMMIT ON;

CREATE SEQUENCE LOCATION_SYSTEM_SEQ;

CREATE TABLE location_system (
    id          NUMBER(38,0) NOT NULL,
    name        VARCHAR(32)  NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY(id)
);


CREATE SEQUENCE ELEVATION_MODEL_SEQ;

CREATE TABLE elevation_model (
    id          NUMBER(38,0) NOT NULL,
    name        VARCHAR(32)  NOT NULL,
    unit_id     NUMBER(38,0) NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_unit FOREIGN KEY (unit_id) REFERENCES units(id)
);


-- lookup table for bedheight types
CREATE TABLE bed_height_type (
    id          NUMBER(38,0) NOT NULL,
    name        VARCHAR(65)  NOT NULL,
    PRIMARY KEY(id)
);
INSERT INTO bed_height_type VALUES (1, 'Querprofile');
INSERT INTO bed_height_type VALUES (2, 'Flächenpeilung');
INSERT INTO bed_height_type VALUES (3, 'Flächen- u. Querprofilpeilungen');
INSERT INTO bed_height_type VALUES (4, 'DGM');
INSERT INTO bed_height_type VALUES (5, 'TIN');
INSERT INTO bed_height_type VALUES (6, 'Modell');


CREATE SEQUENCE BED_HEIGHT_ID_SEQ;

CREATE TABLE bed_height (
    id                      NUMBER(38,0) NOT NULL,
    river_id                NUMBER(38,0) NOT NULL,
    year                    NUMBER(38,0),
    type_id                 NUMBER(38,0) NOT NULL,
    location_system_id      NUMBER(38,0) NOT NULL,
    cur_elevation_model_id  NUMBER(38,0) NOT NULL,
    old_elevation_model_id  NUMBER(38,0),
    range_id                NUMBER(38,0),
    evaluation_by           VARCHAR(255),
    description             VARCHAR(255),
    PRIMARY KEY(id),
    CONSTRAINT fk_bh_river_id FOREIGN KEY (river_id)
        REFERENCES rivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_bh_type FOREIGN KEY (type_id) REFERENCES bed_height_type(id),
    CONSTRAINT fk_bh_location_system FOREIGN KEY (location_system_id)
        REFERENCES location_system(id),
    CONSTRAINT fk_bh_cur_elevation_model FOREIGN KEY (cur_elevation_model_id)
        REFERENCES elevation_model(id),
    CONSTRAINT fk_bh_old_elevation_model FOREIGN KEY (old_elevation_model_id)
        REFERENCES elevation_model(id),
    CONSTRAINT fk_bh_range FOREIGN KEY (range_id)
        REFERENCES ranges(id) ON DELETE CASCADE
);


CREATE SEQUENCE BED_HEIGHT_VALUES_ID_SEQ;

CREATE TABLE bed_height_values (
    id                      NUMBER(38,0) NOT NULL,
    bed_height_id           NUMBER(38,0) NOT NULL,
    station                 DOUBLE PRECISION NOT NULL,
    height                  DOUBLE PRECISION,
    uncertainty             DOUBLE PRECISION,
    data_gap                DOUBLE PRECISION,
    sounding_width          DOUBLE PRECISION,
    PRIMARY KEY(id),
    UNIQUE (station, bed_height_id),
    CONSTRAINT fk_bed_values_parent FOREIGN KEY (bed_height_id)
        REFERENCES bed_height(id) ON DELETE CASCADE
);


CREATE SEQUENCE DEPTHS_ID_SEQ;

CREATE TABLE depths (
    id      NUMBER(38,0) NOT NULL,
    lower   NUMBER(38,2) NOT NULL,
    upper   NUMBER(38,2) NOT NULL,
    PRIMARY KEY(id)
);


CREATE SEQUENCE SEDIMENT_DENSITY_ID_SEQ;

CREATE TABLE sediment_density (
    id          NUMBER(38,0) NOT NULL,
    river_id    NUMBER(38,0) NOT NULL,
    depth_id    NUMBER(38,0) NOT NULL,
    description VARCHAR(256),
    PRIMARY KEY(id),
    CONSTRAINT fk_sd_river_id FOREIGN KEY (river_id) REFERENCES rivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_sd_depth_id FOREIGN KEY (depth_id) REFERENCES depths(id)
);


CREATE SEQUENCE SEDIMENT_DENSITY_VALUES_ID_SEQ;

CREATE TABLE sediment_density_values (
    id                  NUMBER(38,0) NOT NULL,
    sediment_density_id NUMBER(38,0) NOT NULL,
    station             NUMBER(38,2) NOT NULL,
    shore_offset	NUMBER(38,2),
    density             NUMBER(38,2) NOT NULL,
    description         VARCHAR(256),
    year                NUMBER(38,0),
    PRIMARY KEY(id),
    CONSTRAINT fk_sdv_sediment_density_id FOREIGN KEY(sediment_density_id) REFERENCES sediment_density(id) ON DELETE CASCADE
);


CREATE SEQUENCE POROSITY_ID_SEQ;

CREATE TABLE porosity (
    id               NUMBER(38,0) NOT NULL,
    river_id         NUMBER(38,0) NOT NULL,
    depth_id         NUMBER(38,0) NOT NULL,
    description      VARCHAR(256),
    time_interval_id NUMBER(38,0) NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_p_river_id FOREIGN KEY (river_id) REFERENCES rivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_p_depth_id FOREIGN KEY (depth_id) REFERENCES depths(id),
    CONSTRAINT fk_p_time_interval_id FOREIGN KEY (time_interval_id) REFERENCES time_intervals(id)
);


CREATE SEQUENCE POROSITY_VALUES_ID_SEQ;

CREATE TABLE porosity_values (
    id                  NUMBER(38,0) NOT NULL,
    porosity_id         NUMBER(38,0) NOT NULL,
    station             DOUBLE PRECISION NOT NULL,
    shore_offset        DOUBLE PRECISION,
    porosity            DOUBLE PRECISION NOT NULL,
    description         VARCHAR(256),
    PRIMARY KEY(id),
    CONSTRAINT fk_pv_porosity_id FOREIGN KEY(porosity_id) REFERENCES porosity(id) ON DELETE CASCADE
);


CREATE SEQUENCE MORPHOLOGIC_WIDTH_ID_SEQ;

CREATE TABLE morphologic_width (
    id          NUMBER(38,0) NOT NULL,
    river_id    NUMBER(38,0) NOT NULL,
    unit_id     NUMBER(38,0) NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_mw_river_id FOREIGN KEY(river_id) REFERENCES rivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_mw_unit_id FOREIGN KEY(unit_id) REFERENCES units(id)
);


CREATE SEQUENCE MORPH_WIDTH_VALUES_ID_SEQ;

CREATE TABLE morphologic_width_values (
    id                      NUMBER(38,0) NOT NULL,
    morphologic_width_id    NUMBER(38,0) NOT NULL,
    station                 NUMBER(38,3) NOT NULL,
    width                   NUMBER(38,3) NOT NULL,
    description             VARCHAR(256),
    PRIMARY KEY(id),
    CONSTRAINT fk_mwv_morphologic_width_id FOREIGN KEY (morphologic_width_id) REFERENCES morphologic_width(id) ON DELETE CASCADE
);


CREATE SEQUENCE DISCHARGE_ZONE_ID_SEQ;

CREATE TABLE discharge_zone (
    id                      NUMBER(38,0) NOT NULL,
    river_id                NUMBER(38,0) NOT NULL,
    gauge_name              VARCHAR(64)  NOT NULL, -- this is not very proper, but there are gauges with no db instance
    value                   NUMBER(38,3) NOT NULL,
    lower_discharge         VARCHAR(64)  NOT NULL,
    upper_discharge         VARCHAR(64),
    PRIMARY KEY(id),
    CONSTRAINT fk_dz_river_id FOREIGN KEY (river_id) REFERENCES rivers(id) ON DELETE CASCADE
);


CREATE SEQUENCE FLOW_VELOCITY_MODEL_ID_SEQ;

CREATE TABLE flow_velocity_model (
    id                  NUMBER(38,0) NOT NULL,
    discharge_zone_id   NUMBER(38,0) NOT NULL,
    description         VARCHAR(256),
    PRIMARY KEY (id),
    CONSTRAINT fk_fvm_discharge_zone_id FOREIGN KEY (discharge_zone_id) REFERENCES discharge_zone (id) ON DELETE CASCADE
);


CREATE SEQUENCE FLOW_VELOCITY_M_VALUES_ID_SEQ;

CREATE TABLE flow_velocity_model_values (
    id                      NUMBER(38,0) NOT NULL,
    flow_velocity_model_id  NUMBER(38,0) NOT NULL,
    station                 NUMBER(38,3) NOT NULL,
    q                       NUMBER(38,3) NOT NULL,
    total_channel           NUMBER(38,3) NOT NULL,
    main_channel            NUMBER(38,3) NOT NULL,
    shear_stress            NUMBER(38,3) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE (station, flow_velocity_model_id),
    CONSTRAINT fk_fvv_flow_velocity_model_id FOREIGN KEY (flow_velocity_model_id) REFERENCES flow_velocity_model(id) ON DELETE CASCADE
);



CREATE SEQUENCE FV_MEASURE_ID_SEQ;

CREATE TABLE flow_velocity_measurements (
    id          NUMBER(38,0) NOT NULL,
    river_id    NUMBER(38,0) NOT NULL,
    description VARCHAR(256),
    PRIMARY KEY (id),
    CONSTRAINT fk_fvm_rivers_id FOREIGN KEY (river_id) REFERENCES rivers(id) ON DELETE CASCADE
);

CREATE SEQUENCE FV_MEASURE_VALUES_ID_SEQ;

CREATE TABLE flow_velocity_measure_values (
    id              NUMBER(38,0) NOT NULL,
    measurements_id NUMBER(38,0) NOT NULL,
    station         NUMBER(38,3) NOT NULL,
    datetime        TIMESTAMP,
    w               NUMBER(38,3) NOT NULL,
    q               NUMBER(38,3) NOT NULL,
    v               NUMBER(38,3) NOT NULL,
    description     VARCHAR(256),
    PRIMARY KEY (id),
    CONSTRAINT fk_fvmv_measurements_id FOREIGN KEY (measurements_id) REFERENCES flow_velocity_measurements (id) ON DELETE CASCADE
);


CREATE SEQUENCE GRAIN_FRACTION_ID_SEQ;

CREATE TABLE grain_fraction (
    id      NUMBER(38,0)   NOT NULL,
    name    VARCHAR(64)    NOT NULL,
    lower   NUMBER(38,3),
    upper   NUMBER(38,3),
    PRIMARY KEY (id),
    UNIQUE(name, lower, upper)
);
-- single fractions
INSERT INTO grain_fraction VALUES (1, 'coarse', 16, 200);
INSERT INTO grain_fraction VALUES (2, 'fine_middle', 2, 16);
INSERT INTO grain_fraction VALUES (3, 'sand', 0.063, 2);
INSERT INTO grain_fraction VALUES (4, 'susp_sand', 0.063, 2);
INSERT INTO grain_fraction VALUES (5, 'susp_sand_bed', 0.063, 2);
INSERT INTO grain_fraction VALUES (6, 'suspended_sediment', 0, 0.063);
-- aggregations of fractions
INSERT INTO grain_fraction VALUES (7, 'total', 0, 200);
INSERT INTO grain_fraction VALUES (8, 'bed_load', 0.063, 200);
INSERT INTO grain_fraction VALUES (9, 'suspended_load', 0, 2);


--lookup table for sediment yield kinds
CREATE TABLE sediment_load_kinds (
    id 	     int PRIMARY KEY NOT NULL,
    kind     VARCHAR(64) NOT NULL
);
INSERT INTO sediment_load_kinds (id, kind) VALUES (0, 'non-official');
INSERT INTO sediment_load_kinds (id, kind) VALUES (1, 'official');

CREATE SEQUENCE SEDIMENT_LOAD_LS_ID_SEQ;

CREATE TABLE sediment_load_ls (
    id                  NUMBER(38,0) NOT NULL,
    river_id            NUMBER(38,0) NOT NULL,
    grain_fraction_id   NUMBER(38,0),
    unit_id             NUMBER(38,0) NOT NULL,
    time_interval_id    NUMBER(38,0) NOT NULL,
    sq_time_interval_id NUMBER(38,0),
    description         VARCHAR(256),
    kind                NUMBER(38,0),
    PRIMARY KEY (id),
    CONSTRAINT fk_slls_river_id FOREIGN KEY (river_id)
        REFERENCES rivers(id) ON DELETE CASCADE,
    CONSTRAINT fk_slls_kind_id FOREIGN KEY (kind)
        REFERENCES sediment_load_kinds(id),
    CONSTRAINT fk_slls_grain_fraction_id FOREIGN KEY (grain_fraction_id)
        REFERENCES grain_fraction(id),
    CONSTRAINT fk_slls_unit_id FOREIGN KEY (unit_id)
        REFERENCES units(id),
    CONSTRAINT fk_slls_time_interval_id FOREIGN KEY (time_interval_id)
        REFERENCES time_intervals(id),
    CONSTRAINT fk_slls_sq_time_interval_id FOREIGN KEY (sq_time_interval_id)
        REFERENCES time_intervals(id)
);


CREATE SEQUENCE SEDIMENT_LOAD_LS_VALUES_ID_SEQ;

CREATE TABLE sediment_load_ls_values (
    id                  NUMBER(38,0) NOT NULL,
    sediment_load_ls_id   NUMBER(38,0) NOT NULL,
    station             NUMBER(38,3) NOT NULL,
    value               NUMBER(38,3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sllsv_sediment_load_ls_id FOREIGN KEY (sediment_load_ls_id)
        REFERENCES sediment_load_ls(id) ON DELETE CASCADE
);


CREATE SEQUENCE MEASUREMENT_STATION_ID_SEQ;
CREATE TABLE measurement_station (
    id                       int          NOT NULL,
    range_id                 int          NOT NULL,
    reference_gauge_id       int,
    time_interval_id         int,
    name                     VARCHAR2(256 CHAR) NOT NULL,
    measurement_type         VARCHAR2(64 CHAR)  NOT NULL,
    riverside                VARCHAR2(16 CHAR),
    -- store name of reference gauges here too, as not all are in gauges
    reference_gauge_name     VARCHAR2(64 CHAR),
    operator                 VARCHAR2(64 CHAR),
    commentary               VARCHAR2(512 CHAR),
    PRIMARY KEY (id),
    CHECK(measurement_type IN ('Geschiebe', 'Schwebstoff')),
    CONSTRAINT fk_ms_range_id FOREIGN KEY (range_id)
        REFERENCES ranges(id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_reference_gauge_id FOREIGN KEY (reference_gauge_id)
        REFERENCES gauges(id) ON DELETE CASCADE,
    CONSTRAINT fk_time_interval_id FOREIGN KEY (time_interval_id)
        REFERENCES time_intervals(id)
);


CREATE SEQUENCE SEDIMENT_LOAD_ID_SEQ;

CREATE TABLE sediment_load (
    id                    int NOT NULL,
    grain_fraction_id     int NOT NULL,
    time_interval_id      int NOT NULL,
    sq_time_interval_id   int,
    description           VARCHAR(256),
    kind                  int,
    PRIMARY KEY (id),
    CONSTRAINT fk_sl_kind_id FOREIGN KEY (kind)
        REFERENCES sediment_load_kinds(id),
    CONSTRAINT fk_sl_grain_fraction_id FOREIGN KEY (grain_fraction_id)
        REFERENCES grain_fraction(id),
    CONSTRAINT fk_sl_time_interval_id FOREIGN KEY (time_interval_id)
        REFERENCES time_intervals(id),
    CONSTRAINT fk_sl_sq_time_interval_id FOREIGN KEY (sq_time_interval_id)
        REFERENCES time_intervals(id)
);


CREATE SEQUENCE SEDIMENT_LOAD_VALUES_ID_SEQ;

CREATE TABLE sediment_load_values (
    id                      int NOT NULL,
    sediment_load_id        int NOT NULL,
    measurement_station_id  int NOT NULL,
    value                   DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_slv_sediment_load_id FOREIGN KEY (sediment_load_id)
        REFERENCES sediment_load(id) ON DELETE CASCADE,
    CONSTRAINT fk_slv_m_station_id FOREIGN KEY (measurement_station_id)
        REFERENCES measurement_station(id) ON DELETE CASCADE
);


CREATE SEQUENCE SQ_RELATION_ID_SEQ;

CREATE TABLE sq_relation (
    id               NUMBER(38,0) NOT NULL,
    time_interval_id NUMBER(38,0) NOT NULL,
    description      VARCHAR(256),
    PRIMARY KEY (id),
    CONSTRAINT fk_sqr_tinterval_id FOREIGN KEY (time_interval_id) REFERENCES time_intervals(id)
);


CREATE SEQUENCE SQ_RELATION_VALUES_ID_SEQ;

CREATE TABLE sq_relation_value (
    id                       NUMBER(38,0) NOT NULL,
    sq_relation_id           NUMBER(38,0) NOT NULL,
    measurement_station_id   NUMBER(38,0) NOT NULL,
    parameter                VARCHAR(1) NOT NULL,
    a                        NUMBER(38,20) NOT NULL,
    b                        NUMBER(38,20) NOT NULL,
    qmax                     NUMBER(38,20) NOT NULL,
    rsq                      NUMBER(38,3),
    ntot                     NUMBER(38,0),
    noutl                    NUMBER(38,0),
    cferguson                NUMBER(38,20),
    cduan                    NUMBER(38,20),
    PRIMARY KEY (id),
    UNIQUE(sq_relation_id, measurement_station_id, parameter),
    CONSTRAINT fk_sqr_id FOREIGN KEY (sq_relation_id) REFERENCES sq_relation(id) ON DELETE CASCADE,
    CONSTRAINT fk_mstation_id FOREIGN KEY (measurement_station_id) REFERENCES measurement_station(id) ON DELETE CASCADE
);
