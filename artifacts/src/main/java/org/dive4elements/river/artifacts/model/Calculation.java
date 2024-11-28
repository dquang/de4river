/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.Serializable;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.Formatter;

/** A calculation that can have result and problems. */
public class Calculation
implements   Serializable
{
    /** Problem of a calculation.
     * Has location and message. */
    public static class Problem
    implements          Serializable
    {
        protected Double    km;
        protected String    msg;
        protected Object [] args;

        public Problem() {
        }

        public Problem(String msg) {
            this.msg = msg;
        }

        public Problem(String msg, Object [] args) {
            this.msg  = msg;
            this.args = args;
        }

        public Problem(double km, String msg) {
            this.km  = km;
            this.msg = msg;
        }

        public Problem(double km, String msg, Object [] args) {
            this.km   = km;
            this.msg  = msg;
            this.args = args;
        }

        public Element toXML(Document document, CallMeta meta) {
            Element problem = document.createElement("problem");
            if (km != null) {
                problem.setAttribute(
                    "km",
                    Formatter.getCalculationKm(meta).format(km));
            }
            String text = args != null
                ? Resources.getMsg(meta, msg, msg, args)
                : Resources.getMsg(meta, msg, msg);
            problem.setTextContent(text);
            return problem;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("problem: ");
            if (km != null) {
                sb.append("km: ").append(km).append(' ');
            }
            sb.append(msg);
            if (args != null) {
                for (Object arg: args) {
                    sb.append(' ').append(arg);
                }
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Problem)) {
                return false;
            }
            Problem o = (Problem)other;
            return !(!msg.equals(o.msg)
                || (km == null && o.km != null)
                || (km != null && o.km == null)
                || (km != null && !km.equals(o.km))
                || !Arrays.equals(args, o.args));
        }
    } // class Problem

    protected List<Problem> problems;

    public Calculation() {
    }

    public Calculation(String msg) {
        addProblem(msg);
    }

    /** New Calculation with error which can be translated given args. */
    public Calculation(String msg, Object ... args) {
        addProblem(msg, args);
    }

    protected List<Problem> checkProblems() {
        if (problems == null) {
            problems = new ArrayList<Problem>();
        }
        return problems;
    }

    public void addProblems(Calculation other) {
        List<Problem> otherProblems = other.problems;
        if (otherProblems != null) {
            List<Problem> problems = checkProblems();
            for (Problem problem: otherProblems) {
                if (!problems.contains(problem)) {
                    problems.add(problem);
                }
            }
        }
    }

    public void addProblem(Problem problem) {
        List<Problem> problems = checkProblems();
        if (!problems.contains(problem)) {
            problems.add(problem);
        }
    }

    public void addProblem(String msg) {
        addProblem(new Problem(msg));
    }

    public void addProblem(String msg, Object ... args) {
        addProblem(new Problem(msg, args));
    }

    public void addProblem(double km, String msg) {
        addProblem(new Problem(km, msg));
    }

    public void addProblem(double km, String msg, Object ... args) {
        addProblem(new Problem(km, msg, args));
    }

    public boolean hasProblems() {
        return problems != null && !problems.isEmpty();
    }

    public int numProblems() {
        return problems != null ? problems.size() : 0;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public String problemsToString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0, N = problems.size(); i < N; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(problems.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    public void toXML(Document document, CallMeta meta) {

        Element root = document.createElement("problems");

        if (hasProblems()) {
            for (Problem problem: problems) {
                root.appendChild(problem.toXML(document, meta));
            }
        }

        document.appendChild(root);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
