try:
    from osgeo import ogr, osr
except ImportError:
    import ogr, osr
import utils
import re
import logging

logger = logging.getLogger("importer")

class Importer:

    def __init__(self, river_id, dbconn, dry_run):
        self.river_id = river_id
        self.dbconn = dbconn
        self.dry_run = dry_run
        self.dest_srs = osr.SpatialReference()
        self.dest_srs.ImportFromEPSG(31467)
        self.handled_fields = []
        self.tracking_import = False
        self.srcLayer = None

    def getKind(self, path):
        raise NotImplementedError("Importer.getKind is abstract!")

    def getPath(self, base):
        raise NotImplementedError("Importer.getPath is abstract!")

    def getTablename(self):
        raise NotImplementedError("Importer.getTablename is abstract!")

    def getName(self):
        raise NotImplementedError("Importer.getName is abstract!")

    def isGeometryValid(self, geomType):
        raise NotImplementedError("Importer.isGeometryValid is abstract!")

    def createNewFeature(self, featureDef, feat, **args):
        raise NotImplementedError("Importer.createNewFeature is abstract!")

    def IsFieldSet(self, feat, name):
        if not name:
            return False
        if feat.GetFieldIndex(name) == -1:
            return False # Avoids an Error in IsFieldSet
        return feat.IsFieldSet(feat.GetFieldIndex(name))

    def searchValue(self, feat, regex):
        """
        Searches for a value that matches regex in all attribute
        fields of a feature.

        @returns the name of the field where a match was found or None
        """
        for val in feat.items():
            if not isinstance(feat.items()[val], basestring):
                continue
            match = re.match(regex, feat.items()[val], re.IGNORECASE)
            if match:
                return val

    def searchField(self, regex):
        """
        Searches for a field in the current src layer that matches
        the expression regex.
        Throws an exception if more than one field matches
        @param feat: The feature to search for attributes
        @param regex: The regex to look for

        @returns: The field name as a string
        """

        if not hasattr(self.srcLayer, "fieldnames"):
            self.srcLayer.fieldnames = []
            for i in range(0, self.srcLayer.GetLayerDefn().GetFieldCount()):
                self.srcLayer.fieldnames.append(
                    self.srcLayer.GetLayerDefn().GetFieldDefn(i).GetNameRef())

        result = None
        for name in self.srcLayer.fieldnames:
            match = re.match(regex, name, re.IGNORECASE)
            if match:
                if result:
                    raise Exception("More than one field matches: %s" % regex)
                else:
                    result = match.group(0)
        return result

    def IsDoubleFieldSet(self, feat, name):
        if not self.IsFieldSet(feat, name):
            return False
        try:
            isset = feat.GetFieldAsDouble(name)
            return isset is not None
        except:
            return False

    def isShapeRelevant(self, name, path):
        return True

    def walkOverShapes(self, shape):
        (name, path) = shape

        shp = ogr.Open(shape[1])
        if shp is None:
            logger.error("Shapefile '%s' could not be opened!" % path)
            return

        if not self.isShapeRelevant(name, path):
            logger.info("Skip shapefile: '%s' of Type: %s" % (path,
                utils.getWkbString(shp.GetLayerByName(name).GetGeomType())))
            return


        logger.info("Processing shapefile '%s'" % path)
        srcLayer = shp.GetLayerByName(name)

        if srcLayer is None:
            logger.error("Layer '%s' was not found!" % name)
            return

        return self.shape2Database(srcLayer, name, path)

    def transform(self, feat):
        geometry = feat.GetGeometryRef()
        src_srs  = geometry.GetSpatialReference()

        if src_srs is None:
            logger.error("No source SRS given! No transformation possible!")
            return feat

        transformer = osr.CoordinateTransformation(src_srs, self.dest_srs)
        if geometry.Transform(transformer):
            return None

        return feat

    def handled(self, field):
        """
        Register a field or a map of as handled during the import.

        There is a warning printed after the import for each unhandled field!
        """
        if not field in self.handled_fields:
            self.handled_fields.append(field)

    def copyFields(self, src, target, mapping):
        """
        Checks the mapping dictonary for key value pairs to
        copy from the source to the destination feature.
        The keys can be reguar expressions that are matched
        agains the source fieldnames

        The Key is the attribute of the source feature to be copied
        into the target attribute named by the dict's value.
        """
        self.tracking_import = True
        for key, value in mapping.items():
            realname = self.searchField(key)
            if realname == None:
                continue
            if not realname in self.handled_fields:
                self.handled_fields.append(realname)
            # 0 OFTInteger, Simple 32bit integer
            # 1 OFTIntegerList, List of 32bit integers
            # 2 OFTReal, Double Precision floating point
            # 3 OFTRealList, List of doubles
            # 4 OFTString, String of ASCII chars
            # 5 OFTStringList, Array of strings
            # 6 OFTWideString, deprecated
            # 7 OFTWideStringList, deprecated
            # 8 OFTBinary, Raw Binary data
            # 9 OFTDate, Date
            # 10 OFTTime, Time
            # 11 OFTDateTime, Date and Time
            if src.IsFieldSet(src.GetFieldIndex(realname)):
                if src.GetFieldType(realname) == 2:
                    target.SetField(value, src.GetFieldAsDouble(realname))
                else:
                    target.SetField(value, utils.getUTF8(src.GetField(realname)))

    def shape2Database(self, srcLayer, name, path):
        destLayer = self.dbconn.GetLayerByName(self.getTablename())

        if srcLayer is None:
            logger.error("Shapefile is None!")
            return -1

        if destLayer is None:
            logger.error("No destination layer given!")
            return -1

        count = srcLayer.GetFeatureCount()
        logger.debug("Try to add %i features to database." % count)

        srcLayer.ResetReading()
        self.srcLayer = srcLayer

        geomType    = -1
        success     = 0
        unsupported = {}
        creationFailed = 0
        featureDef  = destLayer.GetLayerDefn()

        for feat in srcLayer:
            geom     = feat.GetGeometryRef()

            if geom is None:
                logger.debug("Unkown Geometry reference for feature")
                continue

            geomType = geom.GetGeometryType()

            if self.isGeometryValid(geomType):
                newFeat = self.createNewFeature(featureDef,
                                                feat,
                                                name=utils.getUTF8(name),
                                                path=path)

                if newFeat is not None:
                    newFeat.SetField("path", utils.getUTF8Path(path))
                    newFeat = self.transform(newFeat)
                    if newFeat:
                        res = destLayer.CreateFeature(newFeat)
                        if res is None or res > 0:
                            logger.error("Unable to insert feature. Error: %r" % res)
                        else:
                            success = success + 1
                    else:
                        logger.error("Could not transform feature: %s " % feat.GetFID())
                        creationFailed += 1
                else:
                    creationFailed = creationFailed + 1
            else:
                unsupported[utils.getWkbString(geomType)] = \
                        unsupported.get(utils.getWkbString(geomType), 0) + 1

        logger.info("Inserted %i features" % success)
        logger.info("Failed to create %i features" % creationFailed)
        for key, value in unsupported.items():
            logger.info("Found %i unsupported features of type: %s" % (value, key))

        if self.tracking_import:
            unhandled = []
            for i in range(0, srcLayer.GetLayerDefn().GetFieldCount()):
                act_field = srcLayer.GetLayerDefn().GetFieldDefn(i).GetNameRef()
                if not act_field in self.handled_fields:
                    unhandled.append(act_field)

            if len(unhandled):
                logger.info("Did not import values from fields: %s " % \
                        " ".join(unhandled))

        try:
            if self.dry_run:
                return geomType
            destLayer.CommitTransaction()
        except:
            logger.error("Exception while committing transaction.")

        return geomType
