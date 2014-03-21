package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Johan Siebens
 */
public class ControllersTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    public static class ControllerA extends Controller {

        public ControllerA() {
            get("/a", this::handle);
            post("/a", this::handle);
            put("/a", this::handle);
            delete("/a", this::handle);
            patch("/a", this::handle);
        }

        private CompletableFuture<Response> handle(Request request) {
            return ok().body(request.method.name()).toFuture();
        }

    }

    public static class ControllerB extends Controller {

        {
            get("/b", (r) -> ok().body(r.method.name()).toFuture());
            post("/b", (r) -> ok().body(r.method.name()).toFuture());
            put("/b", (r) -> ok().body(r.method.name()).toFuture());
            delete("/b", (r) -> ok().body(r.method.name()).toFuture());
            patch("/b", (r) -> ok().body(r.method.name()).toFuture());
        }

    }

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(new ControllerA())
                .register(ControllerB.class)
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/a"));

        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/b"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/b"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/b"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/b"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/b"));
    }

}
