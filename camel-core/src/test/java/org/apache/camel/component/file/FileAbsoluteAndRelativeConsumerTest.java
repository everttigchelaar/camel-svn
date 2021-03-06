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
 * @version 
 */
public class FileAbsoluteAndRelativeConsumerTest extends ContextTestSupport {

    private String base;

    @Override
    protected void setUp() throws Exception {
        deleteDirectory("target/filerelative");
        deleteDirectory("target/fileabsolute");
        // use current dir as base as aboslute path
        base = new File("").getAbsolutePath() + "/target/fileabsolute";
        super.setUp();
    }

    public void testRelative() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:relative");
        mock.expectedMessageCount(1);

        mock.message(0).header(Exchange.FILE_NAME).isEqualTo("test" + File.separator + "hello.txt");
        mock.message(0).header(Exchange.FILE_NAME_ONLY).isEqualTo("hello.txt");

        template.sendBodyAndHeader("file://target/filerelative", "Hello World", Exchange.FILE_NAME, "test/hello.txt");

        assertMockEndpointsSatisfied();
    }

    public void testAbsolute() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:absolute");
        mock.expectedMessageCount(1);

        mock.message(0).header(Exchange.FILE_NAME).isEqualTo("test" + File.separator + "hello.txt");
        mock.message(0).header(Exchange.FILE_NAME_ONLY).isEqualTo("hello.txt");

        template.sendBodyAndHeader("file://target/fileabsolute", "Hello World", Exchange.FILE_NAME, "test/hello.txt");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/filerelative?recursive=true").convertBodyTo(String.class).to("mock:relative");

                from("file://" + base + "?recursive=true").convertBodyTo(String.class).to("mock:absolute");
            }
        };
    }
}
