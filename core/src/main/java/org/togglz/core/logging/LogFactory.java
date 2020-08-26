package org.togglz.core.logging;

import java.util.*;

import org.togglz.core.spi.LogProvider;
import org.togglz.core.util.Weighted;

/**
 * 
 * Factory class for creating new loggers. Uses the {@link LogProvider} SPI to find the actual implementation.
 * 
 * @author Christian Kaltepoth
 * 
 */
public class LogFactory {

    private static volatile LogProvider _logProvider;

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String name) {
        return getLogProvider().getLog(name);
    }

    private static LogProvider getLogProvider() {
        if (_logProvider == null) {
            synchronized (LogFactory.class) {
                if (_logProvider == null) {
                    List<LogProvider> providers = asList(ServiceLoader.load(LogProvider.class).iterator());
                    providers.sort(new Weighted.WeightedComparator());
                    _logProvider = providers.get(0);

                }
            }

        }
        return _logProvider;
    }

    private static <E> List<E> asList(Iterator<E> i) {
        List<E> list = new ArrayList<E>();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }
}
