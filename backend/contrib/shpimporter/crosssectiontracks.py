try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer

TABLE_NAME="cross_section_tracks"
PATH="Geodaesie/Querprofile"
NAME="Crosssections"


class CrosssectionTrack(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType == 2


    def isShapeRelevant(self, name, path):
        return True


    def createNewFeature(self, featureDef, feat, **args):
        newFeat = ogr.Feature(featureDef)
        newFeat.SetGeometry(feat.GetGeometryRef())
        newFeat.SetField("name", args['name'])

        if args['path'].lower().endswith("/qps.shp") and \
                not "sonstige" in args['path'].lower():
            newFeat.SetField("kind_id", 1) # offical
        else:
            newFeat.SetField("kind_id", 0) # misc

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        if self.IsFieldSet(feat, "KILOMETER"):
            newFeat.SetField("km", feat.GetFieldAsDouble("KILOMETER"))
        elif self.IsFieldSet(feat, "KM"):
            newFeat.SetField("km", feat.GetFieldAsDouble("KM"))
        elif self.IsFieldSet(feat, "STATION"):
            newFeat.SetField("km", feat.GetFieldAsDouble("STATION"))
        else:
            return None

        if self.IsFieldSet(feat, "ELEVATION"):
            newFeat.SetField("z", feat.GetFieldAsDouble("ELEVATION"))
        else:
            newFeat.SetField("z", 0)

        return newFeat

