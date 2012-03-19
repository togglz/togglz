package org.togglz.core.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.togglz.core.util.Weighted.WeightedComparator;

public class WeightedTest {

    @Test
    public void testWeightedComparator() {

        List<Weighted> list = new ArrayList<Weighted>();
        list.add(new Second());
        list.add(new First());
        Collections.sort(list, new WeightedComparator());

        assertEquals(First.class, list.get(0).getClass());
        assertEquals(Second.class, list.get(1).getClass());

    }

    private final static class First implements Weighted {

        @Override
        public int priority() {
            return 10;
        }

    }

    private final static class Second implements Weighted {

        @Override
        public int priority() {
            return 20;
        }

    }

}
