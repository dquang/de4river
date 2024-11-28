# -*- coding: utf-8 -*-
import os

try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import utils

import logging
logger = logging.getLogger("Jetties")

PATH="Geodaesie/Bauwerke"
NAME="Jetties"

# strings need to be lowercase
# buhnenkopf 0
# buhnenfuß 1
# buhnenwurzel 2
JETTY_KIND = {
        "bkl" : 0,
        "bkr" : 0,
        "bk"  : 0,
        "bfl" : 1,
        "bfr" : 1,
        "bf"  : 1,
        "bwl" : 2,
        "bwr" : 2,
        "bw"  : 2,
    }

class Jetties(Importer):
    fieldmap = {
            "^station$"       : "km",
            "^km$"            : "km",
            "^wsv-km$"        : "km",
            "^z$"             : "z",
            "^H[oeö]{0,2}he$" : "z",
            "^m+NHN$"         : "z",
        }

    def getPath(self, base):
        return "%s/%s" % (base, PATH)

    def getTablename(self):
        return "jetties"

    def getName(self):
        return "JETTIES"

    def isGeometryValid(self, geomType):
        return geomType == ogr.wkbPoint or geomType == ogr.wkbPoint25D

    def isShapeRelevant(self, name, path):
        if not path.endswith("Buhnen.shp"):
            return False
        shp = ogr.Open(path)
        return self.isGeometryValid(shp.GetLayerByName(name).GetGeomType())

    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)

        self.copyFields(feat, newFeat, self.fieldmap)

        newFeat.SetGeometry(geometry)

        newFeat.SetField("river_id", self.river_id)

        artname = self.searchField("^type$")
        if self.IsFieldSet(feat, artname):
            self.handled(artname)
            kind_id = JETTY_KIND.get(feat.GetField(artname).lower())
            if kind_id == None:
                logger.warn("Unknown Type: %s" % \
                        feat.GetField(artname))
            else:
                newFeat.SetField("kind_id", kind_id)

        return newFeat

