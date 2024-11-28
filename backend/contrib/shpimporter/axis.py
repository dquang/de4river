try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer
import utils

NAME="Axis"
TABLE_NAME="river_axes"
PATH="Geodaesie/Flussachse+km"


class Axis(Importer):

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
        return "km.shp" not in path.lower()


    def createNewFeature(self, featureDef, feat, **args):
        newFeat = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)
        newFeat.SetGeometry(geometry)
        newFeat.SetField("name", args['name'])

        if self.IsFieldSet(feat, "river_id"):
            riverId = feat.GetField("river_id")
        else:
            riverId = self.river_id

        newFeat.SetField("river_id", riverId)
        if args.get("name", "").lower() == "achse":
            newFeat.SetField("kind_id", 1) # 1 is Current
        else:
            newFeat.SetField("kind_id", 2) # 2 Is Other

        return utils.convertToMultiLine(newFeat)
