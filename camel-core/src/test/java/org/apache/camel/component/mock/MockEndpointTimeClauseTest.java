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
package org.apache.camel.component.mock;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class MockEndpointTimeClauseTest extends ContextTestSupport {

    public void testReceivedTimestamp() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.message(0).property(Exchange.CREATED_TIMESTAMP).isNotNull();
        mock.message(0).property(Exchange.CREATED_TIMESTAMP).isInstanceOf(Date.class);
        mock.message(0).property(Exchange.RECEIVED_TIMESTAMP).isNotNull();
        mock.message(0).property(Exchange.RECEIVED_TIMESTAMP).isInstanceOf(Date.class);

        template.sendBody("direct:a", "A");

        assertMockEndpointsSatisfied();
    }

    public void testAssertPeriod() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.setAssertPeriod(1000);

        template.sendBody("direct:a", "A");

        assertMockEndpointsSatisfied();
    }

    public void testAssertPeriodNot() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.setAssertPeriod(1000);

        template.sendBody("direct:a", "A");
        template.sendBody("direct:a", "B");

        mock.assertIsNotSatisfied();
    }

    public void testAssertPeriodSecondMessageArrives() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        // wait 2 sec after preliminary assertion to ensure its still correct
        // after the 2 seconds.
        mock.setAssertPeriod(2000);

        template.sendBody("direct:a", "A");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // ignore
                }
                template.sendBody("direct:a", "B");
            }
        });

        try {
            mock.assertIsSatisfied();
            fail("Should have thrown an exception");
        } catch (AssertionError e) {
            assertEquals("mock://result Received message count. Expected: <1> but was: <2>", e.getMessage());
        }
    }

    public void testNoAssertPeriodSecondMessageArrives() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:a", "A");

        // this executor was bound to send a 2nd message
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // ignore
                }
                template.sendBody("direct:a", "B");
            }
        });

        // but the assertion would be complete before hand and thus
        // the assertion was valid at the time given
        assertMockEndpointsSatisfied();
    }

    public void testArrivesBeforeNext() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.message(0).arrives().noLaterThan(2).seconds().beforeNext();

        template.sendBody("direct:a", "A");
        Thread.sleep(500);
        template.sendBody("direct:a", "B");

        assertMockEndpointsSatisfied();
    }

    public void testArrivesAfterPrevious() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.message(1).arrives().noLaterThan(2).seconds().afterPrevious();

        template.sendBody("direct:a", "A");
        Thread.sleep(500);
        template.sendBody("direct:a", "B");

        assertMockEndpointsSatisfied();
    }

    public void testArrivesBeforeAndAfter() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(3);
        mock.message(1).arrives().noLaterThan(500).millis().afterPrevious();
        mock.message(1).arrives().noLaterThan(1000).millis().beforeNext();

        template.sendBody("direct:a", "A");
        Thread.sleep(100);
        template.sendBody("direct:a", "B");
        Thread.sleep(200);
        template.sendBody("direct:a", "C");

        assertMockEndpointsSatisfied();
    }

    public void testArrivesWithinAfterPrevious() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.message(1).arrives().between(1, 4).seconds().afterPrevious();

        template.sendBody("direct:a", "A");
        Thread.sleep(1500);
        template.sendBody("direct:a", "B");

        assertMockEndpointsSatisfied();
    }

    public void testArrivesWithinBeforeNext() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);
        mock.message(0).arrives().between(1, 4).seconds().beforeNext();

        template.sendBody("direct:a", "A");
        Thread.sleep(1500);
        template.sendBody("direct:a", "B");

        assertMockEndpointsSatisfied();
    }

    public void testArrivesAllMessages() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(5);
        mock.allMessages().arrives().noLaterThan(1).seconds().beforeNext();

        template.sendBody("direct:a", "A");
        template.sendBody("direct:a", "B");
        Thread.sleep(100);
        template.sendBody("direct:a", "C");
        Thread.sleep(150);
        template.sendBody("direct:a", "D");
        Thread.sleep(200);
        template.sendBody("direct:a", "E");

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:a").to("mock:result");
            }
        };
    }

}
