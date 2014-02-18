package org.togglz.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.togglz.core.util.Weighted.WeightedComparator;

public class WeightedTest {

    @Test
    public void shouldSortCorrectlySimpleIntValues() {

        Weighted prio10 = new Weighted() {
            @Override
            public int priority() {
                return 10;
            }
        };

        Weighted prio20 = new Weighted() {
            @Override
            public int priority() {
                return 20;
            }
        };

        List<Weighted> list = new ArrayList<Weighted>();
        list.add(prio20);
        list.add(prio10);
        Collections.sort(list, new WeightedComparator());

        assertThat(list).containsExactly(prio10, prio20);

    }

    @Test
    public void shouldSupportMinimumIntValue() {

        Weighted prioMinInt = new Weighted() {
            @Override
            public int priority() {
                return Integer.MIN_VALUE;
            }
        };

        Weighted prio50 = new Weighted() {
            @Override
            public int priority() {
                return 50;
            }
        };

        List<Weighted> list = new ArrayList<Weighted>();
        list.add(prio50);
        list.add(prioMinInt);
        Collections.sort(list, new WeightedComparator());

        assertThat(list).containsExactly(prioMinInt, prio50);

    }

    @Test
    public void shouldSupportMaximumIntValue() {

        Weighted prioMaxInt = new Weighted() {
            @Override
            public int priority() {
                return Integer.MAX_VALUE;
            }
        };

        Weighted prio50 = new Weighted() {
            @Override
            public int priority() {
                return 50;
            }
        };

        List<Weighted> list = new ArrayList<Weighted>();
        list.add(prioMaxInt);
        list.add(prio50);
        Collections.sort(list, new WeightedComparator());

        assertThat(list).containsExactly(prio50, prioMaxInt);

    }

}
