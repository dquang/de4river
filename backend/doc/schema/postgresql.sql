BEGIN;

CREATE SEQUENCE UNITS_ID_SEQ;

CREATE TABLE units (
    id   int PRIMARY KEY NOT NULL,
    name VARCHAR(32)     NOT NULL UNIQUE
);

-- SEDDB_NAME
-- Lookup table for optional matching with differing river names in SedDB
-- Add name here and set rivers.seddb_name_id to id
CREATE TABLE seddb_name (
    id       int PRIMARY KEY NOT NULL,
    name     VARCHAR(256) NOT NULL
);

-- Gewaesser
CREATE SEQUENCE RIVERS_ID_SEQ;

CREATE TABLE rivers (
    id              int PRIMARY KEY NOT NULL,
    model_uuid      CHAR(36)        UNIQUE,
    official_number int8,
    name            VARCHAR(256)    NOT NULL UNIQUE,
    km_up           int DEFAULT 0   NOT NULL,
    wst_unit_id int                 NOT NULL REFERENCES units(id),
    seddb_name_id   int REFERENCES seddb_name(id),
    CHECK(km_up IN(0,1))
);

-- Bruecke, Haefen, etc.
CREATE SEQUENCE ATTRIBUTES_ID_SEQ;

CREATE TABLE attributes (
    id    int PRIMARY KEY NOT NULL,
    value VARCHAR(256)    NOT NULL UNIQUE
);

-- segments from/to at a river
CREATE SEQUENCE RANGES_ID_SEQ;

CREATE TABLE ranges (
    id       int PRIMARY KEY NOT NULL,
    river_id int             NOT NULL REFERENCES rivers(id) ON DELETE CASCADE,
    a        NUMERIC         NOT NULL,
    b        NUMERIC,
    UNIQUE (river_id, a, b),
    CHECK (a < b)
);


-- Lage 'links', 'rechts', etc.
CREATE SEQUENCE POSITIONS_ID_SEQ;

CREATE TABLE positions (
    id    int PRIMARY KEY NOT NULL,
    value VARCHAR(256)    NOT NULL UNIQUE
);

-- Kante 'oben', 'unten'
CREATE SEQUENCE EDGES_ID_SEQ;

CREATE TABLE edges (
    id     int PRIMARY KEY NOT NULL,
    top    NUMERIC,
    bottom NUMERIC
);

-- Types of annotatations (Hafen, Bruecke, Zufluss, ...)
CREATE SEQUENCE ANNOTATION_TYPES_ID_SEQ;

CREATE TABLE annotation_types (
    id    int PRIMARY KEY NOT NULL,
    name  VARCHAR(256)    NOT NULL UNIQUE
);

-- Some object (eg. Hafen) at a segment of river
-- plus its position.
CREATE SEQUENCE ANNOTATIONS_ID_SEQ;

CREATE TABLE annotations (
    id           int PRIMARY KEY NOT NULL,
    range_id     int NOT NULL REFERENCES ranges(id) ON DELETE CASCADE,
    attribute_id int NOT NULL REFERENCES attributes(id),
    position_id  int REFERENCES positions(id),
    edge_id      int REFERENCES edges(id),
    type_id      int REFERENCES annotation_types(id)
);

-- Pegel
CREATE SEQUENCE GAUGES_ID_SEQ;

CREATE TABLE gauges (
    id              int PRIMARY KEY NOT NULL,
    name            VARCHAR(256)    NOT NULL,
    -- remove river id here because range_id references river already
    river_id        int             NOT NULL REFERENCES rivers(id) ON DELETE CASCADE,
    station         NUMERIC         NOT NULL,
    aeo             NUMERIC         NOT NULL,
    official_number int8,

    -- Pegelnullpunkt
    datum    NUMERIC NOT NULL,
    -- Streckengueltigkeit
    range_id int REFERENCES ranges (id) ON DELETE CASCADE,

    UNIQUE (name, river_id),
    UNIQUE (official_number, river_id),
    UNIQUE (river_id, station)
);

-- Type of a Hauptwert 'W', 'Q', 'D', etc.
CREATE SEQUENCE MAIN_VALUE_TYPES_ID_SEQ;

CREATE TABLE main_value_types (
    id   int PRIMARY KEY NOT NULL,
    name VARCHAR(256)    NOT NULL UNIQUE
);

--  Named type of a Hauptwert (eg. HQ100)
CREATE SEQUENCE NAMED_MAIN_VALUES_ID_SEQ;

