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
package org.apache.camel.component.netty;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUDPSyncTest extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(NettyUDPSyncTest.class);

    @Produce(uri = "direct:start")
    protected ProducerTemplate producerTemplate;

    @Test
    public void testUDPStringInOutWithNettyConsumer() throws Exception {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Beginning Test ---> testUDPInOutWithNettyConsumer()");
        }
        
        for (int i = 0; i < 5; i++) {
            String response = producerTemplate.requestBody(
                "netty:udp://localhost:5152?sync=true", 
                "After the Battle of Thermopylae in 480 BC - Simonides of Ceos (c. 556 BC-468 BC), Greek lyric poet wrote ?", String.class);        
            assertEquals("Go tell the Spartans, thou that passest by, That faithful to their precepts here we lie.", response);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Completed Test ---> testUDPInOutWithNettyConsumer()");
        }        
    }   
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {                
                from("netty:udp://localhost:5152?sync=true")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            exchange.getOut().setBody("Go tell the Spartans, thou that passest by, That faithful to their precepts here we lie.");                           
                        }
                    });
            }
        };
    }
    

} 
