package org.togglz.console.handlers.index;

import java.util.ArrayList;
import java.util.List;

public class IndexPageTab implements Comparable<IndexPageTab> {

    private final int index;
    private final List<IndexPageRow> rows = new ArrayList<IndexPageRow>();
    private final String label;

    private IndexPageTab(int index, String label) {
        this.index = index;
        this.label = label;
    }

    static IndexPageTab allTab(int index) {
        return new IndexPageTab(index, null);
    }

    static IndexPageTab groupTab(int index, String label) {
        return new IndexPageTab(index, label);
    }

    @Override
    public int compareTo(IndexPageTab o) {
        return (label != null ? label : "").compareTo(o.label != null ? o.label : "");
    }

    public void add(IndexPageRow row) {
        rows.add(row);
    }

    public List<IndexPageRow> getRows() {
        return rows;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAllTab() {
        return index == 0;
    }

}