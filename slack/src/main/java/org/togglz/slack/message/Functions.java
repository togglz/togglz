package org.togglz.slack.message;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Predicate;

class Functions {

    static final Predicate<String> IS_NOT_NULL_OR_EMPTY = new Predicate<String>() {
        @Override
        public boolean apply(String s) {
            return !isNullOrEmpty(s);
        }
    };
}
