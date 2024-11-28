# -*- coding: utf-8 -*-
try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import utils

import logging
logger = logging.getLogger("HWS")

PATH="Hydrologie/HW-Schutzanlagen"
NAME="HWS"

# Keep in sync with hws_kinds table:
# strings need to be lowercase
HWS_KIND = {
        "durchlass" : 1,
        "damm" : 2,
        "deich" : 2,
        "hochufer" : 2,
        "graben" : 3,
        "rohr1" : 1,
        "rohr 1" : 1,
        "rohr 2" : 1,
        "hauptdeich" : 2,
        "sommerdeich" : 2
    }

# Keep in sync with fed_states table:
# strings need to be lowercase
FED_STATES = {
    "bayern" : 1,
    "hessen" : 2,
    "niedersachsen" : 3,
    "nordrhein-westfalen" : 4,
    "nordrhein westfalen" : 4,
    "rheinland-pfalz" : 5,
    "rheinland pfalz" : 5,
    "saarland" : 6,
    "schleswig-holstein" : 7,
    "schleswig holstein" : 7,
    "brandenburg" : 8,
    "mecklenburg-vorpommern" : 9,
    "mecklenburg vorpommern" : 9,
    "thüringen" : 10,
    "baden-württemberg" : 11,
    "baden württemberg" : 11,
    "sachsen-anhalt" : 12,
    "sachsen anhalt" : 12,
    "sachsen" : 13,
    "berlin" : 14,
    "bremen" : 15,
    "hamburg" : 16,
}

class HWSPoints(Importer):
    fieldmap = {
            "name$" : "name",
            "quelle$" : "source",
            "anmerkung$" : "description",
            "stand$" : "status_date",
            "verband$" : "agency",
            "Deich_{0,1}KM$" : "dike_km",
            "Bereich$" : "range",
            "H[oeö]{0,2}he_{0,1}SOLL$" : "z_target",
            "(WSP_){0,1}BfG_{0,1}100$" : "rated_level",
            "H[oeö]{0,2}he_{0,1}IST$" : "z",
        }

    printedforpath=[]

    def getPath(self, base):
        return "%s/%s" % (base, PATH)

    def getTablename(self):
        return "hws_points"

    def getName(self):
        return "HWS_POINTS"

    def isGeometryValid(self, geomType):
        return geomType == ogr.wkbPoint or geomType == ogr.wkbPoint25D

    def isShapeRelevant(self, name, path):
        shp = ogr.Open(path)
        return self.isGeometryValid(shp.GetLayerByName(name).GetGeomType())

    def getFedStateIDfromPath(self, path):
        """
        Tries to get extract a bundesland from the path
        """
        for state in sorted(FED_STATES.keys(), key = len, reverse = True):
            if state in path.lower():
                if not path in self.printedforpath:
                    logger.info("Extracted federal state from path: %s" % state)
                    self.printedforpath.append(path)
                return FED_STATES[state]

    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)

        self.copyFields(feat, newFeat, self.fieldmap)

        newFeat.SetGeometry(geometry)

        artname = self.searchField("art$")
        if self.IsFieldSet(feat, artname):
            self.handled(artname)
            kind_id = HWS_KIND.get(feat.GetField(artname).lower())
            if not kind_id:
                logger.warn("Unknown Art: %s" % \
                        feat.GetField(artname))
                newFeat.SetField("kind_id", 2)
            else:
                newFeat.SetField("kind_id", kind_id)
        else:
            newFeat.SetField("kind_id", 2)

        fname = self.searchField("Bundesland$")
        if self.IsFieldSet(feat, fname):
            self.handled(fname)
            fed_id = FED_STATES.get(feat.GetField(fname).lower())

            if not fed_id:
                logger.warn("Unknown Bundesland: %s" % \
                        feat.GetField(fname))
            else:
                newFeat.SetField("fed_state_id", fed_id)
        else:
            # Try to get the bundesland from path
            fed_id = self.getFedStateIDfromPath(args['path'])
            if fed_id:
                newFeat.SetField("fed_state_id", fed_id)

        fname = self.searchField("(ufer$)|(flussseite$)")
        if self.IsFieldSet(feat, fname):
            self.handled(fname)
            shoreString = feat.GetField(fname)
            if "links" in shoreString.lower():
                newFeat.SetField("shore_side", True)
            elif "rechts" in shoreString.lower():
                newFeat.SetField("shore_side", False)


        fname = self.searchField("river_{0,1}id$")
        if self.IsFieldSet(feat, fname):
            self.handled(fname)
            if feat.GetField(fname) != self.river_id:
                logger.warn("River_id mismatch between shapefile and"
                     " importer parameter.")
            newFeat.SetField("river_id", feat.GetField(fname))
        else:
            newFeat.SetField("river_id", self.river_id)

        fname = self.searchField("name$")
        if not self.IsFieldSet(feat, fname):
            newFeat.SetField("name", args['name'])

        official = False
        fname = self.searchField("offiziell$")
        if self.IsFieldSet(feat, fname):
            self.handled(fname)
            offiziell = feat.GetField(fname)
            if offiziell == "1" or offiziell == 1:
                official = True
        # Set the official value based on the file name as a fallback
        elif args.get("name", "").lower() == "rohre_und_sperren" or \
                args.get("name", "").lower() == "rohre-und-sperren":
             official = True
        newFeat.SetField("official", official)

        if self.IsFieldSet(newFeat, "z") and \
            self.IsFieldSet(newFeat, "rated_level"):
            fname = self.searchField("freibord(_m){0,1}$")
            self.handled(fname)
            z = newFeat.GetFieldAsDouble("z")
            rl = newFeat.GetFieldAsDouble("rated_level")
            newFeat.SetField("freeboard", z - rl)

        return newFeat

class HWSLines(HWSPoints):

    # TODO: GEOM_target, GEOM_rated_level, dike_km_from, dike_km_to
    fieldmap = {
            "name$" : "name",
            "quelle$" : "source",
            "anmerkung$" : "description",
            "stand$" : "status_date",
            "verband$" : "agency",
            "Bereich$" : "range",
        }

    def getPath(self, base):
        return "%s/%s" % (base, PATH)

    def getTablename(self):
        return "hws_lines"

    def getName(self):
        return "HWS_LINES"

    def isGeometryValid(self, geomType):
        return geomType in [ogr.wkbLineString,
                            ogr.wkbLineString25D,
                            ogr.wkbMultiLineString25D,
                            ogr.wkbMultiLineString]

    def isShapeRelevant(self, name, path):
        shp = ogr.Open(path)
        return self.isGeometryValid(shp.GetLayerByName(name).GetGeomType())

    def createNewFeature(self, featureDef, feat, **args):
        newFeat = HWSPoints.createNewFeature(self, featureDef, feat, **args)
        geometry = feat.GetGeometryRef()
        if geometry.GetCoordinateDimension() == 2:
            geometry.SetCoordinateDimension(3)
            for i in range(0, geometry.GetPointCount()):
                x,y,z = geometry.GetPoint(i)
                z = 9999
                geometry.SetPoint(i, x, y, z)
        newFeat.SetGeometry(geometry)

        return utils.convertToMultiLine(newFeat)


