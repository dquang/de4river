/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.sql.Date;
import java.sql.Timestamp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TypeConverter
{
    private static Logger log = LogManager.getLogger(TypeConverter.class);

    private TypeConverter() {
    }

    public static Object convert(Object object, String type) {

        if (type == null) {
            return object;
        }

        type = type.toLowerCase();

        if ("integer".equals(type)) {
            return Integer.valueOf(object.toString());
        }

        if ("double".equals(type)) {
            return Double.valueOf(object.toString());
        }

        if ("string".equals(type)) {
            return object.toString();
        }

        if ("date".equals(type)) {
            if (object instanceof Date) {
                return object;
            }
            try {
                return new Date((long)Double.parseDouble(object.toString()));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe);
                return null;
            }
        }

        if ("timestamp".equals(type)) {
            if (object instanceof Timestamp) {
                return object;
            }
            try {
                return new Timestamp(
                    (long)Double.parseDouble(object.toString()));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe);
                return null;
            }
        }

        // TODO: Add more types

        return object;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
