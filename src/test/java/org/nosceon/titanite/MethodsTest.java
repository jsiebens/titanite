package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ServerSocket;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class MethodsTest extends AbstractE2ETest {

    private Stopable stopable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            newServer()
                .get("/resource", (r) -> ok(r.method.name()))
                .post("/resource", (r) -> ok(r.method.name()))
                .put("/resource", (r) -> ok(r.method.name()))
                .delete("/resource", (r) -> ok(r.method.name()))
                .patch("/resource", (r) -> ok(r.method.name()))
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/resource"));
    }

}
