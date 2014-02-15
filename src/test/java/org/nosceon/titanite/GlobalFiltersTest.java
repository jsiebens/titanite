package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Responses.ok;
import static org.nosceon.titanite.Responses.status;

/**
 * @author Johan Siebens
 */
public class GlobalFiltersTest extends AbstractE2ETest {

    private Stopable stopable;

    private int port;

    private static final SimpleFilter<Request, Response> SECURITY = (r, f) -> {
        String s = r.headers.getString(HttpHeaders.Names.AUTHORIZATION).orElse("");
        if ("admin".equals(s)) {
            return f.apply(r).header("x-titanite-a", "lorem");
        }
        else {
            return status(401);
        }
    };

    private static final SimpleFilter<Request, Response> CONTENT_TYPE_JSON = (r, f) -> {
        String s = r.headers.getString(HttpHeaders.Names.CONTENT_TYPE).orElse("");
        if ("application/json".equals(s)) {
            return f.apply(r).header("x-titanite-b", "ipsum");
        }
        else {
            return status(415);
        }
    };

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            newServer()
                .register(SECURITY)
                .register(CONTENT_TYPE_JSON)
                .get("/resource", (r) -> ok("hello"))
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
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
