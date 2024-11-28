/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.FixRealizingAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.fixings.FixOutlierFacet;
import org.dive4elements.river.artifacts.model.fixings.FixRealizingCalculation;
import org.dive4elements.river.artifacts.model.fixings.FixRealizingResult;
import org.dive4elements.river.artifacts.model.fixings.FixReferenceEventsFacet;
import org.dive4elements.river.artifacts.model.fixings.FixWQCurveFacet;
import org.dive4elements.river.artifacts.model.fixings.FixWaterlevelFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.UniqueDateFormatter;

/**
 * State to compute the fixation realizing (vollmer) results.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixRealizingCompute extends DefaultState implements FacetTypes {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(FixRealizingCompute.class);

    public static final String I18N_WQ_CURVE = "fix.vollmer.wq.curve";

    public static final String I18N_WQ_OUTLIER = "fix.vollmer.wq.outliers";

    /**
     * The default constructor that initializes an empty State object.
     */
    public FixRealizingCompute() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("FixRealizingCompute.computeAdvance");

        CalculationResult res;

        FixRealizingAccess access = new FixRealizingAccess(artifact);

        if (old instanceof CalculationResult) {
            res = (CalculationResult) old;
        }
        else {
            FixRealizingCalculation calc = new FixRealizingCalculation(access);
            res = calc.calculate();
        }

        if (facets == null) {
            return res;
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet());
        }

        String   id   = getID();
        CallMeta meta = context.getMeta();

        FixRealizingResult fixRes = (FixRealizingResult) res.getData();
        WQKms [] wqkms = fixRes != null ? fixRes.getWQKms() : new WQKms[0];

        for (int i = 0; i < wqkms.length; i++) {
            String nameW = null;
            String nameQ = null;

            if (access.isQ()) {
                nameQ = wqkms[i].getName();
                nameW = "W(" + nameQ + ")";
            }
            else {
                nameW = wqkms[i].getName();
                nameQ = "Q(" + nameW + ")";
            }

            Facet wq = new FixWaterlevelFacet(
                i, FIX_WQ_LS, nameW, ComputeType.ADVANCE, hash, id);

            Facet w = new FixWaterlevelFacet(
                i, LONGITUDINAL_W, nameW, ComputeType.ADVANCE, hash, id);

            Facet q = new FixWaterlevelFacet(
                i, LONGITUDINAL_Q, nameQ, ComputeType.ADVANCE, hash, id);
            Facet csFacet = new CrossSectionWaterLineFacet(i, nameW);

            facets.add(wq);
            facets.add(w);
            facets.add(q);
            facets.add(csFacet);

            // XXX: THIS CAN NOT HAPPEN! REMOVE IT!
            if (wqkms[i] instanceof WQCKms) {
                String nameC = nameW.replace(
                    "benutzerdefiniert",
                    "benutzerdefiniert [korrigiert]");

                Facet c = new FixWaterlevelFacet(
                    i, DISCHARGE_LONGITUDINAL_C, nameC);

                facets.add(c);
            }
        }

        if (wqkms.length > 0) {
            DateFormat df = Formatter.getDateFormatter(context.getMeta(),
                "dd.MM.yyyy");
            DateFormat lf = Formatter.getDateFormatter(context.getMeta(),
                "dd.MM.yyyy'T'HH:mm");

            Collection<Date> reds = fixRes.getReferenceEventsDates();
            UniqueDateFormatter cf = new UniqueDateFormatter(df, lf, reds);

            int i = 0;
            for (Date d: reds) {
                facets.add(new FixReferenceEventsFacet(
                        (1 << 9) | i,
                    FIX_EVENTS,
                    cf.format(d)));
                i++;
            }

            facets.add(
                new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));

            facets.add(
                new DataFacet(WST, "WST data", ComputeType.ADVANCE, hash, id));

            facets.add(
                new DataFacet(PDF, "PDF data", ComputeType.ADVANCE, hash, id));
        }

        facets.add(
            new DataFacet(AT, "AT data", ComputeType.ADVANCE, hash, id));

        facets.add(new FixWQCurveFacet(
            0,
            Resources.getMsg(meta, I18N_WQ_CURVE, I18N_WQ_CURVE) + " ("
                + access.getFunction() + ")"));

        if (access.getPreprocessing()) {
            facets.add(new FixOutlierFacet(
                0,
                FIX_OUTLIER,
                Resources.getMsg(meta, I18N_WQ_OUTLIER, I18N_WQ_OUTLIER)));
        }

        return res;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
