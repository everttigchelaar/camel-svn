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
package org.apache.camel.itest.security;

import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;

import org.apache.camel.component.spring.security.DefaultAuthenticationAdapter;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;


public class MyAuthenticationAdapter extends DefaultAuthenticationAdapter {
    
    protected Authentication convertToAuthentication(Subject subject) {
        Authentication answer = null;
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof WSUsernameTokenPrincipal) {
                WSUsernameTokenPrincipal ut = (WSUsernameTokenPrincipal) principal;
                answer = new UsernamePasswordAuthenticationToken(ut.getName(), ut.getPassword());
                break;
            }
        }
        return answer;
    }

}
