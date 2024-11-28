/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.utils;

import java.util.Map;

import com.smartgwt.client.widgets.form.fields.FormItem;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 *
 * This validator is used for SmartGWT FormItems.
 */
public interface Validator {

    boolean validate(FormItem item, Map errors);
}

