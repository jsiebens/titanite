/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import java.net.ServerSocket;

/**
 * @author Johan Siebens
 */
public class AbstractE2ETest extends Responses {

    protected HttpServer newServer() {
        return new HttpServer(2);
    }

    protected int findFreePort() {
        int port;
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        }
        catch (Exception e) {
            port = -1;
        }
        return port;
    }

    protected String uri(int port, String path) {
        return "http://localhost:" + port + path;
    }

}
