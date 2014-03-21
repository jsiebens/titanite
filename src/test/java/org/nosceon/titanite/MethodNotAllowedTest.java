package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Method.DELETE;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class MethodNotAllowedTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/resource", (r) -> Responses.ok().body(r.method.name()).toFuture())
                .register(DELETE, "/resource", (r) -> Responses.ok().body(r.method.name()).toFuture())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).when().get(uri(port, "/resource"));
        given().expect().statusCode(405).when().post(uri(port, "/resource"));
        given().expect().statusCode(405).when().put(uri(port, "/resource"));
        given().expect().statusCode(200).when().delete(uri(port, "/resource"));
        given().expect().statusCode(405).when().patch(uri(port, "/resource"));
    }

}
