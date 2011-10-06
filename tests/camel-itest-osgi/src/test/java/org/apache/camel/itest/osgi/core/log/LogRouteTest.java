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
package org.apache.camel.itest.osgi.core.log;

import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

@RunWith(JUnit4TestRunner.class)
public class LogRouteTest extends OSGiIntegrationTestSupport {

    @Test
    public void testSendMessageToLog() throws Exception {
        template.sendBody("log:org.apache.camel.TEST", "<level>default</level>");
    }

    @Test
    public void testSendMessageToInfoLog() throws Exception {
        template.sendBody("log:org.apache.camel.TEST?level=INFO", "<level>INFO</level>");
    }

    @Test
    public void testSendMessageToWarnLog() throws Exception {
        template.sendBody("log:org.apache.camel.TEST?level=warn", "<level>WARN</level>");
    }

    @Test
    public void testSendMessageToBadLevel() throws Exception {
        try {
            template.sendBody("log:org.apache.camel.TEST?level=noSuchLevel", "<level>noSuchLevel</level>");
            fail("Shoudl have failed!");
        } catch (Exception e) {
            LOG.debug("Caught expected exception: " + e, e);
        }
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Configuration
    public static Option[] configure() {
        Option[] options = options(
            // install the spring dm profile            
            profile("spring.dm").version("1.2.0"),    
            // this is how you set the default log level when using pax logging (logProfile)
            org.ops4j.pax.exam.CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
            
            // using the features to install the camel components             
            scanFeatures(getCamelKarafFeatureUrl(),                         
                          "camel-core", "camel-test"),
            
            workingDirectory("target/paxrunner/"),

            felix(), equinox());
        
        return options;
    }

}
