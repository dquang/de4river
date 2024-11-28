try:
    from osgeo import ogr
except ImportError:
    import ogr

from importer import Importer

TABLE_NAME="river_axes_km"
PATH="Geodaesie/Flussachse+km"
NAME="KMS"


class KM(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType == ogr.wkbPoint or geomType == ogr.wkbPoint25D


    def isShapeRelevant(self, name, path):
        return name.lower() == "km"


    def createNewFeature(self, featureDef, feat, **args):
        newFeat = ogr.Feature(featureDef)

        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)
        newFeat.SetGeometry(geometry)

        newFeat.SetField("name", args['name'])

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        if self.IsDoubleFieldSet(feat, "landkm"):
            newFeat.SetField("fedstate_km", feat.GetFieldAsDouble("landkm"))

        if self.IsDoubleFieldSet(feat, "km"):
            newFeat.SetField("km", feat.GetFieldAsDouble("km"))
        elif self.IsDoubleFieldSet(feat, "KM"):
            newFeat.SetField("km", feat.GetFieldAsDouble("KM"))
        else:
            return None

        return newFeat

