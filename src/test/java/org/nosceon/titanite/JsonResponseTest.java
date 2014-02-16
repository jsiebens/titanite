package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;

/**
 * @author Johan Siebens
 */
public class JsonResponseTest extends AbstractE2ETest {

    public static class Hello {

        private String name;

        public Hello() {
        }

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/json", (r) -> ok().json(new Hello("world")))
                .register(POST, "/json", (r) -> {
                    Hello hello = r.body.asJson(Hello.class);
                    return ok().json(new Hello(hello.getName().toUpperCase()));
                })
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
            .body(equalTo("{\"name\":\"world\"}"))
            .when().get(uri(port, "/json"));

        given()
            .body("{\"name\":\"world\"}")
            .expect()
            .statusCode(200)
            .body(equalTo("{\"name\":\"WORLD\"}"))
            .when().post(uri(port, "/json"));
    }

}
