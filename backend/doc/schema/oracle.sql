-- ANNOTATION_TYPES
CREATE SEQUENCE ANNOTATION_TYPES_ID_SEQ;

CREATE TABLE annotation_types (
    id              NUMBER(38,0) NOT NULL,
    name            VARCHAR2(255) NOT NULL UNIQUE,
    PRIMARY KEY     (id)
);


-- ANNOTATIONS
CREATE SEQUENCE ANNOTATIONS_ID_SEQ;

CREATE TABLE annotations (
    id              NUMBER(38,0) NOT NULL,
    attribute_id    NUMBER(38,0) NOT NULL,
    edge_id         NUMBER(38,0),
    position_id     NUMBER(38,0),
    range_id        NUMBER(38,0),
    type_id         NUMBER(38,0),
    PRIMARY KEY     (id)
);


-- ATTRIBUTES
CREATE SEQUENCE ATTRIBUTES_ID_SEQ;

CREATE TABLE attributes (
    id              NUMBER(38,0) NOT NULL,
    value           VARCHAR2(255) UNIQUE,
    primary key     (id)
);
-- value can not be NOT NULL in Oracle:
-- '' is needed here and silently converted to NULL in Oracle

-- CROSS_SECTION_LINES
CREATE SEQUENCE CROSS_SECTION_LINES_ID_SEQ;

CREATE TABLE cross_section_lines (
    id                  NUMBER(38,0) NOT NULL,
    km                  NUMBER(38,5) NOT NULL,
    cross_section_id    NUMBER(38,0) NOT NULL,
    PRIMARY KEY         (id),
    UNIQUE (km, cross_section_id)
);


-- CROSS_SECTION_POINTS
CREATE SEQUENCE CROSS_SECTION_POINTS_ID_SEQ;

CREATE TABLE cross_section_points (
    id                      NUMBER(38,0) NOT NULL,
    col_pos                 NUMBER(38,0) NOT NULL,
    x                       NUMBER(38,2) NOT NULL,
    y                       NUMBER(38,2) NOT NULL,
    cross_section_line_id   NUMBER(38,0) NOT NULL,
    PRIMARY KEY             (id)
);


-- CROSS_SECTIONS
CREATE SEQUENCE CROSS_SECTIONS_ID_SEQ;

CREATE TABLE cross_sections (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255),
    river_id            NUMBER(38,0) NOT NULL,
    time_interval_id    NUMBER(38,0),
    PRIMARY KEY         (id)
);

-- Indices for faster access of the points
CREATE INDEX cross_section_lines_km_idx
    ON cross_section_lines(km);
CREATE INDEX cross_section_points_line_idx
    ON cross_section_points(cross_section_line_id);

-- DISCHARGE_TABLE_VALUES
CREATE SEQUENCE DISCHARGE_TABLE_VALUES_ID_SEQ;

CREATE TABLE discharge_table_values (
    id                  NUMBER(38,0) NOT NULL,
    q                   NUMBER(38,4) NOT NULL,
    w                   NUMBER(38,2) NOT NULL,
    table_id            NUMBER(38,0) NOT NULL,
    UNIQUE (table_id, q, w),
    PRIMARY KEY         (id)
);


-- DISCHARGE_TABLES
CREATE SEQUENCE DISCHARGE_TABLES_ID_SEQ;

CREATE TABLE discharge_tables (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255) NOT NULL,
    bfg_id              VARCHAR2(50),
    kind                NUMBER(38,0) DEFAULT 0 NOT NULL,
    gauge_id            NUMBER(38,0) NOT NULL,
    time_interval_id    NUMBER(38,0),
    PRIMARY KEY         (id),
    UNIQUE(gauge_id, bfg_id, kind)
);


-- EDGES
CREATE SEQUENCE EDGES_ID_SEQ;

CREATE TABLE edges (
    id                  NUMBER(38,0) NOT NULL,
    bottom              NUMBER(38,2),
    top                 NUMBER(38,2),
    PRIMARY KEY         (id)
);


-- GAUGES
CREATE SEQUENCE GAUGES_ID_SEQ;

CREATE TABLE gauges (
    id                  NUMBER(38,0) NOT NULL,
    aeo                 NUMBER(38,2) NOT NULL,
    datum               NUMBER(38,2) NOT NULL,
    name                VARCHAR2(255) NOT NULL,
    station             NUMBER(38,4) NOT NULL,
    official_number     NUMBER(38,0),
    range_id            NUMBER(38,0),
    -- TODO: remove river id here because range_id references river already
    river_id            NUMBER(38,0) NOT NULL,
    PRIMARY KEY         (id),
    UNIQUE (name, river_id),
    UNIQUE (official_number, river_id),
    UNIQUE (river_id, station)
);


