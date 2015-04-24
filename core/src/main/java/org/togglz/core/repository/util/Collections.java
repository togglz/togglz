package org.togglz.core.repository.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Collections {

	private Collections() {}

    /**
     * A convenience method to merge a single argument plus the (possibly zero) remaining values.
     * Useful for iterating over a varargs collection that requires at least one value.
     *  
     * @param <T> the type
     * @param pFirst the first (required) values
     * @param pRest the remaining values
     * @return the set containing all of the values
     */
    public static <T> List<T> merge(T pFirst, T... pRest) {
        
        List<T> result = new ArrayList<T>(1 + (pRest == null ? 0 : pRest.length));
        
        result.add(pFirst);
        if (pRest != null) {
            result.addAll(Arrays.asList(pRest));
        }
        
        return result;
    }
}
