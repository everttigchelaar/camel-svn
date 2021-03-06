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
package org.apache.camel.component.cxf.mtom;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.XPathUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;

/**
* Unit test for exercising MTOM feature of a CxfConsumer in PAYLOAD mode
* 
* @version 
*/
@ContextConfiguration
public class CxfMtomConsumerPayloadModeTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected CamelContext context;
    
    @Test
    public void testConsumer() throws Exception {
        context.createProducerTemplate().send("cxf:bean:consumerEndpoint", new Processor() {

            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                assertEquals("Get a wrong Content-Type header", "application/xop+xml", exchange.getIn().getHeader("Content-Type"));
                List<Element> elements = new ArrayList<Element>();
                elements.add(DOMUtils.readXml(new StringReader(getRequestMessage())).getDocumentElement());
                CxfPayload<SoapHeader> body = new CxfPayload<SoapHeader>(new ArrayList<SoapHeader>(),
                    elements);
                exchange.getIn().setBody(body);
                exchange.getIn().addAttachment(MtomTestHelper.REQ_PHOTO_CID, 
                    new DataHandler(new ByteArrayDataSource(MtomTestHelper.REQ_PHOTO_DATA, "application/octet-stream")));

                exchange.getIn().addAttachment(MtomTestHelper.REQ_IMAGE_CID, 
                    new DataHandler(new ByteArrayDataSource(MtomTestHelper.requestJpeg, "image/jpeg")));
            }
        });
    }

    // START SNIPPET: consumer
    public static class MyProcessor implements Processor {

        @SuppressWarnings("unchecked")
        public void process(Exchange exchange) throws Exception {
            CxfPayload<SoapHeader> in = exchange.getIn().getBody(CxfPayload.class);
            
            // verify request
            Assert.assertEquals(1, in.getBody().size());
            
            Map<String, String> ns = new HashMap<String, String>();
            ns.put("ns", MtomTestHelper.SERVICE_TYPES_NS);
            ns.put("xop", MtomTestHelper.XOP_NS);

            XPathUtils xu = new XPathUtils(ns);
            Element ele = (Element)xu.getValue("//ns:Detail/ns:photo/xop:Include", in.getBody().get(0),
                                               XPathConstants.NODE);
            String photoId = ele.getAttribute("href").substring(4); // skip "cid:"
            Assert.assertEquals(MtomTestHelper.REQ_PHOTO_CID, photoId);

            ele = (Element)xu.getValue("//ns:Detail/ns:image/xop:Include", in.getBody().get(0),
                                               XPathConstants.NODE);
            String imageId = ele.getAttribute("href").substring(4); // skip "cid:"
            Assert.assertEquals(MtomTestHelper.REQ_IMAGE_CID, imageId);

            DataHandler dr = exchange.getIn().getAttachment(photoId);
            Assert.assertEquals("application/octet-stream", dr.getContentType());
            MtomTestHelper.assertEquals(MtomTestHelper.REQ_PHOTO_DATA, IOUtils.readBytesFromStream(dr.getInputStream()));
       
            dr = exchange.getIn().getAttachment(imageId);
            Assert.assertEquals("image/jpeg", dr.getContentType());
            MtomTestHelper.assertEquals(MtomTestHelper.requestJpeg, IOUtils.readBytesFromStream(dr.getInputStream()));

            // create response
            List<Element> elements = new ArrayList<Element>();
            elements.add(DOMUtils.readXml(new StringReader(MtomTestHelper.RESP_MESSAGE)).getDocumentElement());
            CxfPayload<SoapHeader> body = new CxfPayload<SoapHeader>(new ArrayList<SoapHeader>(),
                elements);
            exchange.getOut().setBody(body);
            exchange.getOut().addAttachment(MtomTestHelper.RESP_PHOTO_CID, 
                new DataHandler(new ByteArrayDataSource(MtomTestHelper.RESP_PHOTO_DATA, "application/octet-stream")));

            exchange.getOut().addAttachment(MtomTestHelper.RESP_IMAGE_CID, 
                new DataHandler(new ByteArrayDataSource(MtomTestHelper.responseJpeg, "image/jpeg")));

        }
    }
    // END SNIPPET: consumer

    protected String getRequestMessage() {
        return MtomTestHelper.REQ_MESSAGE;
    }

}
