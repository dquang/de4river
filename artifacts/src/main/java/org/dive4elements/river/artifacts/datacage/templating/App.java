/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.dive4elements.river.backend.SessionFactoryProvider;

import org.hibernate.Session;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.datacage.Recommendations;

public class App
{
    private static Logger log = LogManager.getLogger(App.class);

    public static final String template =
        System.getProperty("meta.data.template", "meta-data.xml");

    public static final String userId =
        System.getProperty("user.id");

    public static final String PARAMETERS =
        System.getProperty("meta.data.parameters", "");

    public static final String OUTPUT =
        System.getProperty("meta.data.output");

    public static Map<String, Object> getParameters() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String [] parts = PARAMETERS.split("\\s*;\\s*");
        for (String part: parts) {
            String [] kv = part.split("\\s*:\\s*");
            if (kv.length < 2 || (kv[0] = kv[0].trim()).length() == 0) {
                continue;
            }
            String [] values = kv[1].split("\\s*,\\s*");
            map.put(kv[0], values.length == 1 ? values[0] : values);
        }
        return map;
    }

    public static void main(String [] args) {

        Recommendations rec = Recommendations.createRecommendations(
            new File(template));

        if (rec == null) {
            System.err.println("No recommendations created");
            return;
        }

        final Document result = XMLUtils.newDocument();

        final Map<String, Object> parameters = getParameters();

        Session session = SessionFactoryProvider
            .createSessionFactory()
            .openSession();

        try {
            rec.recommend(parameters, userId, result, session);
        }
        finally {
            session.close();
        }

        OutputStream out;

        if (OUTPUT == null) {
            out = System.out;
        }
        else {
            try {
                out = new FileOutputStream(OUTPUT);
            }
            catch (IOException ioe) {
                log.error(ioe);
                return;
            }
        }

        try {
            XMLUtils.toStream(result, out);
        }
        finally {
            if (OUTPUT != null) {
                try {
                    out.close();
                }
                catch (IOException ioe) {
                    log.error(ioe);
                }
            }
        }
        System.exit(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