CREATE TABLE named_main_values (
    id      int PRIMARY KEY NOT NULL,
    name    VARCHAR(256)    NOT NULL,
    type_id int NOT NULL REFERENCES main_value_types(id)
);

-- Table for time intervals
CREATE SEQUENCE TIME_INTERVALS_ID_SEQ;

CREATE TABLE time_intervals (
    id         int PRIMARY KEY NOT NULL,
    start_time TIMESTAMP       NOT NULL,
    stop_time  TIMESTAMP,
    CHECK (start_time <= stop_time)
);


-- Stammdaten
CREATE SEQUENCE MAIN_VALUES_ID_SEQ;

CREATE TABLE main_values (
    id             int PRIMARY KEY NOT NULL,
    gauge_id       int NOT NULL REFERENCES gauges(id) ON DELETE CASCADE,
    named_value_id int NOT NULL REFERENCES named_main_values(id),
    value          NUMERIC NOT NULL,

    time_interval_id int REFERENCES time_intervals(id),

    -- TODO: better checks
    UNIQUE (gauge_id, named_value_id, time_interval_id)
);

-- Abflusstafeln
CREATE SEQUENCE DISCHARGE_TABLES_ID_SEQ;

CREATE TABLE discharge_tables (
    id               int PRIMARY KEY NOT NULL,
    gauge_id         int NOT NULL REFERENCES gauges(id) ON DELETE CASCADE,
    description      VARCHAR(256) NOT NULL,
    bfg_id           VARCHAR(50),
    kind             int NOT NULL DEFAULT 0,
    time_interval_id int REFERENCES time_intervals(id),
    UNIQUE(gauge_id, bfg_id, kind)
);

-- Values of the Abflusstafeln
CREATE SEQUENCE DISCHARGE_TABLE_VALUES_ID_SEQ;

CREATE TABLE discharge_table_values (
    id       int PRIMARY KEY NOT NULL,
    table_id int NOT NULL REFERENCES discharge_tables(id) ON DELETE CASCADE,
    q        NUMERIC NOT NULL,
    w        NUMERIC NOT NULL,

    UNIQUE (table_id, q, w)
);

-- WST files
--lookup table for wst kinds
CREATE TABLE wst_kinds (
    id 	     int PRIMARY KEY NOT NULL,
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
    id          int PRIMARY KEY NOT NULL,
    river_id    int NOT NULL REFERENCES rivers(id) ON DELETE CASCADE,
    description VARCHAR(256) NOT NULL,
    kind        int NOT NULL REFERENCES wst_kinds(id) DEFAULT 0,
    -- TODO: more meta infos
    UNIQUE (river_id, description)
);

-- columns of WST files
CREATE SEQUENCE WST_COLUMNS_ID_SEQ;

CREATE TABLE wst_columns (
    id          int PRIMARY KEY NOT NULL,
    wst_id      int NOT NULL REFERENCES wsts(id) ON DELETE CASCADE,
    name        VARCHAR(256) NOT NULL,
    description VARCHAR(256),
    source      VARCHAR(256),
    position    int NOT NULL DEFAULT 0,

    time_interval_id int REFERENCES time_intervals(id),

    UNIQUE (wst_id, name),
    UNIQUE (wst_id, position)
);

-- w values in  WST file column
CREATE SEQUENCE WST_COLUMN_VALUES_ID_SEQ;

CREATE TABLE wst_column_values (
    id            int PRIMARY KEY NOT NULL,
    wst_column_id int NOT NULL REFERENCES wst_columns(id) ON DELETE CASCADE,
    position      NUMERIC NOT NULL,
    w             NUMERIC NOT NULL,

    UNIQUE (position, wst_column_id),
    UNIQUE (position, wst_column_id, w)
);

-- bind q values to range
CREATE SEQUENCE WST_Q_RANGES_ID_SEQ;

CREATE TABLE wst_q_ranges (
    id       int PRIMARY KEY NOT NULL,
    range_id int NOT NULL REFERENCES ranges(id) ON DELETE CASCADE,
    q        NUMERIC NOT NULL
);

-- bind q ranges to wst columns
CREATE SEQUENCE WST_COLUMN_Q_RANGES_ID_SEQ;

CREATE TABLE wst_column_q_ranges (
    id             int PRIMARY KEY NOT NULL,
    wst_column_id  int NOT NULL REFERENCES wst_columns(id) ON DELETE CASCADE,
    wst_q_range_id int NOT NULL REFERENCES wst_q_ranges(id) ON DELETE CASCADE,

    UNIQUE (wst_column_id, wst_q_range_id)
);

