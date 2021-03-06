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
package org.apache.camel.component.log;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.CamelLogger;

/**
 * @version 
 */
public class LogEndpointTest extends ContextTestSupport {

    private static Exchange logged;

    private class MyLogger extends CamelLogger {

        @Override
        public void process(Exchange exchange) {
            super.process(exchange);
            logged = exchange;
        }

        @Override
        public String toString() {
            return "myLogger";
        }
    }

    public void testLogEndpoint() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();

        assertNotNull(logged);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                LogEndpoint end = new LogEndpoint();
                end.setCamelContext(context);
                end.setLogger(new MyLogger());

                assertEquals("log:myLogger", end.getEndpointUri());
                assertNotNull(end.getLogger());

                from("direct:start").to(end).to("mock:result");
            }
        };
    }
}
