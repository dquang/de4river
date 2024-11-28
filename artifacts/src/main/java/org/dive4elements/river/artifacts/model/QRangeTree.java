/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class QRangeTree
implements   Serializable
{
    private static Logger log = LogManager.getLogger(QRangeTree.class);

    public static final double EPSILON = 1e-4;

    public static class Node
    implements          Serializable
    {
        Node left;
        Node right;
        Node prev;
        Node next;

        double a;
        double b;
        double q;

        public Node() {
        }

        public Node(double a, double b, double q) {
            this.a = a;
            this.b = b;
            this.q = q;
        }

        protected final double interpolatePrev(double pos) {
            /*
            f(prev.b) = prev.q
            f(a)      = q

            prev.q = m*prev.b + n
            q      = m*a      + n <=> n = q - m*a

            q - prev.q = m*(a - prev.b)

            m = (q - prev.q)/(a - prev.b) # a != prev.b
            */

            if (a == prev.b) {
                return 0.5*(q + prev.q);
            }
            double m = (q - prev.q)/(a - prev.b);
            double n = q - m*a;
            return m*pos + n;
        }

        protected final double interpolateNext(double pos) {
            /*
            f(next.a) = next.q
            f(b)      = q

            next.q = m*next.a + n
            q      = m*b      + n <=> n = q - m*b

            q - next.q = m*(b - next.a)
            m = (q - next.q)/(b - next.a) # b != next.a
            */

            if (b == next.a) {
                return 0.5*(q + next.q);
            }
            double m = (q - next.q)/(b - next.a);
            double n = q - m*b;
            return m*pos + n;
        }

        /** @param pos the station (km). */
        public double findQ(double pos) {

            Node current = this;
            for (;;) {
                if (pos < current.a) {
                    if (current.left != null) {
                        current = current.left;
                        continue;
                    }
                    return current.prev != null
                        ? current.interpolatePrev(pos)
                        : Double.NaN;
                }
                if (pos > current.b) {
                    if (current.right != null) {
                        current = current.right;
                        continue;
                    }
                    return current.next != null
                        ? current.interpolateNext(pos)
                        : Double.NaN;
                }
                return current.q;
            }
        }

        public Node findNode(double pos) {
            Node current = this;
            while (current != null) {
                if (pos < current.a) {
                    current = current.left;
                }
                else if (pos > current.b) {
                    current = current.right;
                }
                return current;
            }
            return null;
        }

        public boolean contains(double c) {
            return c >= a && c <= b;
        }
    } // class Node

    /** Class to cache the last found tree leaf in a search for Q.
     *  Its likely that a neighbored pos search
     *  results in using the same leaf node. So
     *  caching this leaf will minimize expensive
     *  tree traversals.
     *  Modeled as inner class because the QRangeTree
     *  itself is a shared data structure.
     *  Using this class omits interpolation between
     *  leaves.
     */
    public final class QuickQFinder {

        private Node last;

        public QuickQFinder() {
        }

        public double findQ(double pos) {
            if (last != null && last.contains(pos)) {
                return last.q;
            }
            last = QRangeTree.this.findNode(pos);
            return last != null ? last.q : Double.NaN;
        }

        public double [] findQs(double [] kms, Calculation report) {
            return findQs(kms, new double[kms.length], report);
        }

        public double [] findQs(
            double []    kms,
            double []   qs,
            Calculation report
        ) {
            for (int i = 0; i < kms.length; ++i) {
                if (Double.isNaN(qs[i] = findQ(kms[i]))) {
                    report.addProblem(kms[i], "cannot.find.q");
                }
            }
            return qs;
        }
    } // class QuickQFinder

    protected Node root;

    public QRangeTree() {
    }

    public static final class AccessQAB {
        private int startIndex;

        public AccessQAB(int startIndex) {
            this.startIndex = startIndex;
        }

        public Double getQ(Object [] row) {
            return (Double)row[startIndex];
        }

        public Double getA(Object [] row) {
            return (Double)row[startIndex+1];
        }

        public Double getB(Object [] row) {
            return (Double)row[startIndex+2];
        }
    }

    public static final AccessQAB WITH_COLUMN    = new AccessQAB(1);
    public static final AccessQAB WITHOUT_COLUMN = new AccessQAB(0);

    /** wstQRanges need to be sorted by range.a */
    public QRangeTree(List<Object []> qRanges, int start, int stop) {
        this(qRanges, WITH_COLUMN, start, stop);
    }

    public QRangeTree(
        List<Object []> qRanges,
        AccessQAB       accessQAB,
        int             start,
        int             stop
    ) {
        if (stop <= start) {
            return;
        }

        int N = stop-start;

        List<Node> nodes = new ArrayList<Node>(N);

        Node last = null;

        for (int i = 0; i < N; ++i) {
            Object [] qRange = qRanges.get(start + i);
            Double q = accessQAB.getQ(qRange);
            Double a = accessQAB.getA(qRange);
            Double b = accessQAB.getB(qRange);

            double av = a != null ? a.doubleValue() : -Double.MAX_VALUE;
            double bv = b != null ? b.doubleValue() :  Double.MAX_VALUE;
            double qv = q.doubleValue();

            // If nodes are directly neighbored and Qs are the same
            // join them.
            if (last != null
            && Math.abs(last.b - av) < EPSILON
            && Math.abs(last.q - qv) < EPSILON) {
                last.b = bv;
            }
            else {
                nodes.add(last = new Node(av, bv, qv));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Before/after nodes join: " +
                N + "/" + nodes.size());
        }

        root = wireTree(nodes);
    }

    protected static Node wireTree(List<Node> nodes) {
        int N = nodes.size();
        for (int i = 0; i < N; ++i) {
            Node node = nodes.get(i);
            if (i > 0  ) node.prev = nodes.get(i-1);
            if (i < N-1) node.next = nodes.get(i+1);
        }

        return buildTree(nodes, 0, N-1);
    }

    protected static Node buildTree(List<Node> nodes, int lo, int hi) {

        if (lo > hi) {
            return null;
        }

        int mid = (lo + hi) >> 1;
        Node parent = nodes.get(mid);

        parent.left  = buildTree(nodes, lo, mid-1);
        parent.right = buildTree(nodes, mid+1, hi);

        return parent;
    }

    public double maxQ() {
        double max = -Double.MAX_VALUE;
        for (Node node = head(); node != null; node = node.next) {
            if (node.q > max) {
                max = node.q;
            }
        }
        return max;
    }

    /** @param pos the station (km). */
    public double findQ(double pos) {
        return root != null ? root.findQ(pos) : Double.NaN;
    }

    public Node findNode(double pos) {
        return root != null ? root.findNode(pos) : null;
    }

    protected Node head() {
        Node head = root;
        while (head.left != null) {
            head = head.left;
        }
        return head;
    }

    public boolean intersectsQRange(double qMin, double qMax) {
        if (qMin > qMax) {
            double t = qMin;
            qMin = qMax;
            qMax = t;
        }
        for (Node curr = head(); curr != null; curr = curr.next) {
            if (curr.q >= qMin || curr.q <= qMax) {
                return true;
            }
        }
        return false;
    }

    public List<Range> findSegments(double a, double b) {
        if (a > b) { double t = a; a = b; b = t; }
        return findSegments(new Range(a, b));
    }

    public List<Range> findSegments(Range range) {
        List<Range> segments = new ArrayList<Range>();

        // Linear scan should be good enough here.
        for (Node curr = head(); curr != null; curr = curr.next) {
            if (!range.disjoint(curr.a, curr.b)) {
                Range r = new Range(curr.a, curr.b);
                if (r.clip(range)) {
                    segments.add(r);
                }
            }
        }

        return segments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        inorder(root, sb);
        return sb.toString();
    }

    protected static void inorder(Node node, StringBuilder sb) {
        if (node != null) {
            inorder(node.left, sb);
            sb.append('[')
              .append(node.a)
              .append(", ")
              .append(node.b)
              .append(": ")
              .append(node.q)
              .append(']');
            inorder(node.right, sb);
        }
    }

    private static final String name(Object o) {
        return String.valueOf(System.identityHashCode(o) & 0xffffffffL);
    }

    public String toGraph() {
        StringBuilder sb = new StringBuilder();
        sb.append("subgraph c");
        sb.append(name(this));
        sb.append(" {\n");
        if (root != null) {
            java.util.Deque<Node> stack = new java.util.ArrayDeque<Node>();
            stack.push(root);
            while (!stack.isEmpty()) {
                Node current = stack.pop();
                String name = "n" + name(current);
                sb.append(name);
                sb.append(" [label=\"");
                sb.append(current.a).append(", ").append(current.b);
                sb.append(": ").append(current.q).append("\"]\n");
                if (current.left != null) {
                    String leftName = name(current.left);
                    sb.append(name).append(" -- n").append(leftName)
                        .append("\n");
                    stack.push(current.left);
                }
                if (current.right != null) {
                    String rightName = name(current.right);
                    sb.append(name).append(" -- n").append(rightName)
                        .append("\n");
                    stack.push(current.right);
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
