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
package org.apache.camel.itest.osgi.spring.xslt;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.UrlReference;

import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;


@RunWith(JUnit4TestRunner.class)
public class XsltRouteTest extends OSGiIntegrationTestSupport {

    @Test
    public void testXsltRoute() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("<?xml version=\"1.0\" encoding=\"UTF-8\"?><goodbye>world!</goodbye>");
        mock.message(0).body().isInstanceOf(String.class);

        template.sendBody("direct:start", "<hello>world!</hello>");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .to("xslt:org/apache/camel/itest/osgi/spring/xslt/example.xsl")
                    .to("log:result")
                    .to("mock:result");
            }
        };
    }
    
    @Configuration
    public static Option[] configure() throws Exception {
        Option[] options = options(
            // install the spring dm profile            
            profile("spring.dm").version("1.2.0"),
            // setup the boot delegation of sun's package
            //bootDelegationPackage("com.sun.org.apache.xml.internal.*"),
            systemPackage("com.sun.org.apache.xalan.internal.xsltc"),
            systemPackage("com.sun.org.apache.xml.internal.dtm"),
            
            // this is how you set the default log level when using pax logging (logProfile)
            org.ops4j.pax.exam.CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),

            // need to install some karaf features
            scanFeatures(getKarafFeatureUrl(), "http"),

            // using the features to install the camel components             
            scanFeatures(getCamelKarafFeatureUrl(),                         
                          "camel-core", "camel-spring", "camel-test"),
            
            workingDirectory("target/paxrunner/"),
            
            equinox(),
            felix());
        
        return options;
    }

}
