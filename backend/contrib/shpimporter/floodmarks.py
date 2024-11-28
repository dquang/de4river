# -*- coding: utf-8 -*-
try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import logging
import os
import re

TABLE_NAME="flood_marks"
PATH="Hydrologie/HW-Marken"
NAME="Floodmarks"

logger = logging.getLogger(NAME)

class Floodmark(Importer):
    fieldmap = {
            "^station$"       : "km",
            "^km$"            : "km",
            "^wsv-km$"        : "km",
            "^FlussKm$"       : "km",
            "^z$"             : "z",
            "^z\d*"           : "z", # z02, z1890, usw.
            "^m+NHN$"         : "z",
            "^Ort$"           : "location",
            "^Pegel$"         : "location",
        }

    def getPath(self, base):
        return "%s/%s" % (base, PATH)

    def getTablename(self):
        return TABLE_NAME

    def getName(self):
        return NAME

    def isGeometryValid(self, geomType):
        return geomType == ogr.wkbPoint

    def isShapeRelevant(self, name, path):
        return "hw-marken" in name.lower()

    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)
        newFeat.SetGeometry(geometry)

        self.copyFields(feat, newFeat, self.fieldmap)

        newFeat.SetField("river_id", self.river_id)

        filename = os.path.basename(args['path'])

        # Try to extract the year from the filename
        match = re.search(r"([_\-])(\d\d\d\d)([_\-])", filename)
        if match:
            year = match.groups()[1]
            year = int(year)
            newFeat.SetField("year", year)
        else:
            logger.warn(u"Could not extract year from filename: %s " % filename)

        return newFeat

