/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite.examples;

import twitter4j.TwitterFactory;

import static org.nosceon.titanite.HttpServer.httpServer;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.service.ResourceService.publicResourceService;


/**
 * @author Johan Siebens
 */
public class TweetMap {

    public static void main(String[] args) throws Exception {
        new TwitterFactory().getInstance().verifyCredentials();

        TweetEventSource eventSource = new TweetEventSource();

        httpServer()
            .register(GET, "/data", req -> eventSource.newSubscription())
            .register(GET, "/*path", publicResourceService())
            .start();
    }

}
