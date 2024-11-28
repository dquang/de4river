CREATE INDEX river_axes_km_spatial_idx ON river_axes_km(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=point');

CREATE INDEX buildings_spatial_idx ON buildings(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');

CREATE INDEX fixpoints_spatial_idx ON fixpoints(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POINT');

CREATE INDEX river_axes_spatial_idx ON river_axes(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=MULTILINE');

CREATE INDEX CrossSectionTracks_spatial_idx ON cross_section_tracks(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=LINE');

CREATE INDEX floodplain_spatial_idx ON floodplain(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POLYGON');

CREATE INDEX hydr_boundaries_idx ON hydr_boundaries(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=MULTILINE');

CREATE INDEX hws_points_spatial_idx ON hws_points(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POINT');
CREATE INDEX hws_lines_spatial_idx ON hws_lines(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=MULTILINE');

CREATE INDEX floodmaps_spatial_idx ON floodmaps(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=MULTIPOLYGON');

CREATE INDEX hydr_boundaries_poly_idx ON hydr_boundaries_poly(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=MULTIPOLYGON');

CREATE INDEX jetties_idx ON jetties(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POINT');

CREATE INDEX flood_marks_idx ON flood_marks(GEOM) indextype IS MDSYS.SPATIAL_INDEX parameters ('LAYER_GTYPE=POINT');
