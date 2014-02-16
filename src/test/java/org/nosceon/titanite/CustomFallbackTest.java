package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Johan Siebens
 */
public class CustomFallbackTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .get("/a", (r) -> ok().text("ok"))
                .notFound((r) -> ok().text("notFound"))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("ok")).when().get(uri(port, "/a"));
        given().expect().statusCode(200).body(equalTo("notFound")).when().get(uri(port, "/b"));
    }

}
