# -*- coding: utf-8 -*-
try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer

TABLE_NAME="buildings"
PATH="Geodaesie/Bauwerke"
NAME="Buildings"

BUILDING_KINDS= {
        "sonstige" : 0,
        "brücken"  : 1,
        "wehre"    : 2,
        "pegel"    : 3,
        }

class Building(Importer):
    fieldmap = {
            "^station$"       : "km",
            "^km$"            : "km",
            "^wsv-km$"        : "km",
            "^z$"             : "z",
            "^H[oeö]{0,2}he$" : "z",
            "^m+NHN$"         : "z",
            "^KWNAAM$"        : "description",
            "^Name$"          : "description"
        }

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType == 2


    def isShapeRelevant(self, name, path):
        return "buhnen.shp" not in path.lower()

    def getKind(self, feat, path):
        # First try to resolve it with the filename
        for fname in ["brücke.shp", "bruecke.shp",
                     "brücken.shp", "bruecken.shp"]:
            if path.lower().endswith(fname):
                return BUILDING_KINDS["brücken"]
        for fname in ["wehr.shp", "wehre.shp"]:
            if path.lower().endswith(fname):
                return BUILDING_KINDS["wehre"]
        for fname in ["pegel.shp"]:
            if path.lower().endswith(fname):
                return BUILDING_KINDS["pegel"]

        # Now it gets ugly when we search all attributes
        ret = self.searchValue(feat, "^br[ueü]{0,2}cke[n]{0,1}$")
        if ret:
            self.handled(ret)
            return BUILDING_KINDS["brücken"]
        ret = self.searchValue(feat, "^wehr[e]{0,1}$")
        if ret:
            self.handled(ret)
            return BUILDING_KINDS["wehre"]

        return BUILDING_KINDS["sonstige"]


    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)
        newFeat.SetGeometry(geometry)

        self.copyFields(feat, newFeat, self.fieldmap)

        newFeat.SetField("kind_id", self.getKind(feat, args['path']))
        newFeat.SetField("name", args["name"])

        newFeat.SetField("river_id", self.river_id)

        return newFeat

