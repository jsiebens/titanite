package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class CookiesTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/a", (r) -> ok().body(r.cookies.getString("p").orElse("default value")))
                .register(GET, "/cookie", (r) -> ok().cookie(new Cookie("fruit", "apple").version(2).comment("my comment").path("/cookie")))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().cookie("p", "apple").expect().statusCode(200).body(equalTo("apple")).when().get(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo("default value")).when().get(uri(port, "/a"));
        given().expect().statusCode(200).cookie("fruit", "apple").when().get(uri(port, "/cookie"));
    }

}
