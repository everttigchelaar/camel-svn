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
package org.apache.camel.component.file;

import java.io.File;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Unit test for consuming the same filename only.
 */
public class FileConsumeAsyncTest extends ContextTestSupport {

    @Override
    protected void setUp() throws Exception {
        deleteDirectory("target/files");
        super.setUp();
        template.sendBodyAndHeader("file://target/files", "Hello World", Exchange.FILE_NAME, "report.txt");
    }

    public void testConsumeAsync() throws Exception {
        MockEndpoint before = getMockEndpoint("mock:before");
        before.expectedMessageCount(1);
        before.assertIsSatisfied();

        // file should still exist as its the async done that will complete it
        assertTrue("File should not have been deleted", new File("target/files/report.txt").getAbsoluteFile().exists());

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.assertIsSatisfied();

        oneExchangeDone.matchesMockWaitTime();

        assertFalse("File should been deleted", new File("target/files/report.txt").getAbsoluteFile().exists());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("file://target/files/?delete=true&delay=10000")
                    .convertBodyTo(String.class)
                    .threads()
                        .to("mock:before")
                        .delay(1000)
                        .to("mock:result");
            }
        };
    }
}