-- HYK_ENTRIES
CREATE SEQUENCE HYK_ENTRIES_ID_SEQ;

CREATE TABLE hyk_entries (
    id                  NUMBER(38,0) NOT NULL,
    km                  NUMBER(38,2),
    measure             TIMESTAMP,
    hyk_id              NUMBER(38,0),
    PRIMARY KEY         (id)
);


-- HYK_FLOW_ZONE_TYPES
CREATE SEQUENCE HYK_FLOW_ZONE_TYPES_ID_SEQ;

CREATE TABLE hyk_flow_zone_types (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255),
    name                VARCHAR2(255),
    PRIMARY KEY         (id)
);


-- HYK_FLOW_ZONES
CREATE SEQUENCE HYK_FLOW_ZONES_ID_SEQ;

CREATE TABLE hyk_flow_zones (
    id                  NUMBER(38,0) NOT NULL,
    a                   NUMBER(38,2),
    b                   NUMBER(38,2),
    formation_id        NUMBER(38,0),
    type_id             NUMBER(38,0),
    primary key         (id)
);


-- HYK_FORMATIONS
CREATE SEQUENCE HYK_FORMATIONS_ID_SEQ;

CREATE TABLE hyk_formations (
    id                  NUMBER(38,0) NOT NULL,
    bottom              NUMBER(38,2),
    distance_hf         NUMBER(38,2),
    distance_vl         NUMBER(38,2),
    distance_vr         NUMBER(38,2),
    formation_num       NUMBER(38,0),
    top                 NUMBER(38,2),
    hyk_entry_id        NUMBER(38,0),
    PRIMARY KEY         (id)
);


-- HYKS
CREATE SEQUENCE HYKS_ID_SEQ;

CREATE TABLE hyks (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255),
    river_id            NUMBER(38,0),
    primary key         (id)
);


-- MAIN_VALUE_TYPES
CREATE SEQUENCE MAIN_VALUE_TYPES_ID_SEQ;

CREATE TABLE main_value_types (
    id                  NUMBER(38,0) NOT NULL,
    name                VARCHAR2(255) NOT NULL UNIQUE,
    PRIMARY KEY         (id)
);


-- MAIN_VALUES
CREATE SEQUENCE MAIN_VALUES_ID_SEQ;

CREATE TABLE main_values (
    id                  NUMBER(38,0) NOT NULL,
    value               NUMBER(38,2) NOT NULL,
    gauge_id            NUMBER(38,0) NOT NULL,
    named_value_id      NUMBER(38,0) NOT NULL,
    time_interval_id    NUMBER(38,0),
    UNIQUE (gauge_id, named_value_id, time_interval_id),
    PRIMARY KEY         (id)
);


-- NAMED_MAIN_VALUES
CREATE SEQUENCE NAMED_MAIN_VALUES_ID_SEQ;

CREATE TABLE named_main_values (
    id                  NUMBER(38,0) NOT NULL,
    name                VARCHAR2(256) NOT NULL,
    type_id             NUMBER(38,0) NOT NULL,
    PRIMARY KEY (id)
);


-- POSITIONS
CREATE SEQUENCE POSITIONS_ID_SEQ;

CREATE TABLE positions (
    id                  NUMBER(38,0) NOT NULL,
    value               VARCHAR2(255 char) NOT NULL UNIQUE,
    PRIMARY KEY         (id)
);


--- RANGES
CREATE SEQUENCE RANGES_ID_SEQ;

CREATE TABLE ranges (
    id                  NUMBER(38,0) NOT NULL,
    a                   NUMBER(38,10) NOT NULL,
    b                   NUMBER(38,10),
    river_id            NUMBER(38,0),
    UNIQUE (river_id, a, b),
    PRIMARY KEY (id),
    CHECK (a < b)
);

-- SEDDB_NAME
-- Lookup table for optional matching with differing river names in SedDB
-- Add name here and set rivers.seddb_name_id to id
CREATE TABLE seddb_name (
    id                  NUMBER(38,0) NOT NULL,
    name                VARCHAR2(255) NOT NULL,
    PRIMARY KEY         (id)
);

-- RIVERS
CREATE SEQUENCE RIVERS_ID_SEQ;

