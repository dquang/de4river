-- SEDDB_NAME
-- Lookup table for optional matching with differing river names in SedDB
-- Add name here and set rivers.seddb_name_id to id
CREATE TABLE seddb_name (
    id                  NUMBER(38,0) NOT NULL,
    name                VARCHAR2(255) NOT NULL,
    PRIMARY KEY         (id)
);

ALTER TABLE rivers ADD seddb_name_id NUMBER(38,0);

ALTER TABLE rivers ADD CONSTRAINT cRiversSeddbNames
      FOREIGN KEY (seddb_name_id) REFERENCES seddb_name;


-- bed heights
ALTER TABLE bed_height_single DROP CONSTRAINT fk_bed_single_river_id;
ALTER TABLE bed_height_single DROP CONSTRAINT fk_type;
ALTER TABLE bed_height_single DROP CONSTRAINT fk_location_system;
ALTER TABLE bed_height_single DROP CONSTRAINT fk_cur_elevation_model;
ALTER TABLE bed_height_single DROP CONSTRAINT fk_old_elevation_model;
ALTER TABLE bed_height_single DROP CONSTRAINT fk_range;

ALTER TABLE bed_height_single DROP COLUMN sounding_width;

ALTER TABLE bed_height_single RENAME TO bed_height;

ALTER TABLE bed_height ADD CONSTRAINT fk_bh_river_id
      FOREIGN KEY (river_id) REFERENCES rivers(id) ON DELETE CASCADE;
ALTER TABLE bed_height ADD CONSTRAINT fk_bh_type
      FOREIGN KEY (type_id) REFERENCES bed_height_type(id);
ALTER TABLE bed_height ADD CONSTRAINT fk_bh_location_system
      FOREIGN KEY (location_system_id) REFERENCES location_system(id);
ALTER TABLE bed_height ADD CONSTRAINT fk_bh_cur_elevation_model
      FOREIGN KEY (cur_elevation_model_id) REFERENCES elevation_model(id);
ALTER TABLE bed_height ADD CONSTRAINT fk_bh_old_elevation_model
      FOREIGN KEY (old_elevation_model_id) REFERENCES elevation_model(id);
ALTER TABLE bed_height ADD CONSTRAINT fk_bh_range
      FOREIGN KEY (range_id) REFERENCES ranges(id) ON DELETE CASCADE;

-- the following is needed because Oracle is not able to mix DDL with
-- DML in a subselect
VARIABLE seqval NUMBER
BEGIN
    SELECT BED_HEIGHT_SINGLE_ID_SEQ.NEXTVAL INTO :seqval FROM DUAL;
    execute immediate('CREATE SEQUENCE BED_HEIGHT_ID_SEQ START WITH '
                      || :seqval);
END;
/
DROP SEQUENCE BED_HEIGHT_SINGLE_ID_SEQ;


-- bed height values
ALTER TABLE bed_height_single_values
      DROP CONSTRAINT fk_bed_single_values_parent;

ALTER TABLE bed_height_single_values
      RENAME COLUMN bed_height_single_id TO bed_height_id;
ALTER TABLE bed_height_single_values DROP COLUMN width;

ALTER TABLE bed_height_single_values RENAME TO bed_height_values;

ALTER TABLE bed_height_values ADD CONSTRAINT fk_bed_values_parent
      FOREIGN KEY (bed_height_id) REFERENCES bed_height(id) ON DELETE CASCADE;

BEGIN
    SELECT BED_SINGLE_VALUES_ID_SEQ.NEXTVAL INTO :seqval FROM DUAL;
    execute immediate('CREATE SEQUENCE BED_HEIGHT_VALUES_ID_SEQ START WITH '
                      || :seqval);
END;
/
DROP SEQUENCE BED_SINGLE_VALUES_ID_SEQ;


-- measurement stations
ALTER TABLE measurement_station ADD CONSTRAINT check_m_type
      CHECK(measurement_type IN ('Geschiebe', 'Schwebstoff'));


-- SQ relations
ALTER TABLE sq_relation DROP CONSTRAINT fk_sqr_river_id;

ALTER TABLE sq_relation DROP COLUMN river_id;

ALTER TABLE sq_relation_value ADD CONSTRAINT sq_mstation_param_key
      UNIQUE(sq_relation_id, measurement_station_id, parameter);
