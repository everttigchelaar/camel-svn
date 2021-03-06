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
package org.apache.camel.component.mina;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.camel.CamelContext;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * @version 
 */
public class MinaUdpProtocolCodecFactory implements ProtocolCodecFactory {

    private final Charset charset;
    private final CamelContext context;

    public MinaUdpProtocolCodecFactory(CamelContext context, Charset charset) {
        this.context = context;
        this.charset = charset;
    }

    public ProtocolEncoder getEncoder() throws Exception {
        return new ProtocolEncoder() {
            private CharsetEncoder encoder;

            public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
                if (encoder == null) {
                    encoder = charset.newEncoder();
                }
                ByteBuffer buf = toByteBuffer(message, encoder);
                buf.flip();
                out.write(buf);
            }

            public void dispose(IoSession session) throws Exception {
                // do nothing
            }
        };
    }

    public ProtocolDecoder getDecoder() throws Exception {
        return new ProtocolDecoder() {
            public void decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
                // convert to bytes to write, we can not pass in the byte buffer as it could be sent to
                // multiple mina sessions so we must convert it to bytes
                byte[] bytes = context.getTypeConverter().convertTo(byte[].class, in);
                out.write(bytes);
            }

            public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
                // do nothing
            }

            public void dispose(IoSession session) throws Exception {
                // do nothing
            }
        };
    }

    private ByteBuffer toByteBuffer(Object message, CharsetEncoder encoder)
        throws CharacterCodingException, NoTypeConversionAvailableException {
        String value = context.getTypeConverter().convertTo(String.class, message);
        if (value != null) {
            ByteBuffer answer = ByteBuffer.allocate(value.length()).setAutoExpand(false);
            answer.putString(value, encoder);
            return answer;
        }

        // failback to use a byte buffer converter
        return context.getTypeConverter().mandatoryConvertTo(ByteBuffer.class, message);
    }

}
