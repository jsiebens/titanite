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
