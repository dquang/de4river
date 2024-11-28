try:
    from osgeo import ogr, osr
except ImportError:
    import ogr, osr

from importer import Importer
import logging
logger = logging.getLogger("Fixpoints")
fixpoints_no_km_logged=False

TABLE_NAME="fixpoints"
PATH="Geodaesie/Festpunkte"
NAME="Fixpoints"


class Fixpoint(Importer):

    def getPath(self, base):
        return "%s/%s" % (base, PATH)


    def getTablename(self):
        return TABLE_NAME


    def getName(self):
        return NAME


    def isGeometryValid(self, geomType):
        return geomType == 1


    def isShapeRelevant(self, name, path):
        return True


    def createNewFeature(self, featureDef, feat, **args):
        newFeat  = ogr.Feature(featureDef)

        geometry = feat.GetGeometryRef()
        geometry.SetCoordinateDimension(2)
        newFeat.SetGeometry(geometry)

        newFeat.SetField("name", args['name'])

        if self.IsFieldSet(feat, "river_id"):
            newFeat.SetField("river_id", feat.GetField("river_id"))
        else:
            newFeat.SetField("river_id", self.river_id)

        if self.IsFieldSet(feat, "KM"):
            newFeat.SetField("km", feat.GetFieldAsDouble("KM"))
        elif self.IsFieldSet(feat, "ELBE_KM"):
            newFeat.SetField("km", feat.GetFieldAsDouble("ELBE_KM"))
        else:
            global fixpoints_no_km_logged
            if not fixpoints_no_km_logged:
                logger.error("Could not find KM attribute")
                fixpoints_no_km_logged = True
            return None

        if self.IsFieldSet(feat, "X"):
            newFeat.SetField("x", feat.GetFieldAsDouble("X"))

        if self.IsFieldSet(feat, "Y"):
            newFeat.SetField("y", feat.GetFieldAsDouble("Y"))

        if self.IsFieldSet(feat, "HPGP"):
            newFeat.SetField("HPGP", feat.GetField("HPGP"))
        elif self.IsFieldSet(feat, "ART"):
            newFeat.SetField("HPGP", feat.GetField("ART"))

        return newFeat

