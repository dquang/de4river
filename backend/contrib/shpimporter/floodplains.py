try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer

TABLE_NAME="floodplain"
PATH="Hydrologie/Hydr.Grenzen"
NAME="Floodplains"


class Floodplain(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType == 3 or geomType == 6


    def isShapeRelevant(self, name, path):
        return name.lower().find("talaue") >= 0


    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)
        geometry = feat.GetGeometryRef()

        newFeat.SetGeometry(geometry)
        newFeat.SetField("name", args['name'])

        if args['path'].lower().endswith("/talaue.shp") and \
                not "sonstige" in args['path'].lower():
            newFeat.SetField("kind_id", 1) # offical
        else:
            newFeat.SetField("kind_id", 0) # misc

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        return newFeat

