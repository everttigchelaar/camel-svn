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
package org.apache.camel.itest.jetty;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.language.simple.SimpleLanguage.simple;

/**
 * @version 
 */
public class JettySimpleSetHeaderTest extends CamelTestSupport {

    @Test
    public void testJettySimpleSetHeader() throws Exception {
        getMockEndpoint("mock:before").message(0).header("beer").isNull();

        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedBodiesReceived("Hello World");
        result.message(0).header("beer").isEqualTo("Carlsberg");

        String reply = template.requestBody("http://localhost:8224/beer", "Hello World", String.class);
        assertEquals("Bye World", reply);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jetty:http://localhost:8224/beer")
                        .convertBodyTo(String.class)
                        .to("mock:before")
                        .setHeader("beer", simple("Carlsberg"))
                        .to("mock:result")
                        .transform(simple("Bye World"));
            }
        };
    }
}