CREATE TABLE rivers (
    id                  NUMBER(38,0) NOT NULL,
    model_uuid          CHAR(36 CHAR) UNIQUE,
    official_number     NUMBER(38,0),
    km_up               int DEFAULT 0 NOT NULL,
    name                VARCHAR2(255) NOT NULL UNIQUE,
    wst_unit_id         NUMBER(38,0) NOT NULL,
    seddb_name_id       NUMBER(38,0),
    PRIMARY KEY         (id),
    CHECK(km_up IN(0,1))
);


-- TIME_INTERVALS
CREATE SEQUENCE TIME_INTERVALS_ID_SEQ;

CREATE TABLE time_intervals (
    id                  NUMBER(38,0) NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    stop_time           TIMESTAMP,
    PRIMARY KEY         (id),
    CHECK (start_time <= stop_time)
);


--- UNITS
CREATE SEQUENCE UNITS_ID_SEQ;

CREATE TABLE units (
    id                  NUMBER(38,0) NOT NULL,
    name                VARCHAR2(255) NOT NULL UNIQUE,
    PRIMARY KEY         (id)
);


-- WST_COLUMN_Q_RANGES
CREATE SEQUENCE WST_COLUMN_Q_RANGES_ID_SEQ;

CREATE TABLE wst_column_q_ranges (
    id                  NUMBER(38,0) NOT NULL,
    wst_column_id       NUMBER(38,0) NOT NULL,
    wst_q_range_id      NUMBER(38,0) NOT NULL,
    UNIQUE (wst_column_id, wst_q_range_id),
    PRIMARY KEY         (id)
);


-- WST_COLUMN_VALUES
CREATE SEQUENCE WST_COLUMN_VALUES_ID_SEQ;

CREATE TABLE wst_column_values (
    id                  NUMBER(38,0) NOT NULL,
    position            NUMBER(38,5) NOT NULL,
    w                   NUMBER(38,5) NOT NULL,
    wst_column_id       NUMBER(38,0) NOT NULL,
    UNIQUE (position, wst_column_id),
    UNIQUE (position, wst_column_id, w),
    PRIMARY KEY         (id)
);


-- WST_COLUMNS
CREATE SEQUENCE WST_COLUMNS_ID_SEQ;

CREATE TABLE wst_columns (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255),
    name                VARCHAR2(255) NOT NULL,
    source              VARCHAR(256),
    position            NUMBER(38,0) DEFAULT 0 NOT NULL,
    time_interval_id    NUMBER(38,0),
    wst_id              NUMBER(38,0) NOT NULL,
    UNIQUE (wst_id, name),
    UNIQUE (wst_id, position),
    PRIMARY KEY         (id)
);


-- WST_Q_RANGES
CREATE SEQUENCE WST_Q_RANGES_ID_SEQ;

CREATE TABLE wst_q_ranges (
    id                  NUMBER(38,0) NOT NULL,
    q                   NUMBER(38,5) NOT NULL,
    range_id            NUMBER(38,0) NOT NULL,
    PRIMARY KEY         (id)
);

-- OFFICIAL_LINES
CREATE SEQUENCE OFFICIAL_LINES_ID_SEQ;

CREATE TABLE official_lines (
    id                  NUMBER(38,0) NOT NULL,
    wst_column_id       NUMBER(38,0) NOT NULL,
    named_main_value_id NUMBER(38,0) NOT NULL,
    UNIQUE (wst_column_id, named_main_value_id),
    PRIMARY KEY (id)
);

-- WSTS
--lookup table for wst kinds
CREATE TABLE wst_kinds (
    id 	     NUMBER PRIMARY KEY NOT NULL,
    kind     VARCHAR(64) NOT NULL
);
INSERT INTO wst_kinds (id, kind) VALUES (0, 'basedata');
INSERT INTO wst_kinds (id, kind) VALUES (1, 'basedata_additionals_marks');
INSERT INTO wst_kinds (id, kind) VALUES (2, 'basedata_fixations_wst');
INSERT INTO wst_kinds (id, kind) VALUES (3, 'basedata_officials');
INSERT INTO wst_kinds (id, kind) VALUES (4, 'basedata_heightmarks-points-relative_points');
INSERT INTO wst_kinds (id, kind) VALUES (5, 'basedata_flood-protections_relative_points');
INSERT INTO wst_kinds (id, kind) VALUES (6, 'morpho_waterlevel-differences');
INSERT INTO wst_kinds (id, kind) VALUES (7, 'morpho_waterlevels');


