/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river;

import org.dive4elements.river.backend.SessionFactoryProvider;
import org.dive4elements.river.backend.FLYSCredentials;

import org.hibernate.cfg.Configuration;

import org.hibernate.dialect.resolver.DialectFactory;

public class App
{
    public static void dumpSchema(Configuration cfg) {
        System.out.println("BEGIN;");

        String [] setupScript = cfg.generateSchemaCreationScript(
            DialectFactory.constructDialect(
                FLYSCredentials.getDefault().getDialect()));

        for (String line: setupScript) {
            System.out.println(line + ";");
        }

        System.out.println("COMMIT;");
    }

    public static void main(String [] args) {
        dumpSchema(SessionFactoryProvider.createConfiguration());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
