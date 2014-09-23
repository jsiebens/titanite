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
package org.nosceon.titanite;

import com.jayway.restassured.RestAssured;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.ServerSocket;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * @author Johan Siebens
 */
@RunWith(value = Parameterized.class)
public abstract class AbstractMultiE2ETest {

    private static SelfSignedCertificate ssc;

    static {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Parameterized.Parameters(name = "secure: {0}")
    public static Iterable<Object[]> data1() {
        return Arrays.asList(new Object[][]{
            {false},
            {true}
        });
    }

    private int port;

    private Shutdownable shutdownable;

    private final boolean secure;

    protected AbstractMultiE2ETest(boolean secure) {
        this.secure = secure;
    }

    @BeforeClass
    public static void setupSelfSignedCertificate() throws CertificateException {
        ssc = new SelfSignedCertificate();
    }

    @AfterClass
    public static void clearSelfSignedCertificate() {
        ssc.delete();
    }

    @Before
    public void setUpHttpServer() throws Exception {
        this.port = findFreePort();
        this.shutdownable = configureAndStartHttpServer(newServer(port));
    }

    @After
    public void tearDownHttpServer() {
        this.shutdownable.stop();
    }

    protected abstract Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception;

    protected String uri(String path) {
        return (secure ? "https" : "http") + "://localhost:" + port + path;
    }

    protected String ws(String path) {
        return (secure ? "wss" : "ws") + "://localhost:" + port + path;
    }

    private int findFreePort() {
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

    private HttpServer newServer(int port) {
        ImmutableSettings.Builder builder = ImmutableSettings.newSettings();
        if (secure) {
            builder.addHttpsConnector(port, ssc.certificate(), ssc.privateKey());
        }
        else {
            builder.addHttpConnector(port);
        }
        return new HttpServer(builder.setIoWorkerCount(2).setMaxRequestSize(maxRequestSize()).build());
    }

    protected long maxRequestSize() {
        return Settings.DEFAULT_MAX_REQUEST_SIZE;
    }

}
