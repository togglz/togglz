/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.togglz.spring.boot.actuate.autoconfigure;

import org.togglz.core.repository.property.PropertySource;

import java.util.*;

/**
 * {@link PropertySource} implementation for use with {@link Properties}.
 *
 * @author Marcel Overdijk
 */
public class PropertiesPropertySource implements PropertySource {

    private Properties values = new Properties();

    public PropertiesPropertySource(Properties properties) {
        this.values = properties;
    }

    @Override
    public void reloadIfUpdated() {
        // do nothing
    }

    @Override
    public Set<String> getKeysStartingWith(String prefix) {
        Set<String> result = new HashSet<String>();
        Enumeration<?> keys = values.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }

    @Override
    public Editor getEditor() {
        return new PropertiesEditor(values);
    }

    private void setValues(Properties values) {
        this.values = values;
    }

    private class PropertiesEditor implements Editor {

        private Properties newValues;

        private PropertiesEditor(Properties values) {
            newValues = new Properties();
            newValues.putAll(values);
        }

        @Override
        public void setValue(String key, String value) {
            if (value != null) {
                newValues.setProperty(key, value);
            } else {
                newValues.remove(key);
            }
        }

        @Override
        public void removeKeysStartingWith(String prefix) {
            Iterator<Map.Entry<Object, Object>> iterator = newValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> entry = iterator.next();
                if (entry.getKey().toString().startsWith(prefix)) {
                    iterator.remove();
                }
            }
        }

        @Override
        public void commit() {
            setValues(newValues);
        }
    }
}
