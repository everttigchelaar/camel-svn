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
package org.apache.camel.processor.async;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.WaitForTaskToComplete;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Unit test to verify that error handling using async() also works as expected.
 *
 * @version 
 */
public class AsyncDeadLetterChannelTest extends ContextTestSupport {

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    public void testAsyncErrorHandlerWait() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("mock:dead").maximumRedeliveries(2).redeliveryDelay(0).logStackTrace(false).handled(false));

                from("direct:in")
                    .threads(2)
                    .to("mock:foo")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            throw new Exception("Forced exception by unit test");
                        }
                    });
            }
        });
        context.start();

        getMockEndpoint("mock:foo").expectedBodiesReceived("Hello World");

        MockEndpoint mock = getMockEndpoint("mock:dead");
        mock.expectedMessageCount(1);
        mock.message(0).header(Exchange.REDELIVERED).isEqualTo(Boolean.TRUE);
        mock.message(0).header(Exchange.REDELIVERY_COUNTER).isEqualTo(2);
        mock.message(0).header(Exchange.REDELIVERY_MAX_COUNTER).isEqualTo(2);

        try {
            template.requestBody("direct:in", "Hello World");
            fail("Should have thrown a CamelExecutionException");
        } catch (CamelExecutionException e) {
            assertEquals("Forced exception by unit test", e.getCause().getMessage());
            // expected
        }

        assertMockEndpointsSatisfied();
    }

}
