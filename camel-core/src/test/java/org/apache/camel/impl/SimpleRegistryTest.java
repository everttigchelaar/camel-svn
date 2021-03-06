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
package org.apache.camel.impl;

import java.util.Map;

import junit.framework.TestCase;

public class SimpleRegistryTest extends TestCase {

    private SimpleRegistry registry;
    
    protected void setUp() throws Exception {
        registry = new SimpleRegistry();
        registry.put("a", "b");
        registry.put("c", 1);
    }

    public void testLookupByName() {
        assertEquals("b", registry.lookup("a"));
    }

    public void testLookupByWrongName() {
        assertNull(registry.lookup("x"));
    }

    public void testLookupByNameAndType() {
        assertEquals("b", registry.lookup("a", String.class));
    }

    public void testLookupByNameAndWrongType() {
        try {
            registry.lookup("a", Float.class);
            fail();
        } catch (ClassCastException e) {
            // expected
        }
    }
    
    public void testLookupByType() {
        Map map = registry.lookupByType(String.class);
        assertEquals(1, map.size());
        assertEquals("b", map.get("a"));
        map = registry.lookupByType(Object.class);
        assertEquals(2, map.size());
        assertEquals("b", map.get("a"));
        assertEquals(1, map.get("c"));
    }
 
    public void testLookupByWrongType() {
        Map map = registry.lookupByType(Float.class);
        assertEquals(0, map.size());
    }

}
