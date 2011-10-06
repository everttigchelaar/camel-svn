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
package org.apache.camel.component.seda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;

/**
 * @version 
 */
public class SedaConcurrentTest extends ContextTestSupport {

    public void testSedaConcurrentInOnly() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(20);

        // should at least take 3 sec 
        mock.setMinimumResultWaitTime(3000);

        for (int i = 0; i < 20; i++) {
            template.sendBody("seda:foo", "Message " + i);
        }

        assertMockEndpointsSatisfied();
    }

    public void testSedaConcurrentInOnlyWithAsync() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(20);

        // should at least take 3 sec
        mock.setMinimumResultWaitTime(3000);

        for (int i = 0; i < 20; i++) {
            template.asyncSendBody("seda:foo", "Message " + i);
        }

        assertMockEndpointsSatisfied();
    }

    public void testSedaConcurrentInOut() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(20);
        mock.allMessages().body().startsWith("Bye");

        // should at least take 3 sec
        mock.setMinimumResultWaitTime(3000);

        ExecutorService executors = Executors.newFixedThreadPool(10);
        List<Object> replies = new ArrayList<Object>(20);
        for (int i = 0; i < 20; i++) {
            final int num = i;
            Object out = executors.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    return template.requestBody("seda:bar", "Message " + num);
                }
            });
            replies.add(out);
        }

        assertMockEndpointsSatisfied();

        assertEquals(20, replies.size());
    }

    public void testSedaConcurrentInOutWithAsync() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(20);
        mock.allMessages().body().startsWith("Bye");

        // should at least take 3 sec
        mock.setMinimumResultWaitTime(3000);

        // use our own template that has a higher thread pool than default camel that uses 5
        ProducerTemplate pt = new DefaultProducerTemplate(context, Executors.newFixedThreadPool(10));
        // must start the template
        pt.start();

        List<Future> replies = new ArrayList<Future>(20);
        for (int i = 0; i < 20; i++) {
            Future<Object> out = pt.asyncRequestBody("seda:bar", "Message " + i);
            replies.add(out);
        }

        assertMockEndpointsSatisfied();

        assertEquals(20, replies.size());
        for (int i = 0; i < 20; i++) {
            String out = (String) replies.get(i).get();
            assertTrue(out.startsWith("Bye"));
        }

        pt.stop();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:foo?concurrentConsumers=10")
                    .to("mock:before").delay(2000).to("mock:result");

                from("seda:bar?concurrentConsumers=10")
                    .to("mock:before").delay(2000).transform(body().prepend("Bye ")).to("mock:result");
            }
        };
    }
}
