package org.togglz.googleclouddatastore.repository;

import com.google.cloud.datastore.BooleanValue;
import com.google.cloud.datastore.StringValue;

/**
 * Created by fabio on 31/12/16.
 */
class NonIndexed {

    static BooleanValue valueOf(Boolean input) {
        return BooleanValue.newBuilder(input)
                .setExcludeFromIndexes(true).build();
    }

    static StringValue valueOf(String input) {
        return StringValue.newBuilder(input)
                .setExcludeFromIndexes(true)
                .build();
    }
}
