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
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;

public class ExplicitHttpsRouteTest extends HttpsRouteTest {

    // START SNIPPET: e2
    private SslSelectChannelConnector createSslSocketConnector() throws URISyntaxException {
        // From Camel 2.5.0 Camel-Jetty is using SslSelectChannelConnector instead of SslSocketConnector
        SslSelectChannelConnector sslSocketConnector = new SslSelectChannelConnector();
        sslSocketConnector.setKeyPassword(pwd);
        sslSocketConnector.setPassword(pwd);
        URL keyStoreUrl = this.getClass().getClassLoader().getResource("jsse/localhost.ks");
        sslSocketConnector.setKeystore(keyStoreUrl.toURI().getPath());
        sslSocketConnector.setTruststoreType("JKS");
        return sslSocketConnector;
    }
    // END SNIPPET: e2

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws URISyntaxException {
                port1 = getPort();
                port2 = getNextPort();

                // START SNIPPET: e1
                // create SSL select channel connectors for port 9080 and 9090
                Map<Integer, SslSelectChannelConnector> connectors = new HashMap<Integer, SslSelectChannelConnector>();
                connectors.put(port1, createSslSocketConnector());
                connectors.put(port2, createSslSocketConnector());

                // create jetty component
                JettyHttpComponent jetty = new JettyHttpComponent();
                // add connectors
                jetty.setSslSocketConnectors(connectors);
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