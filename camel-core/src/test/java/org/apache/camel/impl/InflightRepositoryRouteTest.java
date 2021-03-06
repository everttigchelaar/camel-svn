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

import java.util.concurrent.CountDownLatch;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class InflightRepositoryRouteTest extends ContextTestSupport {

    private final CountDownLatch latch = new CountDownLatch(1);

    public void testInflight() throws Exception {
        context.setInflightRepository(new MyInflightRepo());

        assertEquals(0, context.getInflightRepository().size());

        template.sendBody("direct:start", "Hello World");

        assertEquals(0, context.getInflightRepository().size());
        assertEquals(0, context.getInflightRepository().size(context.getEndpoint("direct:start")));
        assertEquals(0, context.getInflightRepository().size(context.getEndpoint("mock:result")));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:result");
            }
        };
    }

    private class MyInflightRepo extends DefaultInflightRepository {

        @Override
        public void add(Exchange exchange) {
            super.add(exchange);

            assertEquals(1, context.getInflightRepository().size());

            assertEquals(1, size(context.getEndpoint("direct:start")));

            // but 0 from this endpoint
            assertEquals(0, size(context.getEndpoint("mock:result")));
        }

        @Override
        public void remove(Exchange exchange) {
            super.remove(exchange);
        }
    }
}
