try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import utils

TABLE_NAME="hydr_boundaries"
TABLE_NAME_POLY="hydr_boundaries_poly"
PATH="Hydrologie/Hydr.Grenzen"
NAME="Hydr. Boundaries"


class HydrBoundary(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)

    def getTablename(self):
        return TABLE_NAME

    def getName(self):
        return NAME

    def isGeometryValid(self, geomType):
        return geomType in [ogr.wkbLineString,
                            ogr.wkbLineString25D,
                            ogr.wkbMultiLineString25D,
                            ogr.wkbMultiLineString]

    def isShapeRelevant(self, name, path):
        shp = ogr.Open(path)
        if self.isGeometryValid(shp.GetLayerByName(name).GetGeomType()) and \
                self.getKind(path) > 0 and not "talaue" in path.lower():
            return True
        else:
            return False

    def getKind(self, path):
        if "linien/bfg" in path.lower():
            return 1
        elif "linien/land" in path.lower():
            return 2
        elif "/sonstige/" in path.lower():
            return 3
        else:
            return 0

    def createNewFeature(self, featureDef, feat, **args):
        kind  = self.getKind(args['path'])

        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(3)

        newFeat.SetGeometry(geometry)
        newFeat.SetField("name", args['name'])
        newFeat.SetField("kind", kind)
        if self.IsFieldSet(feat, "SECTIE"):
            newFeat.SetField("sectie", feat.GetField("SECTIE"))

        if self.IsFieldSet(feat, "STROVOER"):
            newFeat.SetField("sobek", feat.GetField("STROVOER"))

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        return utils.convertToMultiLine(newFeat)

class HydrBoundaryPoly(HydrBoundary):

    def getTablename(self):
        return TABLE_NAME_POLY

    def getName(self):
        return "%s (Polygons)" % NAME

    def isGeometryValid(self, geomType):
        return geomType == ogr.wkbPolygon or geomType == ogr.wkbMultiPolygon

    def isShapeRelevant(self, name, path):
        shp = ogr.Open(path)
        if self.isGeometryValid(shp.GetLayerByName(name).GetGeomType()) and \
                self.getKind(path) > 0 and not "talaue" in path.lower():
            return True
        else:
            return False

    def createNewFeature(self, featureDef, feat, **args):
        kind  = self.getKind(args['path'])

        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)

        newFeat.SetGeometry(geometry)
        newFeat.SetField("name", args['name'])
        newFeat.SetField("kind", kind)

        if self.IsFieldSet(feat, "SECTIE"):
            newFeat.SetField("sectie", feat.GetField("SECTIE"))

        if self.IsFieldSet(feat, "STROVOER"):
            newFeat.SetField("sobek", feat.GetField("STROVOER"))

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        return utils.convertToMultiPolygon(newFeat)

