try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import os.path
import utils

TABLE_NAME="floodmaps"
PATH="Hydrologie/UeSG"
NAME="UESG"


class UESG(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType in [ogr.wkbMultiPolygon,
                            ogr.wkbPolygon]

    def getKind(self, path):
        kind = 0
        if path.find("Berechnung") > 0:
            kind = kind + 100

            if path.find("Aktuell") > 0:
                kind = kind + 10
            else:
                kind = kind + 20

            if path.find("Bundesl") > 0:
                kind = kind + 2
            else:
                kind = kind + 1
        else:
            kind = kind + 200

        return kind


    def createNewFeature(self, featureDef, feat, **args):
        kind  = self.getKind(args['path'])
        newFeat = ogr.Feature(featureDef)
        newFeat.SetGeometry(feat.GetGeometryRef())

        if self.IsFieldSet(feat, "river_id"):
            riverId = feat.GetField(feat)
        else:
            riverId = self.river_id

        if self.IsFieldSet(feat, "diff"):
            diff = feat.GetFieldAsDouble("diff")
        else:
            diff = 0

        if self.IsFieldSet(feat, "count"):
            count = feat.GetFieldAsInteger("count")
        else:
            count = 0

        if self.IsFieldSet(feat, "area"):
            area = feat.GetFieldAsDouble("area")
        else:
            area = 0

        if self.IsFieldSet(feat, "perimeter"):
            perimeter = feat.GetFieldAsDouble("perimeter")
        else:
            perimeter = 0

        if self.IsFieldSet(feat, "GEWAESSER"):
            newFeat.SetField("waterbody", feat.GetField("GEWAESSER"))

        if kind >= 200:
            newFeat.SetField("source",
                    os.path.basename(os.path.dirname(args['path'])))


        groupId = 2

        newFeat.SetField("river_id", riverId)
        newFeat.SetField("diff", diff)
        newFeat.SetField("count", count)
        newFeat.SetField("area", area)
        newFeat.SetField("perimeter", perimeter)
        newFeat.SetField("kind", kind)
        newFeat.SetField("name", args['name'])

        return utils.convertToMultiPolygon(newFeat)

