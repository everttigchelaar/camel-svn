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
package org.apache.camel.component.jcr;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.Before;
import org.junit.Test;

public class JcrRouteTest extends CamelTestSupport {

    private Repository repository;

    @Override
    @Before
    public void setUp() throws Exception {
        deleteDirectory("target/repository");
        super.setUp();
    }

    @Test
    public void testJcrRoute() throws Exception {
        Exchange exchange = createExchangeWithBody("<hello>world!</hello>");
        Exchange out = template.send("direct:a", exchange);
        assertNotNull(out);
        String uuid = out.getOut().getBody(String.class);
        Session session = repository.login(new SimpleCredentials("user", "pass".toCharArray()));
        try {
            Node node = session.getNodeByUUID(uuid);
            assertNotNull(node);
            assertEquals("/home/test/node", node.getPath());
            assertEquals("<hello>world!</hello>", node.getProperty("my.contents.property").getString());
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // START SNIPPET: jcr
                from("direct:a").setProperty(JcrConstants.JCR_NODE_NAME, constant("node"))
                    .setProperty("my.contents.property", body()).to("jcr://user:pass@repository/home/test");
                // END SNIPPET: jcr
            }
        };
    }

    @Override
    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();
        repository = new TransientRepository("target/repository.xml", "target/repository");
        context.bind("repository", repository);
        return context;
    }
}
