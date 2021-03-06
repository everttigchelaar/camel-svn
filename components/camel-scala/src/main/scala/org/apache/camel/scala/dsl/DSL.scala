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
package org.apache.camel
package scala
package dsl 

import org.apache.camel.model.DataFormatDefinition
import reflect.Manifest
import java.util.Comparator;
import org.apache.camel.processor.aggregate.AggregationStrategy

import org.apache.camel.spi.Policy

/**
 * Defines the 'keywords' in our Scala DSL
 */
trait DSL {
  
  def aggregate(expression: Exchange => Any, strategy: AggregationStrategy) : SAggregateDefinition
  def as[Target](toType: Class[Target]) : DSL
  def attempt : STryDefinition
  def bean(bean: Any) : DSL
  def choice : SChoiceDefinition
  def delay(delay: Period) : SDelayDefinition
  def enrich(uri:String, strategy: AggregationStrategy) : DSL
  def filter(predicate: Exchange => Any) : SFilterDefinition
  def handle[E](block: => Unit)(implicit manifest: Manifest[E]) : SOnExceptionDefinition
  def idempotentconsumer(expression: Exchange => Any): SIdempotentConsumerDefinition
  def inOnly(): DSL with Block
  def inOut(): DSL with Block
  def loadbalance : SLoadBalanceDefinition

  def log(message: String) : DSL
  def log(level: LoggingLevel, message: String) : DSL
  def log(level: LoggingLevel, logName: String, message: String) : DSL

  def loop(expression: Exchange => Any) : SLoopDefinition
  def marshal(format : DataFormatDefinition) : DSL
  def multicast : SMulticastDefinition
  def otherwise : DSL with Block
  def onCompletion : SOnCompletionDefinition
  def onCompletion(predicate: Exchange => Boolean) : SOnCompletionDefinition
  def onCompletion(config: Config[SOnCompletionDefinition]) : SOnCompletionDefinition
  def pipeline : SPipelineDefinition
  def policy(policy: Policy) : DSL

  def pollEnrich(uri: String, strategy: AggregationStrategy = null, timeout: Long = 0) : DSL

  def process(function: Exchange => Unit) : DSL
  def process(processor: Processor) : DSL
  def recipients(expression: Exchange => Any) : DSL
  def resequence(expression: Exchange => Any) : SResequenceDefinition
  def rollback : DSL

  def routingSlip(header: String) : DSL
  def routingSlip(header: String, separator: String) : DSL
  def routingSlip(expression: Exchange => Any) : DSL

  def dynamicRouter(expression: Exchange => Any) : DSL

  def setbody(expression: Exchange => Any) : DSL
  def setfaultbody(expression: Exchange => Any) : DSL
  def setheader(header: String, expression: Exchange => Any) : DSL

  def sort[T](expression: Exchange => Any, comparator: Comparator[T] = null) : DSL
  def split(expression: Exchange => Any) : SSplitDefinition

  def stop : DSL

  def threads : SThreadsDefinition

  def throttle(frequency: Frequency) : SThrottleDefinition
  def throwException(exception: Exception) : DSL

  def to(uris: String*) : DSL

  def transacted : DSL
  def transacted(ref: String) : DSL

  def transform(expression: Exchange => Any) : DSL

  def unmarshal(format: DataFormatDefinition) : DSL

  def validate(expression: Exchange => Any) : DSL

  def when(filter: Exchange => Any) : DSL with Block
  
  def wiretap(uri: String) : DSL
  def wiretap(uri: String, expression: Exchange => Any) : DSL
  
  def -->(uris: String*) : DSL
}
