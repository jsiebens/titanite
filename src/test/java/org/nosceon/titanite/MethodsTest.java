package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class MethodsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    public static class MyController extends Controller {

        public MyController() {
            get("/controller", this::handle);
            post("/controller", this::handle);
            put("/controller", this::handle);
            delete("/controller", this::handle);
            patch("/controller", this::handle);
        }

        private Response handle(Request request) {
            return ok(request.method.name());
        }

    }

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .get("/resource", (r) -> ok(r.method.name()))
                .post("/resource", (r) -> ok(r.method.name()))
                .put("/resource", (r) -> ok(r.method.name()))
                .delete("/resource", (r) -> ok(r.method.name()))
                .patch("/resource", (r) -> ok(r.method.name()))
                .register(new MyController())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/resource"));

        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/controller"));
    }

}
