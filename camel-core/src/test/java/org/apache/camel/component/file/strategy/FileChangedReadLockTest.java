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
package org.apache.camel.component.file.strategy;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 
 */
public class FileChangedReadLockTest extends ContextTestSupport {

    private static final transient Logger LOG = LoggerFactory.getLogger(FileChangedReadLockTest.class);

    public void testChangedReadLock() throws Exception {
        deleteDirectory("target/changed/");
        createDirectory("target/changed/in");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists("target/changed/out/slowfile.dat");

        writeSlowFile();

        assertMockEndpointsSatisfied();

        String content = context.getTypeConverter().convertTo(String.class, new File("target/changed/out/slowfile.dat").getAbsoluteFile());
        String[] lines = content.split("\n");
        for (int i = 0; i < 20; i++) {
            assertEquals("Line " + i, lines[i]);
        }
    }

    private void writeSlowFile() throws Exception {
        LOG.debug("Writing slow file...");

        FileOutputStream fos = new FileOutputStream("target/changed/in/slowfile.dat");
        for (int i = 0; i < 20; i++) {
            fos.write(("Line " + i + "\n").getBytes());
            LOG.debug("Writing line " + i);
            Thread.sleep(200);
        }

        fos.flush();
        fos.close();
        LOG.debug("Writing slow file DONE...");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:target/changed/in?readLock=changed").to("file:target/changed/out", "mock:result");
            }
        };
    }
}
