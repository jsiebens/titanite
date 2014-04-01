package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletionException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.errors;

/**
 * @author Johan Siebens
 */
public class ExceptionsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    public static class InternalException extends RuntimeException {

    }

    public static class InternalSub1Exception extends InternalException {

    }

    public static class InternalSub2Exception extends InternalException {

    }

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .setFilter(
                    errors()
                        .match(InternalException.class, (r, e) -> ok().text("Internal"))
                        .match(InternalSub1Exception.class, () -> ok().text("Internal Sub1"))
                )
                .register(GET, "/a", (r) -> {
                    throw new RuntimeException();
                })
                .register(GET, "/b", (r) -> {
                    throw new CompletionException(new RuntimeException());
                })
                .register(GET, "/c", (r) -> {
                    throw new HttpServerException(Responses.status(503));
                })
                .register(GET, "/d", (r) -> {
                    throw new CompletionException(new InternalSub1Exception());
                })
                .register(GET, "/e", (r) -> {
                    throw new InternalSub2Exception();
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

        given().expect().statusCode(200).body(equalTo("Internal Sub1")).when().get(uri(port, "/d"));
        given().expect().statusCode(200).body(equalTo("Internal")).when().get(uri(port, "/e"));
    }

}
