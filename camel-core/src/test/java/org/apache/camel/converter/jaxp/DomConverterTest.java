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
package org.apache.camel.converter.jaxp;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.util.ObjectHelper;

/**
 * @version 
 */
public class DomConverterTest extends ContextTestSupport {

    public void testDomConverterToString() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world!</hello>");

        String s = new DomConverter().toString(document.getChildNodes(), null);
        assertEquals("<hello>world!</hello>", s);
    }

    public void testDomConverterToBytes() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world!</hello>");

        byte[] bytes = new DomConverter().toByteArray(document.getChildNodes(), null);
        assertTrue("Should be equal", ObjectHelper.equalByteArray("<hello>world!</hello>".getBytes(), bytes));
    }

    public void testDomConverterToInteger() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>47</hello>");

        Integer number = new DomConverter().toInteger(document.getChildNodes());
        assertEquals(47, number.intValue());
    }

    public void testDomConverterToLong() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>47</hello>");

        Long number = new DomConverter().toLong(document.getChildNodes());
        assertEquals(47L, number.longValue());
    }

    public void testDomConverterToList() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<foo><hello>Hello World</hello><bye>Bye Camel</bye></foo>");

        List list = DomConverter.toList(document.getElementsByTagName("foo"));
        assertEquals(1, list.size());

        NodeList nl = assertIsInstanceOf(NodeList.class, list.get(0));
        List sub = DomConverter.toList(nl);
        assertEquals(2, sub.size());

        assertEquals("<hello>Hello World</hello>", new DomConverter().toString((NodeList) sub.get(0), null));
        assertEquals("<bye>Bye Camel</bye>", new DomConverter().toString((NodeList) sub.get(1), null));
    }

    public void testDomConverterToInputStream() throws Exception {
        Document document = context.getTypeConverter().convertTo(Document.class, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world!</hello>");

        InputStream is = new DomConverter().toInputStream(document.getChildNodes(), null);
        assertEquals("<hello>world!</hello>", context.getTypeConverter().convertTo(String.class, is));
    }

}