CREATE SEQUENCE OFFICIAL_LINES_ID_SEQ;

CREATE TABLE official_lines (
    id                  int PRIMARY KEY NOT NULL,
    wst_column_id       int NOT NULL REFERENCES wst_columns(id) ON DELETE CASCADE,
    named_main_value_id int NOT NULL REFERENCES named_main_values(id) ON DELETE CASCADE,

    UNIQUE (wst_column_id, named_main_value_id)
);

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
CREATE VIEW wst_w_values AS
    SELECT wcv."position" AS km,
           wcv.w          AS w,
           wc."position"  AS column_pos,
           w.id           AS wst_id
        FROM wst_column_values wcv
        JOIN wst_columns wc ON wcv.wst_column_id = wc.id
        JOIN wsts w         ON wc.wst_id = w.id
    ORDER BY wcv."position", wc."position";

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

-- data for the cross-sections

CREATE SEQUENCE CROSS_SECTIONS_ID_SEQ;

CREATE TABLE cross_sections (
    id               int PRIMARY KEY NOT NULL,
    river_id         int NOT NULL REFERENCES rivers(id) ON DELETE CASCADE,
    time_interval_id int REFERENCES time_intervals(id),
    description      VARCHAR(256)
);

CREATE SEQUENCE CROSS_SECTION_LINES_ID_SEQ;

CREATE TABLE cross_section_lines (
    id               int PRIMARY KEY NOT NULL,
    km               NUMERIC         NOT NULL,
    cross_section_id int             NOT NULL REFERENCES cross_sections(id) ON DELETE CASCADE,
    UNIQUE (km, cross_section_id)
);

CREATE SEQUENCE CROSS_SECTION_POINTS_ID_SEQ;

CREATE TABLE cross_section_points (
    id                    int PRIMARY KEY NOT NULL,
    cross_section_line_id int             NOT NULL REFERENCES cross_section_lines(id) ON DELETE CASCADE,
    col_pos               int             NOT NULL,
    x                     NUMERIC         NOT NULL,
    y                     NUMERIC         NOT NULL
);

-- Indices for faster access of the points
CREATE INDEX cross_section_lines_km_idx
    ON cross_section_lines(km);
CREATE INDEX cross_section_points_line_idx
    ON cross_section_points(cross_section_line_id);

-- Hydraulische Kenngroessen

CREATE SEQUENCE HYKS_ID_SEQ;

CREATE TABLE hyks (
    id          int PRIMARY KEY NOT NULL,
    river_id    int             NOT NULL REFERENCES rivers(id) ON DELETE CASCADE,
    description VARCHAR(256)    NOT NULL
);

CREATE SEQUENCE HYK_ENTRIES_ID_SEQ;

CREATE TABLE hyk_entries (
    id          int PRIMARY KEY NOT NULL,
    hyk_id      int             NOT NULL REFERENCES hyks(id) ON DELETE CASCADE,
    km          NUMERIC         NOT NULL,
    measure     TIMESTAMP,
    UNIQUE (hyk_id, km)
);

CREATE SEQUENCE HYK_FORMATIONS_ID_SEQ;

CREATE TABLE hyk_formations (
    id            int PRIMARY KEY NOT NULL,
    formation_num int             NOT NULL DEFAULT 0,
    hyk_entry_id  int             NOT NULL REFERENCES hyk_entries(id) ON DELETE CASCADE,
    top           NUMERIC         NOT NULL,
    bottom        NUMERIC         NOT NULL,
    distance_vl   NUMERIC         NOT NULL,
    distance_hf   NUMERIC         NOT NULL,
    distance_vr   NUMERIC         NOT NULL,
    UNIQUE (hyk_entry_id, formation_num)
);

CREATE SEQUENCE HYK_FLOW_ZONE_TYPES_ID_SEQ;

CREATE TABLE hyk_flow_zone_types (
    id          int PRIMARY KEY NOT NULL,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    description VARCHAR(256)
);

CREATE SEQUENCE HYK_FLOW_ZONES_ID_SEQ;

CREATE TABLE hyk_flow_zones (
    id           int PRIMARY KEY NOT NULL,
    formation_id int             NOT NULL REFERENCES hyk_formations(id) ON DELETE CASCADE,
    type_id      int             NOT NULL REFERENCES hyk_flow_zone_types(id),
    a            NUMERIC         NOT NULL,
    b            NUMERIC         NOT NULL,
    CHECK (a <= b)
);

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

COMMIT;
