/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.freemarker;

import freemarker.cache.CacheStorage;

/**
 * A cache storage for Freemarker with no cache used for development to force reload of templates
 * on every request.
 */
public class NoCacheStorage implements CacheStorage {

    public Object get(Object key) {
        // noop
        return null;
    }

    public void put(Object key, Object value) {
        // noop
    }

    public void remove(Object key) {
        // noop
    }

    public void clear() {
        // noop
    }

}
