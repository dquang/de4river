/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.auth.plain;

import org.dive4elements.river.client.server.auth.AuthenticationException;
import org.dive4elements.river.client.server.auth.DefaultUser;
import org.dive4elements.river.client.server.auth.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.server.features.Features;

/**
 * Authenticator that uses a local file as user backend.
 */
public class Authenticator
implements   org.dive4elements.river.client.server.auth.Authenticator
{
    private static final Logger log =
        LogManager.getLogger(Authenticator.class);

    public static class Authentication
    implements org.dive4elements.river.client.server.auth.Authentication
    {
        protected String       user;
        protected String       password;
        protected List<String> roles;
        protected Features     features;

        public Authentication(
            String       user,
            String       password,
            List<String> roles,
            Features features
        ) {
            this.user     = user;
            this.password = password;
            this.roles    = roles;
            this.features = features;
        }

        @Override
        public boolean isSuccess() {
            return user != null;
        }

        @Override
        public User getUser() {
            return isSuccess()
                ? new DefaultUser(
                    user, password, null, false, roles,
                    this.features.getFeatures(roles))
                : null;
        }
    } // class Authentication

    public Authenticator() {
    }

    private static File credentialsFile() {
        String env = System.getenv("FLYS_USER_FILE");
        if (env == null) {
            env = System.getProperty(
                "flys.user.file",
                System.getProperty("user.home", ".")
                + System.getProperty("file.separator")
                + "flys_user_file");
        }
        log.debug("Using credentials file " + env);
        return new File(env);

    }

    @Override
    public org.dive4elements.river.client.server.auth.Authentication auth(
        String username,
        String password,
        String encoding,
        Features features,
        ServletContext context
    )
    throws AuthenticationException, IOException
    {
        File file = credentialsFile();
        if (!file.canRead() || !file.isFile()) {
            log.error("cannot find user file '" + file + "'");
            return new Authentication(
                null, null, new ArrayList<String>(0), features);
        }

        BufferedReader reader =
            new BufferedReader(
            new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).length() == 0
                || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                if (parts[0].equals(username)) {
                    log.debug("user '" + username + "' found.");
                    if (parts[1].equals(password)) {
                        List<String> roles =
                            new ArrayList<String>(parts.length - 2);

                        for (int i = 2; i < parts.length; i++) {
                            roles.add(parts[i]);
                        }

                        log.debug("success");
                        return new Authentication(
                            username, password, roles, features);
                    }
                    // Stop: user found, wrong password
                    break;
                }
            }
        }
        finally {
            reader.close();
        }
        log.debug("failed");
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
