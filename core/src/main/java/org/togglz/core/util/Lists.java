package org.togglz.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Lists {

    public static <E> List<E> asList(Iterator<E> i) {
        List<E> list = new ArrayList<E>();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }

}
