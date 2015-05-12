package org.togglz.junit.vary;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TogglzRuleWithVariations_CalledMultipleTimes_Test {

    @Rule
    public TogglzRule togglzRule = TogglzRule.builder(MyFeatures.class)
            .vary(MyFeatures.F2)
            .vary(MyFeatures.F3)
            .build();

    private List<Tuple> tuples = new ArrayList<Tuple>();
    private int i;

    @Test
    public void test() {
        tuples.add(new Tuple(MyFeatures.F1.isActive(), MyFeatures.F2.isActive(), MyFeatures.F3.isActive()));

        i++;
        switch (i) {
        case 1:
            // can't assert here because the order seems to vary between "environment" (ie mvn vs IDE)
            break;
        case 2:
            // can't assert here because the order seems to vary between "environment" (ie mvn vs IDE)
            break;
        case 3:
            // can't assert here because the order seems to vary between "environment" (ie mvn vs IDE)
            break;
        case 4:
            assertTrue(tuples.contains(new Tuple(false,false,false)));
            assertTrue(tuples.contains(new Tuple(false,false,true)));
            assertTrue(tuples.contains(new Tuple(false,true,false)));
            assertTrue(tuples.contains(new Tuple(false, true, true)));
            break;
        default:
            fail();
        }
    }


    static class Tuple {
        boolean f1;
        boolean f2;
        boolean f3;

        public Tuple(final boolean f1, final boolean f2, final boolean f3) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Tuple tuple = (Tuple) o;

            if (f1 != tuple.f1)
                return false;
            if (f2 != tuple.f2)
                return false;
            return f3 == tuple.f3;
        }

        @Override
        public int hashCode() {
            int result = (f1 ? 1 : 0);
            result = 31 * result + (f2 ? 1 : 0);
            result = 31 * result + (f3 ? 1 : 0);
            return result;
        }

        @Override public String toString() {
            return "Tuple{" +
                    "f1=" + f1 +
                    ", f2=" + f2 +
                    ", f3=" + f3 +
                    '}';
        }
    }



}
