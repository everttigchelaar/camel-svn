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
package org.apache.camel.itest.osgi.core.seda;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * @version 
 */
@RunWith(JUnit4TestRunner.class)
public class SedaTest extends OSGiIntegrationTestSupport {
    
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("seda:foo").to("mock:bar");
            }
        };
    }

    @Test
    public void testSendMessage() throws Exception {
        MockEndpoint mock =  getMandatoryEndpoint("mock:bar", MockEndpoint.class);
        assertNotNull("The mock endpoint should not be null", mock);
        
        mock.expectedBodiesReceived("Hello World");
        template.sendBody("seda:foo", "Hello World");
        assertMockEndpointsSatisfied();        
    }
    
    @Test
    public void testCamelContextName() throws Exception {
        // should get the context name with osgi bundle id
        String name1 = context.getName();

        CamelContext context2 = createCamelContext();
        String name2 = context2.getName();

        assertNotSame(name1, name2);

        String id = "" + bundleContext.getBundle().getBundleId();
        assertTrue(name1.startsWith(id));
        assertTrue(name2.startsWith(id));
    }
   
}