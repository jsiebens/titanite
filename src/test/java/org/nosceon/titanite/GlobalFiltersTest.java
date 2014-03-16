package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class GlobalFiltersTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    private static final SimpleFilter<Request, CompletableFuture<Response>> SECURITY = (req, f) -> {
        String s = req.headers.getString(HttpHeaders.Names.AUTHORIZATION).orElse("");
        if ("admin".equals(s)) {
            return f.apply(req).thenCompose(resp -> resp.header("x-titanite-a", "lorem").completed());
        }
        else {
            return status(401).completed();
        }
    };

    private static final SimpleFilter<Request, CompletableFuture<Response>> CONTENT_TYPE_JSON = (req, f) -> {
        String s = req.headers.getString(HttpHeaders.Names.CONTENT_TYPE).orElse("");
        if ("application/json".equals(s)) {
            return f.apply(req).thenCompose(resp -> resp.header("x-titanite-b", "ipsum").completed());
        }
        else {
            return status(415).completed();
        }
    };

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(SECURITY)
                .register(CONTENT_TYPE_JSON)
                .register(GET, "/resource", (r) -> ok().body("hello").completed())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(401).when().get(uri(port, "/resource"));

        given()
            .header(HttpHeaders.Names.AUTHORIZATION, "admin")
            .expect()
            .header("x-titanite-a", "lorem")
            .statusCode(415).when().get(uri(port, "/resource"));

        given()
            .header(HttpHeaders.Names.AUTHORIZATION, "admin")
            .header(HttpHeaders.Names.CONTENT_TYPE, "application/json")
            .expect()
            .header("x-titanite-a", "lorem")
            .header("x-titanite-b", "ipsum")
            .statusCode(200).when().get(uri(port, "/resource"));
    }

}