CREATE SEQUENCE WSTS_ID_SEQ;

CREATE TABLE wsts (
    id                  NUMBER(38,0) NOT NULL,
    description         VARCHAR2(255) NOT NULL,
    kind                NUMBER(38,0) NOT NULL,
    river_id            NUMBER(38,0) NOT NULL,
    UNIQUE (river_id, description),
    PRIMARY KEY         (id)
);


-- ADD CONSTRAINTs
ALTER TABLE annotations ADD CONSTRAINT cAnnotationsAttributes FOREIGN KEY (attribute_id) REFERENCES attributes;
ALTER TABLE annotations ADD CONSTRAINT cAnnotationsEdges FOREIGN KEY (edge_id) REFERENCES edges;
ALTER TABLE annotations ADD CONSTRAINT cAnnotationsPositions FOREIGN KEY (position_id) REFERENCES positions;
ALTER TABLE annotations ADD CONSTRAINT cAnnotationsTypes FOREIGN KEY (type_id) REFERENCES annotation_types;
ALTER TABLE cross_sections ADD CONSTRAINT cCrossSectionsTimeIntervals FOREIGN KEY (time_interval_id) REFERENCES time_intervals;
ALTER TABLE discharge_tables ADD CONSTRAINT cDischargeTablesTime_intervals FOREIGN KEY (time_interval_id) REFERENCES time_intervals;
ALTER TABLE hyk_flow_zones ADD CONSTRAINT cHykFlowZonesHykFlowZoneTypes FOREIGN KEY (type_id) REFERENCES hyk_flow_zone_types;
ALTER TABLE main_values ADD CONSTRAINT cMainValuesNamedMainValues FOREIGN KEY (named_value_id) REFERENCES named_main_values;
ALTER TABLE main_values ADD CONSTRAINT cMainValuesTimeIntervals FOREIGN KEY (time_interval_id) REFERENCES time_intervals;
ALTER TABLE named_main_values ADD CONSTRAINT cNamedMainValuesMainValueTypes FOREIGN KEY (type_id) REFERENCES main_value_types;
ALTER TABLE rivers ADD CONSTRAINT cRiversUnits FOREIGN KEY (wst_unit_id) REFERENCES units;
ALTER TABLE rivers ADD CONSTRAINT cRiversSeddbNames FOREIGN KEY (seddb_name_id) REFERENCES seddb_name;
ALTER TABLE wst_columns ADD CONSTRAINT cWstColumnsTime_intervals FOREIGN KEY (time_interval_id) REFERENCES time_intervals;

