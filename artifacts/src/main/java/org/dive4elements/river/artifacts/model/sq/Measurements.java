/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Measurements
{
    private static final Logger log = LogManager.getLogger(Measurements.class);

    public interface SExtractor {
        double getS(Measurement measument);
    } // interface SExtractor

    public static final SExtractor S_SF_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_SF();
        }
    };

    public static final SExtractor S_SS_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_SS();
        }
    };

    public static final SExtractor S_BL_S_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_BL_S();
        }
    };

    public static final SExtractor S_BL_FG_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_BL_FG();
        }
    };

    public static final SExtractor S_BL_CG_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_BL_CG();
        }
    };

    public static final SExtractor S_BL_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_BL_1();
        }
    };

    public static final SExtractor S_BL2_EXTRACTOR = new SExtractor() {
        @Override
        public double getS(Measurement measument) {
            return measument.S_BL_2();
        }
    };

    protected List<Measurement> measuments;
    protected List<Measurement> accumulated;

    protected SQ.Factory sqFactory;

    public Measurements() {
    }

    public Measurements(
        List<Measurement> measuments,
        List<Measurement> accumulated,
        SQ.Factory        sqFactory
    ) {
        this.sqFactory = sqFactory;
        if (log.isDebugEnabled()) {
            log.debug("number of measuments: " + measuments.size());
            log.debug("number of accumulated: " + accumulated.size());
        }
        this.measuments = measuments;
        this.accumulated = accumulated;
    }

    public List<SQ> extractSQ(
        List<Measurement> measuments,
        SExtractor extractor
    ) {
        List<SQ> result = new ArrayList<SQ>(measuments.size());
        int invalid = 0;
        for (Measurement measument: measuments) {
            SQ sq = sqFactory.createSQ(extractor.getS(measument), measument.Q(),
                    measument.getDate());
            if (sq.isValid()) {
                result.add(sq);
            }
            else {
                ++invalid;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed num invalid: " + invalid
                + " of " + measuments.size());
        }
        return result;
    }

    public List<SQ> S_SF() {
        return extractSQ(measuments, S_SF_EXTRACTOR);
    }

    public List<SQ> S_SS() {
        return extractSQ(measuments, S_SS_EXTRACTOR);
    }

    public List<SQ> S_BL_S() {
        return extractSQ(accumulated, S_BL_S_EXTRACTOR);
    }

    public List<SQ> S_BL_FG() {
        return extractSQ(accumulated, S_BL_FG_EXTRACTOR);
    }

    public List<SQ> S_BL_CG() {
        return extractSQ(accumulated, S_BL_CG_EXTRACTOR);
    }

    public List<SQ> S_BL() {
        return extractSQ(accumulated, S_BL_EXTRACTOR);
    }

    public List<SQ> S_BL2() {
        return extractSQ(accumulated, S_BL2_EXTRACTOR);
    }

    public List<SQ> getSQs(int index) {
        switch (index) {
            case 0: return S_SF();
            case 1: return S_SS();
            case 2: return S_BL_S();
            case 3: return S_BL_FG();
            case 4: return S_BL_CG();
            case 5: return S_BL();
            case 6: return S_BL2();
        }
        log.error("THIS SHOULD NOT HAPPEN: Tried to access SQ[" + index + "]");
        return new ArrayList<SQ>(0);
    }

    /**
     * Gets the accumulated for this instance.
     *
     * @return The accumulated.
     */
    public List<Measurement> getAccumulated() {
        return this.accumulated;
    }

    /**
     * Sets the accumulated for this instance.
     *
     * @param accumulated The accumulated.
     */
    public void setAccumulated(List<Measurement> accumulated) {
        this.accumulated = accumulated;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Measurements [");
        for (int i = 0, M = measuments.size(); i < M; ++i) {
            if (i > 0) sb.append(", ");
            sb.append(measuments.get(i));
        }
        return sb.append(']').toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
