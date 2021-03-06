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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

public class MultipleCodecsTest extends CamelTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();

        // START SNIPPET: registry-beans
        LengthFieldBasedFrameDecoder lengthDecoder = new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4);
        StringDecoder stringDecoder = new StringDecoder();
        registry.bind("length-decoder", lengthDecoder);
        registry.bind("string-decoder", stringDecoder);

        LengthFieldPrepender lengthEncoder = new LengthFieldPrepender(4);
        StringEncoder stringEncoder = new StringEncoder();
        registry.bind("length-encoder", lengthEncoder);
        registry.bind("string-encoder", stringEncoder);

        List<ChannelUpstreamHandler> decoders = new ArrayList<ChannelUpstreamHandler>();
        decoders.add(lengthDecoder);
        decoders.add(stringDecoder);

        List<ChannelDownstreamHandler> encoders = new ArrayList<ChannelDownstreamHandler>();
        encoders.add(lengthEncoder);
        encoders.add(stringEncoder);

        registry.bind("encoders", encoders);
        registry.bind("decoders", decoders);
        // END SNIPPET: registry-beans
        return registry;
    }

    @Test
    public void canSupplyMultipleCodecsToEndpointPipeline() throws Exception {
        String poem = new Poetry().getPoem();
        MockEndpoint mock = getMockEndpoint("mock:multiple-codec");
        mock.expectedBodiesReceived(poem);
        sendBody("direct:multiple-codec", poem);
        mock.await(1, TimeUnit.SECONDS);
        mock.assertIsSatisfied();

    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                // START SNIPPET: routes
                from("direct:multiple-codec").to("netty:tcp://localhost:5150?encoders=#encoders&sync=false");
                
                from("netty:tcp://localhost:5150?decoders=#length-decoder,#string-decoder&sync=false").to("mock:multiple-codec");
                // START SNIPPET: routes
            }
        };
    }
}