-- Cascading references
ALTER TABLE annotations ADD CONSTRAINT cAnnotationsRanges FOREIGN KEY (range_id) REFERENCES ranges ON DELETE CASCADE;
ALTER TABLE cross_section_lines ADD CONSTRAINT cQPSLinesCrossSections FOREIGN KEY (cross_section_id) REFERENCES cross_sections ON DELETE CASCADE;
ALTER TABLE cross_section_points ADD CONSTRAINT cQPSPointsCrossSectionLines FOREIGN KEY (cross_section_line_id) REFERENCES cross_section_lines ON DELETE CASCADE;
ALTER TABLE cross_sections ADD CONSTRAINT cCrossSectionsRivers FOREIGN KEY (river_id) REFERENCES rivers ON DELETE CASCADE;
ALTER TABLE discharge_tables ADD CONSTRAINT cDischargeTablesGauges FOREIGN KEY (gauge_id) REFERENCES gauges ON DELETE CASCADE;
ALTER TABLE discharge_table_values ADD CONSTRAINT cTableValuesDischargeTables FOREIGN KEY (table_id) REFERENCES discharge_tables ON DELETE CASCADE;
ALTER TABLE gauges ADD CONSTRAINT cGaugesRanges FOREIGN KEY (range_id) REFERENCES ranges ON DELETE CASCADE;
ALTER TABLE gauges ADD CONSTRAINT cGaugesRivers FOREIGN KEY (river_id) REFERENCES rivers ON DELETE CASCADE;
ALTER TABLE hyk_entries ADD CONSTRAINT cHykEntriesHyks FOREIGN KEY (hyk_id) REFERENCES hyks ON DELETE CASCADE;
ALTER TABLE hyk_flow_zones ADD CONSTRAINT cHykFlowZonesHykFormations FOREIGN KEY (formation_id) REFERENCES hyk_formations ON DELETE CASCADE;
ALTER TABLE hyk_formations ADD CONSTRAINT cHykFormationsHykEntries FOREIGN KEY (hyk_entry_id) REFERENCES hyk_entries ON DELETE CASCADE;
ALTER TABLE hyks ADD CONSTRAINT cHyksRivers FOREIGN KEY (river_id) REFERENCES rivers ON DELETE CASCADE;
ALTER TABLE main_values ADD CONSTRAINT cMainValuesGauges FOREIGN KEY (gauge_id) REFERENCES gauges ON DELETE CASCADE;
ALTER TABLE ranges ADD CONSTRAINT cRangesRivers FOREIGN KEY (river_id) REFERENCES rivers ON DELETE CASCADE;
ALTER TABLE wst_column_q_ranges ADD CONSTRAINT cWstColumnQRangesWstColums FOREIGN KEY (wst_column_id) REFERENCES wst_columns ON DELETE CASCADE;
ALTER TABLE wst_column_q_ranges ADD CONSTRAINT cWstColumnQRangesWstQRanges FOREIGN KEY (wst_q_range_id) REFERENCES wst_q_ranges ON DELETE CASCADE;
ALTER TABLE wst_columns ADD CONSTRAINT cWstColumnsWsts FOREIGN KEY (wst_id) REFERENCES wsts ON DELETE CASCADE;
ALTER TABLE wst_column_values ADD CONSTRAINT cWstColumnValuesWstColumns FOREIGN KEY (wst_column_id) REFERENCES wst_columns ON DELETE CASCADE;
ALTER TABLE wst_q_ranges ADD CONSTRAINT cWstQRangesRanges FOREIGN KEY (range_id) REFERENCES RANGES ON DELETE CASCADE;
ALTER TABLE wsts ADD CONSTRAINT cWstsRivers FOREIGN KEY (river_id) REFERENCES rivers ON DELETE CASCADE;
ALTER TABLE wsts ADD CONSTRAINT cWstsWstKinds FOREIGN KEY (kind) REFERENCES wst_kinds;

ALTER TABLE official_lines ADD CONSTRAINT cOffLinesWstColumns FOREIGN KEY (wst_column_id) REFERENCES wst_columns ON DELETE CASCADE;
ALTER TABLE official_lines ADD CONSTRAINT cOffLinesNamedMainValues FOREIGN KEY (named_main_value_id) REFERENCES named_main_values ON DELETE CASCADE;

-- VIEWS

CREATE VIEW wst_value_table AS
    SELECT
           wcv.position AS position,
           w,
           q,
           wc.position AS column_pos,
           w.id AS wst_id
        FROM wsts w
        JOIN wst_columns wc
             ON wc.wst_id=w.id
        JOIN wst_column_q_ranges wcqr
             ON wcqr.wst_column_id=wc.id
        JOIN wst_q_ranges wqr
             ON wcqr.wst_q_range_id=wqr.id
        JOIN ranges r
             ON wqr.range_id=r.id
        JOIN wst_column_values wcv
             ON wcv.wst_column_id=wc.id AND wcv.position between r.a and r.b
    ORDER  BY wcv.position ASC,
        wc.position DESC;

-- view to select the w values of a WST
CREATE VIEW wst_w_values  AS
    SELECT wcv.position   AS km,
           wcv.w          AS w,
           wc.position    AS column_pos,
           w.id           AS wst_id
        FROM wst_column_values wcv
        JOIN wst_columns wc ON wcv.wst_column_id = wc.id
        JOIN wsts w         ON wc.wst_id = w.id
    ORDER BY wcv.position, wc.position;

-- view to select the q values of a WST
CREATE VIEW wst_q_values AS
    SELECT wc.position AS column_pos,
           wqr.q       AS q,
           r.a         AS a,
           r.b         AS b,
           wc.wst_id   AS wst_id
    FROM wst_column_q_ranges wcqr
    JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id
    JOIN ranges r         ON wqr.range_id        = r.id
    JOIN wst_columns wc   ON wcqr.wst_column_id  = wc.id
    ORDER BY wc.position, wcqr.wst_column_id, r.a;

CREATE VIEW wst_ranges
AS
  SELECT wc.id             AS wst_column_id,
         wc.wst_id         AS wst_id,
         Min(wcv.position) AS a,
         Max(wcv.position) AS b
  FROM   wst_columns wc
         JOIN wst_column_values wcv
           ON wc.id = wcv.wst_column_id
  GROUP  BY wc.id,
            wc.wst_id;
