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
package org.apache.camel.routepolicy.quartz;

import java.util.Date;

import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 
 */
public class SimpleScheduledCombinedRoutePolicyTest extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(SimpleScheduledCombinedRoutePolicyTest.class);
    
    /* (non-Javadoc)
     * @see org.apache.camel.test.junit4.CamelTestSupport#s;etUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    /* (non-Javadoc)
     * @see org.apache.camel.test.junit4.CamelTestSupport#isUseRouteBuilder()
     */
    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Test
    public void testScheduledStartAndStopRoutePolicy() throws Exception {
        MockEndpoint success = (MockEndpoint) context.getEndpoint("mock:success");        
        
        success.expectedMessageCount(1);
        
        context.getComponent("quartz", QuartzComponent.class).setPropertiesFile("org/apache/camel/routepolicy/quartz/myquartz.properties");
        context.getComponent("quartz", QuartzComponent.class).start();
        context.addRoutes(new RouteBuilder() {
            public void configure() {   
                SimpleScheduledRoutePolicy policy = new SimpleScheduledRoutePolicy();
                long startTime = System.currentTimeMillis() + 3000L;
                long stopTime = System.currentTimeMillis() + 8000L;
                policy.setRouteStartDate(new Date(startTime));
                policy.setRouteStartRepeatCount(1);
                policy.setRouteStartRepeatInterval(3000);
                policy.setRouteStopDate(new Date(stopTime));
                policy.setRouteStopRepeatCount(1);
                policy.setRouteStopRepeatInterval(3000);
                
                from("direct:start")
                    .routeId("test")
                    .routePolicy(policy)
                    .to("mock:success");
            }
        });
        context.start();
        
        Thread.sleep(5000);
        assertTrue(context.getRouteStatus("test") == ServiceStatus.Started);
        template.sendBody("direct:start", "Ready or not, Here, I come");
        Thread.sleep(5000);
        assertTrue(context.getRouteStatus("test") == ServiceStatus.Stopped);
        
        context.getComponent("quartz", QuartzComponent.class).stop();
        success.assertIsSatisfied();
    }

}
