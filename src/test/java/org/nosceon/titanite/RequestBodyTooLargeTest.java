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
public class RequestBodyTooLargeTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private Stopable stopable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            new HttpServer(2, 5, 5)
                .post("/post", (r) -> Responses.ok())
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
    }

    @Test
    public void test() {
        given().body(TEXT).expect().statusCode(413).body(equalTo("")).when().post(uri(port, "/post"));
    }

}
