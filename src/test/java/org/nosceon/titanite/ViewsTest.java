package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class ViewsTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    public static class HelloView extends View {

        private final String name;

        public HelloView(String template, String name) {
            super(template);
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
                .get("/hello1", (r) -> ok().view(new HelloView("hello", "world")))
                .get("/hello2", (r) -> ok().view(new HelloView("hello.mustache", "world")))
                .get("/hello3", (r) -> ok().view(new HelloView("/hello", "world")))
                .get("/unavailable", (r) -> ok().view(new HelloView("unavailable", "world")))
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
            .body(equalTo("Hello world")).when().get(uri(port, "/hello1"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri(port, "/hello2"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri(port, "/hello3"));
        given()
            .expect()
            .statusCode(500)
            .when().get(uri(port, "/unavailable"));
    }

}
