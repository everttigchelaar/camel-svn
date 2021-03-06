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
package org.apache.camel.processor;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The processor which implements the
 * <a href="http://camel.apache.org/message-filter.html">Message Filter</a> EIP pattern.
 *
 * @version 
 */
public class FilterProcessor extends DelegateAsyncProcessor implements Traceable {
    private static final Logger LOG = LoggerFactory.getLogger(FilterProcessor.class);
    private final Predicate predicate;

    public FilterProcessor(Predicate predicate, Processor processor) {
        super(processor);
        this.predicate = predicate;
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        boolean matches = false;
        try {
            matches = predicate.matches(exchange);
        } catch (Throwable e) {
            exchange.setException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Filter matches: " + matches + " for exchange: " + exchange);
        }

        // set property whether the filter matches or not
        exchange.setProperty(Exchange.FILTER_MATCHED, matches);

        if (matches) {
            return super.process(exchange, callback);
        } else {
            callback.done(true);
            return true;
        }
    }

    @Override
    public String toString() {
        return "Filter[if: " + predicate + " do: " + getProcessor() + "]";
    }

    public String getTraceLabel() {
        return "filter[if: " + predicate + "]";
    }

    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        ServiceHelper.startService(predicate);
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopService(predicate);
        super.doStop();
    }
}
