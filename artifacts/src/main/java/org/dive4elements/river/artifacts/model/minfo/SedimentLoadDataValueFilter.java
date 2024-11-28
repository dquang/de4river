/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.dive4elements.river.backend.utils.DateUtil;

import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Value;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Value.Filter;

public final class SedimentLoadDataValueFilter {

    private SedimentLoadDataValueFilter() {
    }

    public static final class Not implements Filter {

        private Filter parent;

        public Not(Filter parent) {
            this.parent = parent;
        }

        @Override
        public boolean accept(Value value) {
            return !parent.accept(value);
        }
    } // class Not

    public static abstract class Composite implements Filter {
        protected List<Filter> filters;

        public Composite() {
            filters = new ArrayList<Filter>();
        }

        public Composite(Filter filter) {
            this();
            add(filter);
        }

        public Composite add(Filter filter) {
            filters.add(filter);
            return this;
        }
    }

    public static final class And extends Composite {

        public And() {
        }

        public And(Filter filter) {
            super(filter);
        }

        @Override
        public boolean accept(Value value) {
            for (Filter filter: filters) {
                if (!filter.accept(value)) {
                    return false;
                }
            }
            return true;
        }
    } // class And

    public static final class Or extends Composite {

        public Or() {
        }

        public Or(Composite filter) {
            super(filter);
        }

        @Override
        public boolean accept(Value value) {
            for (Filter filter: filters) {
                if (filter.accept(value)) {
                    return true;
                }
            }
            return false;
        }
    } // class Or

    public static final class Year implements Filter {

        private int year;

        public Year(int year) {
            this.year = year;
        }

        @Override
        public boolean accept(Value value) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(value.getLoad().getStartTime());
            return cal.get(Calendar.YEAR) == year;
        }
    } // class Year

    public static final class SQTimeInterval implements Filter {

        private Integer sqTiId;

        public SQTimeInterval(Integer sqTiId) {
            this.sqTiId = sqTiId;
        }

        @Override
        public boolean accept(Value value) {
            if (sqTiId == null) {
                /* Nothing set, nothing filtered */
                return true;
            }
            if (value.getLoad().getSQRelationTimeIntervalId() == null) {
                /* Loads without sqRelationTimeInterval are "Schwebstoffe"
                 * and should be included. */
                return true;
            }
            /* All other values should be filtered accordingly. */
            return value.getLoad().getSQRelationTimeIntervalId()
                .equals(sqTiId);
        }
    } // class SQTimeInterval

    public static final class IsEpoch implements Filter {

        public static final IsEpoch INSTANCE = new IsEpoch();

        private IsEpoch() {
        }

        @Override
        public boolean accept(Value value) {
            return value.getLoad().isEpoch();
        }
    } // class Year

    public static final class TimeRangeIntersects implements Filter {

        private Date a;
        private Date b;

        public TimeRangeIntersects(int year) {
            this(year, year);
        }

        public TimeRangeIntersects(int startYear, int endYear) {
            this(DateUtil.getStartDateFromYear(Math.min(startYear, endYear)),
                 DateUtil.getEndDateFromYear(Math.max(startYear, endYear)));
        }

        public TimeRangeIntersects(Date a, Date b) {
            if (a.after(b)) {
                this.b = a;
                this.a = b;
            } else {
                this.a = a;
                this.b = b;
            }
        }

        @Override
        public boolean accept(Value value) {
            Date c = value.getLoad().getStartTime();
            Date d = value.getLoad().getStopTime();
            return d == null
                ? c.compareTo(a) >= 0 && c.compareTo(b) <= 0
                : !(a.after(d) || c.after(b));
        }
    } // class TimeRangeIntersects

    public static final class IsOfficial implements Filter {

        public static final IsOfficial INSTANCE = new IsOfficial();

        private IsOfficial() {
        }

        @Override
        public boolean accept(Value value) {
            return value.getLoad().getKind() == 1;
        }
    } // class IsOfficial
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

