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

import org.nosceon.titanite.sse.EventBroadcast;
import twitter4j.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Johan Siebens
 */
class TweetEventSource extends EventBroadcast {

    private static final double[][] RANGE = new double[][]{{-180, -90}, {180, 90}};

    private TwitterStream stream;

    public TweetEventSource() {
    }

    @Override
    protected void onSubscribe(String id) {
        if (stream == null) {
            stream = new TwitterStreamFactory().getInstance();
            stream.addListener(new StatusAdapter() {

                @Override
                public void onStatus(Status status) {
                    GeoLocation location = status.getGeoLocation();
                    if (location != null) {
                        send("{ \"lat\" : " + location.getLatitude() + ", \"lng\" : " + location.getLongitude() + " }");
                    }
                }

            });
            stream.filter(new FilterQuery().locations(RANGE));
        }
    }

    @Override
    protected void onUnsubscribe(String id) {
        if (nrOfSubscribers() == 0) {
            stream.shutdown();
            stream = null;
        }
    }

}
