package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;

/**
 * @author Johan Siebens
 */
public class MethodNotAllowedTest extends AbstractE2ETest {

    private Stopable stopable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            newServer()
                .get("/resource", (r) -> Responses.ok(r.method.name()))
                .delete("/resource", (r) -> Responses.ok(r.method.name()))
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
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
