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
package org.apache.camel.processor.enricher;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

public class PollEnricherTest extends ContextTestSupport {

    private static SampleAggregator aggregationStrategy = new SampleAggregator();

    protected MockEndpoint mock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mock = getMockEndpoint("mock:mock");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // -------------------------------------------------------------
    //  InOnly routes
    // -------------------------------------------------------------

    public void testPollEnrichInOnly() throws InterruptedException {
        template.sendBody("direct:foo1", "blah");

        mock.expectedBodiesReceived("test:blah");
        template.sendBody("direct:enricher-test-1", "test");
        mock.assertIsSatisfied();
    }

    public void testPollEnrichInOnlyWaitWithTimeout() throws InterruptedException {
        // this first try there is no data so we timeout
        mock.expectedBodiesReceived("test:blah");
        template.sendBody("direct:enricher-test-2", "test");
        // not expected data so we are not happy
        mock.assertIsNotSatisfied();

        // now send it and try again
        mock.reset();
        template.sendBody("direct:foo2", "blah");
        template.sendBody("direct:enricher-test-2", "test");
        mock.assertIsSatisfied();
    }

    public void testPollEnrichInOnlyWaitNoTimeout() throws InterruptedException {
        // use another thread to send it a bit later
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // ignore
                }
                template.sendBody("direct:foo3", "blah");
            }
        });

        long start = System.currentTimeMillis();
        mock.expectedBodiesReceived("test:blah");
        t.start();
        template.sendBody("direct:enricher-test-3", "test");
        // should take approx 1 sec to complete as the other thread is sending a bit later and we wait
        mock.assertIsSatisfied();
        long delta = System.currentTimeMillis() - start;
        assertTrue("Should take approx 0.25 sec: was " + delta, delta > 150);
    }

    // -------------------------------------------------------------
    //  InOut routes
    // -------------------------------------------------------------

    public void testPollEnrichInOut() throws InterruptedException {
        template.sendBody("direct:foo4", "blah");

        String result = (String) template.sendBody("direct:enricher-test-4", ExchangePattern.InOut, "test");
        assertEquals("test:blah", result);
    }

    public void testPollEnrichInOutPlusHeader() throws InterruptedException {
        template.sendBody("direct:foo4", "blah");

        Exchange exchange = template.request("direct:enricher-test-4", new Processor() {
            public void process(Exchange exchange) {
                exchange.getIn().setHeader("foo", "bar");
                exchange.getIn().setBody("test");
            }
        });
        assertEquals("bar", exchange.getIn().getHeader("foo"));
        assertEquals("test:blah", exchange.getIn().getBody());
        assertFalse(exchange.hasOut() && exchange.getOut().isFault());
        assertNull(exchange.getException());
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // -------------------------------------------------------------
                //  InOnly routes
                // -------------------------------------------------------------

                from("direct:enricher-test-1")
                    .pollEnrich("direct:foo1", aggregationStrategy)
                    .to("mock:mock");

                from("direct:enricher-test-2")
                    .pollEnrich("direct:foo2", 1000, aggregationStrategy)
                    .to("mock:mock");

                from("direct:enricher-test-3")
                    .pollEnrich("direct:foo3", -1, aggregationStrategy)
                    .to("mock:mock");

                // -------------------------------------------------------------
                //  InOut routes
                // -------------------------------------------------------------

                from("direct:enricher-test-4")
                    .pollEnrich("direct:foo4", aggregationStrategy);
            }
        };
    }

}