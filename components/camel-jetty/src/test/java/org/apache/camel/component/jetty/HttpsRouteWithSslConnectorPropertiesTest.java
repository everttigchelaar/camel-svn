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
package org.apache.camel.component.jetty;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class HttpsRouteWithSslConnectorPropertiesTest extends HttpsRouteTest {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws URISyntaxException {
                port1 = getPort();
                port2 = getNextPort();

                // START SNIPPET: e1
                // keystore path
                URL keyStoreUrl = this.getClass().getClassLoader().getResource("jsse/localhost.ks");
                String path = keyStoreUrl.toURI().getPath();

                // map with properties
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("keyPassword", pwd);
                properties.put("password", pwd);
                properties.put("keystore", path);
                properties.put("truststoreType", "JKS");

                // create jetty component
                JettyHttpComponent jetty = new JettyHttpComponent();
                jetty.setSslSocketConnectorProperties(properties);

                // add jetty to camel context
                context.addComponent("jetty", jetty);
                // END SNIPPET: e1

                from("jetty:https://localhost:" + port1 + "/test").to("mock:a");

                Processor proc = new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setBody("<b>Hello World</b>");
                    }
                };
                from("jetty:https://localhost:" + port1 + "/hello").process(proc);

                from("jetty:https://localhost:" + port2 + "/test").to("mock:b");
            }
        };
    }
}