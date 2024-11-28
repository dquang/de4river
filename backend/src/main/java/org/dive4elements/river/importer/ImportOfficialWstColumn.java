/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

public class ImportOfficialWstColumn
extends      ImportWstColumn
{
    public static final ImportWst.ImportWstColumnFactory COLUMN_FACTORY =
        new ImportWst.ImportWstColumnFactory() {
            @Override
            public ImportWstColumn create(ImportWst importWst, int position) {
                return new ImportOfficialWstColumn(
                    importWst, null, null, position);
            }
        };

    protected ImportOfficialLine officialLine;

    public ImportOfficialWstColumn() {
        super();
    }

    public ImportOfficialWstColumn(
        ImportWst wst,
        String    name,
        String    description,
        Integer   position
    ) {
        super(wst, name, description, position);
    }

    public ImportOfficialLine getOfficialLine() {
        return officialLine;
    }

    public void setOfficialLine(ImportOfficialLine officialLine) {
        this.officialLine = officialLine;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
