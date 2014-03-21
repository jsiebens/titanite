package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletionException;

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class ExceptionsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/a", (r) -> {
                    throw new RuntimeException();
                })
                .register(GET, "/b", (r) -> {
                    throw new CompletionException(new RuntimeException());
                })
                .register(GET, "/c", (r) -> {
                    throw new HttpServerException(Responses.status(503));
                })
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(500).when().get(uri(port, "/a"));
        given().expect().statusCode(500).when().get(uri(port, "/b"));
        given().expect().statusCode(503).when().get(uri(port, "/c"));
    }

}
