package org.togglz.core.util;

import org.junit.jupiter.api.Test;
import org.togglz.core.util.Weighted.WeightedComparator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightedTest {

    @Test
    void shouldSortCorrectlySimpleIntValues() {
        Weighted prio10 = () -> 10;
        Weighted prio20 = () -> 20;

        List<Weighted> list = new ArrayList<>();
        list.add(prio20);
        list.add(prio10);
        list.sort(new WeightedComparator());

        assertEquals(prio10, list.get(0));
        assertEquals(prio20, list.get(1));
    }

    @Test
    void shouldSupportMinimumIntValue() {
        Weighted prioMinInt = () -> Integer.MIN_VALUE;
        Weighted prio50 = () -> 50;

        List<Weighted> list = new ArrayList<>();
        list.add(prio50);
        list.add(prioMinInt);
        list.sort(new WeightedComparator());

        assertEquals(prioMinInt, list.get(0));
        assertEquals(prio50, list.get(1));
    }

    @Test
    void shouldSupportMaximumIntValue() {
        Weighted prioMaxInt = () -> Integer.MAX_VALUE;
        Weighted prio50 = () -> 50;

        List<Weighted> list = new ArrayList<>();
        list.add(prioMaxInt);
        list.add(prio50);
        list.sort(new WeightedComparator());

        assertEquals(prio50, list.get(0));
        assertEquals(prioMaxInt, list.get(1));
    }
}
