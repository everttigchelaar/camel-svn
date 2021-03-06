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
package org.apache.camel.processor.interceptor;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.processor.WrapProcessor;

public class TraceInterceptorFactoryCreatesHandlerTest extends TracingTestBase {

    private class TraceInterceptorFactoryCreatesHandlerTestFactory implements TraceInterceptorFactory {
        private List<StringBuilder> eventMessages;
        private boolean traceAllNodes;

        TraceInterceptorFactoryCreatesHandlerTestFactory(List<StringBuilder> eventMessages, boolean traceAllNodes) {
            this.eventMessages = eventMessages;
            this.traceAllNodes = traceAllNodes;
        }

        public Processor createTraceInterceptor(ProcessorDefinition node, Processor target, TraceFormatter formatter, Tracer tracer) {
            TraceInterceptor interceptor = new TraceInterceptor(node, target, formatter, tracer);

            if (target instanceof WrapProcessor) {
                target = ((WrapProcessor) target).getProcessor();
            }
            while (target instanceof DelegateProcessor) {
                target = ((DelegateProcessor) target).getProcessor();
            }
            if (traceAllNodes || !target.getClass().equals(TraceTestProcessor.class)) {
                TraceHandlerTestHandler traceHandler = new TraceHandlerTestHandler(eventMessages);
                traceHandler.setTraceAllNodes(true);
                interceptor.setTraceHandler(traceHandler);
            }

            return interceptor;
        }
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext contextLocal = super.createCamelContext();

        tracedMessages = new LinkedList<StringBuilder>();

        Tracer tracer = (Tracer) contextLocal.getDefaultTracer();
        tracer.setEnabled(true);
        tracer.setTraceExceptions(true);
        if ("testTracerExceptionInOut".equals(getName())) {
            tracer.setTraceInterceptorFactory(new TraceInterceptorFactoryCreatesHandlerTestFactory(tracedMessages, true));
        } else {
            tracer.setTraceInterceptorFactory(new TraceInterceptorFactoryCreatesHandlerTestFactory(tracedMessages, false));
        }

        return contextLocal;
    }

}
