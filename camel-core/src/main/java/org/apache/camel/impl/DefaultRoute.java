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
package org.apache.camel.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.spi.RouteContext;

/**
 * Default implementation of {@link Route}.
 *
 * @version 
 */
public abstract class DefaultRoute extends ServiceSupport implements Route {

    private final Endpoint endpoint;
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private final List<Service> services = new ArrayList<Service>();
    private final RouteContext routeContext;

    public DefaultRoute(RouteContext routeContext, Endpoint endpoint) {
        this.routeContext = routeContext;
        this.endpoint = endpoint;
    }

    public DefaultRoute(RouteContext routeContext, Endpoint endpoint, Service... services) {
        this(routeContext, endpoint);
        for (Service service : services) {
            addService(service);
        }
    }

    @Override
    public String toString() {
        return "Route " + getId();
    }

    public String getId() {
        return (String) properties.get(Route.ID_PROPERTY);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public RouteContext getRouteContext() {
        return routeContext;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void onStartingServices(List<Service> services) throws Exception {
        addServices(services);
    }

    public List<Service> getServices() {
        return services;
    }

    public void addService(Service service) {
        getServices().add(service);
    }

    /**
     * Strategy method to allow derived classes to lazily load services for the route
     */
    protected void addServices(List<Service> services) throws Exception {
    }

    protected void doStart() throws Exception {
        // noop
    }

    protected void doStop() throws Exception {
        // clear services when stopping
        services.clear();
    }

}
