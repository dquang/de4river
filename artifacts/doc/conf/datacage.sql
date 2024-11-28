BEGIN;

CREATE SEQUENCE USERS_ID_SEQ;

CREATE TABLE users (
    id  INT  PRIMARY KEY NOT NULL,
    gid UUID             NOT NULL UNIQUE
);

CREATE SEQUENCE COLLECTIONS_ID_SEQ;

CREATE TABLE collections (
    id       INT  PRIMARY KEY NOT NULL,
    gid      UUID             NOT NULL UNIQUE,
    user_id  INT              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name     VARCHAR(256)     NOT NULL,
    creation TIMESTAMP        NOT NULL
);

CREATE SEQUENCE ARTIFACTS_ID_SEQ;

CREATE TABLE artifacts (
    id       INT  PRIMARY KEY NOT NULL,
    gid      UUID             NOT NULL UNIQUE,
    state    VARCHAR(256)     NOT NULL,
    creation TIMESTAMP        NOT NULL
);

CREATE SEQUENCE COLLECTION_ITEMS_ID_SEQ;

CREATE TABLE collection_items (
    id            INT PRIMARY KEY NOT NULL,
    collection_id INT             NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    artifact_id   INT             NOT NULL REFERENCES artifacts(id)   ON DELETE CASCADE
);

CREATE SEQUENCE ARTIFACT_DATA_ID_SEQ;

CREATE TABLE artifact_data (
    id          INT PRIMARY KEY NOT NULL,
    artifact_id INT             NOT NULL REFERENCES artifacts(id) ON DELETE CASCADE,
    kind        VARCHAR(256)    NOT NULL,
    k           VARCHAR(256)    NOT NULL,
    v           TEXT,
    UNIQUE (artifact_id, k)
);

CREATE SEQUENCE OUTS_ID_SEQ;

CREATE TABLE outs (
    id          INT PRIMARY KEY NOT NULL,
    artifact_id INT             NOT NULL REFERENCES artifacts(id) ON DELETE CASCADE,
    name        VARCHAR(256)    NOT NULL,
    description VARCHAR(256),
    out_type    VARCHAR(256)
);

CREATE SEQUENCE FACETS_ID_SEQ;

CREATE TABLE facets (
    id          INT PRIMARY KEY NOT NULL,
    out_id      INT             NOT NULL REFERENCES outs(id) ON DELETE CASCADE,
    name        VARCHAR(256)    NOT NULL,
    num         INT             NOT NULL,
    state       VARCHAR(256)    NOT NULL,
    description VARCHAR(256),
    UNIQUE (out_id, num, name)
);

CREATE VIEW master_artifacts AS
    SELECT a2.id             AS id,
           a2.gid            AS gid,
           a2.state          AS state,
           a2.creation       AS creation,
           ci2.collection_id AS collection_id
    FROM   collection_items ci2 
           JOIN artifacts a2 
             ON ci2.artifact_id = a2.id 
           JOIN (SELECT ci.collection_id AS c_id, 
                        MIN(a.creation)  AS oldest_a 
                 FROM   collection_items ci 
                        JOIN artifacts a 
                          ON ci.artifact_id = a.id 
                 GROUP  BY ci.collection_id) o 
             ON o.c_id = ci2.collection_id 
    WHERE  a2.creation = o.oldest_a;

CREATE VIEW master_artifacts_range AS
    SELECT ma.id                   AS id,
           ma.gid                  AS gid,
           ma.state                AS state,
           ma.creation             AS creation,
           ma.collection_id        AS collection_id,
           mam.ld_mode             AS ld_mode,
           mal.ld_locations        AS ld_locations,
           maf.ld_from             AS ld_from,
           mat.ld_to               AS ld_to
    FROM master_artifacts ma
        LEFT JOIN (SELECT ad.v           AS ld_mode,
                          ad.artifact_id AS artifact_id
                   FROM artifact_data ad
                   WHERE ad.k = 'ld_mode') mam
                   ON mam.artifact_id = ma.id
        LEFT JOIN (SELECT ad.v           AS ld_locations,
                          ad.artifact_id AS artifact_id
                   FROM artifact_data ad
                   WHERE ad.k = 'ld_locations') mal
                   ON mal.artifact_id = ma.id
        LEFT JOIN (SELECT ad.v           AS ld_from,
                          ad.artifact_id AS artifact_id
                   FROM artifact_data ad
                   WHERE ad.k = 'ld_from') maf
                   ON maf.artifact_id = ma.id
        LEFT JOIN (SELECT ad.v           AS ld_to,
                          ad.artifact_id AS artifact_id
                   FROM artifact_data ad
                   WHERE ad.k = 'ld_to') mat
                   ON mat.artifact_id = ma.id;

-- DROP VIEW master_artifacts;
-- DROP VIEW master_artifacts_range;
-- DROP SEQUENCE USERS_ID_SEQ;
-- DROP SEQUENCE COLLECTIONS_ID_SEQ;
-- DROP SEQUENCE ARTIFACTS_ID_SEQ;
-- DROP SEQUENCE COLLECTION_ITEMS_ID_SEQ;
-- DROP SEQUENCE ARTIFACT_DATA_ID_SEQ;
-- DROP SEQUENCE OUTS_ID_SEQ;
-- DROP SEQUENCE FACETS_ID_SEQ;
-- DROP TABLE facets;
-- DROP TABLE outs;
-- DROP TABLE artifact_data;
-- DROP TABLE collection_items;
-- DROP TABLE collections;
-- DROP TABLE artifacts;
-- DROP TABLE users;

COMMIT;
