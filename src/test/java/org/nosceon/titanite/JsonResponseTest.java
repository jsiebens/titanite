package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class JsonResponseTest extends AbstractE2ETest {

    public static class Hello {

        private final String name;

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .get("/json", (r) -> ok().json(new Hello("world")))
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
            .statusCode(200)
            .header(HttpHeaders.Names.CONTENT_TYPE, equalTo("application/json"))
            .body(equalTo("{\"name\":\"world\"}")).when().get(uri(port, "/json"));
    }

}
