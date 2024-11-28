/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.List;


public class MapserverStyle {

    public static class Clazz {
        protected List<ClazzItem> items;
        protected String    name;

        public Clazz(String name) {
            this.name  = name;
            this.items = new ArrayList<ClazzItem>();
        }

        public void addItem(ClazzItem item) {
            if (item != null) {
                items.add(item);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CLASS\n");
            sb.append("NAME \"" + name + "\"\n");

            for (ClazzItem item: items) {
                item.toString(sb);
            }

            sb.append("END\n");

            return sb.toString();
        }
    }

    public interface ClazzItem {
        void toString(StringBuilder sb);
    }

    public static class Style implements ClazzItem {
        protected String color;
        protected String outlinecolor;
        protected String symbol;
        protected int    size;

        public void setColor(String color) {
            this.color = color;
        }

        public void setOutlineColor(String outlinecolor) {
            this.outlinecolor = outlinecolor;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void setSymbol(String symbol) {
            if (symbol != null && symbol.length() > 0) {
                this.symbol = symbol;
            }
        }

        public void toString(StringBuilder sb) {
            sb.append("STYLE\n");
            sb.append("WIDTH " + String.valueOf(size) + "\n");

            if (outlinecolor != null) {
                sb.append("OUTLINECOLOR " + outlinecolor + "\n");
            }

            if (color != null) {
                sb.append("COLOR " + color + "\n");
            }

            if (symbol != null) {
                sb.append("SYMBOL '" + symbol + "'\n");
            }

            sb.append("END\n");
        }
    } // end of Style

    public static class Label implements ClazzItem {
        protected String color;
        protected int    size;

        public void setColor(String color) {
            this.color = color;
        }

        public void setSize(int size) {
            this.size = size;
        }

        @Override
        public void toString(StringBuilder sb) {
            sb.append("LABEL\n");
            sb.append("ANGLE auto\n");
            sb.append("SIZE " + String.valueOf(size) + "\n");
            sb.append("COLOR " + color + "\n");
            sb.append("TYPE truetype\n");
            sb.append("FONT DefaultFont\n");
            sb.append("POSITION ur\n");
            sb.append("OFFSET 2 2\n");
            sb.append("END\n");
        }
    }

    public static class Expression implements ClazzItem {
        protected String value;

        public Expression(String value) {
            this.value = value;
        }

        @Override
        public void toString(StringBuilder sb) {
            sb.append("EXPRESSION " + value);
            sb.append("\n");
        }
    }


    protected List<Clazz> classes;


    public MapserverStyle() {
        classes = new ArrayList<Clazz>();
    }

    public void addClazz(Clazz clazz) {
        if (clazz != null) {
            classes.add(clazz);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Clazz clazz: classes) {
            sb.append(clazz.toString());
        }

        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
