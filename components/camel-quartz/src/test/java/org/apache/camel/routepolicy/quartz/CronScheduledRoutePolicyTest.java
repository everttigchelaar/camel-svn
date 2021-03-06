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

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.ServiceHelper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 
 */
public class CronScheduledRoutePolicyTest extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(CronScheduledRoutePolicyTest.class);
    
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
    public void testScheduledStartRoutePolicy() throws Exception {

        MockEndpoint success = (MockEndpoint) context.getEndpoint("mock:success");        
        
        success.expectedMessageCount(1);
        
        context.getComponent("quartz", QuartzComponent.class).setPropertiesFile("org/apache/camel/routepolicy/quartz/myquartz.properties");
        context.getComponent("quartz", QuartzComponent.class).start();
        context.addRoutes(new RouteBuilder() {
            public void configure() {    
                CronScheduledRoutePolicy policy = new CronScheduledRoutePolicy();
                policy.setRouteStartTime("*/3 * * * * ?");
                
                from("direct:start")
                    .routeId("test")
                    .routePolicy(policy)
                    .to("mock:success");
            }
        });
        context.start();
        context.stopRoute("test", 0, TimeUnit.MILLISECONDS);
        
        Thread.sleep(4000);
        assertTrue(context.getRouteStatus("test") == ServiceStatus.Started);
        template.sendBody("direct:start", "Ready or not, Here, I come");

        context.getComponent("quartz", QuartzComponent.class).stop();
        success.assertIsSatisfied();
    }

    @Test
    public void testScheduledStopRoutePolicy() throws Exception {
        boolean consumerStopped = false;
        
        context.getComponent("quartz", QuartzComponent.class).setPropertiesFile("org/apache/camel/routepolicy/quartz/myquartz.properties");
        context.getComponent("quartz", QuartzComponent.class).start();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                CronScheduledRoutePolicy policy = new CronScheduledRoutePolicy();
                policy.setRouteStopTime("*/3 * * * * ?");
                policy.setRouteStopGracePeriod(0);
                policy.setTimeUnit(TimeUnit.MILLISECONDS);
                
                from("direct:start")
                    .routeId("test")
                    .routePolicy(policy)
                    .to("mock:unreachable");
            }
        });
        context.start();
        
        Thread.sleep(4000);

        assertTrue(context.getRouteStatus("test") == ServiceStatus.Stopped);
        
        try {
            template.sendBody("direct:start", "Ready or not, Here, I come");
        } catch (CamelExecutionException e) {
            consumerStopped = true;
        }    
        assertTrue(consumerStopped);
        context.getComponent("quartz", QuartzComponent.class).stop();
    } 
    
    @Test
    public void testScheduledSuspendRoutePolicy() throws Exception {
        boolean consumerSuspended = false;
  
        context.getComponent("quartz", QuartzComponent.class).setPropertiesFile("org/apache/camel/routepolicy/quartz/myquartz.properties");
        context.getComponent("quartz", QuartzComponent.class).start();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                CronScheduledRoutePolicy policy = new CronScheduledRoutePolicy();
                policy.setRouteSuspendTime("*/3 * * * * ?");
                
                from("direct:start")
                    .routeId("test")
                    .routePolicy(policy)
                    .to("mock:unreachable");
            } 
        });
        context.start();
        
        Thread.sleep(4000);
        try {
            template.sendBody("direct:start", "Ready or not, Here, I come");
        } catch (CamelExecutionException e) {
            consumerSuspended = true;
        }        
        assertTrue(consumerSuspended);
        context.getComponent("quartz", QuartzComponent.class).stop();
    }    
    
    @Test
    public void testScheduledResumeRoutePolicy() throws Exception {
  
        MockEndpoint success = (MockEndpoint) context.getEndpoint("mock:success");        
        
        success.expectedMessageCount(1);
        
        context.getComponent("quartz", QuartzComponent.class).setPropertiesFile("org/apache/camel/routepolicy/quartz/myquartz.properties");
        context.getComponent("quartz", QuartzComponent.class).start();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                CronScheduledRoutePolicy policy = new CronScheduledRoutePolicy();
                policy.setRouteResumeTime("*/3 * * * * ?");
                
                from("direct:start")
                    .routeId("test")
                    .routePolicy(policy)
                    .to("mock:success");
            } 
        });
        context.start();
        ServiceHelper.suspendService(context.getRoute("test").getConsumer());
        try {
            template.sendBody("direct:start", "Ready or not, Here, I come");
        } catch (CamelExecutionException e) {
            LOG.debug("Consumer successfully suspended");
        } 
        
        Thread.sleep(4000);
        template.sendBody("direct:start", "Ready or not, Here, I come");
        
        context.getComponent("quartz", QuartzComponent.class).stop();
        success.assertIsSatisfied();
    }  

}
