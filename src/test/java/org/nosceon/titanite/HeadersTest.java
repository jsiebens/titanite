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
public class HeadersTest extends AbstractE2ETest {

    private Stopable stopable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            newServer()
                .get("/a", (r) -> ok(r.headers.getString("p").get()).header("m", r.headers.getString("p").get()))
                .get("/b", (r) -> ok(String.valueOf(r.headers.getShort("p").get())))
                .get("/c", (r) -> ok(String.valueOf(r.headers.getInt("p").get())))
                .get("/d", (r) -> ok(String.valueOf(r.headers.getLong("p").get())))
                .get("/e", (r) -> ok(String.valueOf(r.headers.getFloat("p").get())))
                .get("/f", (r) -> ok(String.valueOf(r.headers.getDouble("p").get())))
                .get("/g", (r) -> ok(String.valueOf(r.headers.getBoolean("p").get())))
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
    }

    @Test
    public void test() {
        given().header("p", "apple").expect().statusCode(200).header("m", "apple").body(equalTo("apple")).when().get(uri(port, "/a"));
        given().header("p", "10").expect().statusCode(200).body(equalTo("10")).when().get(uri(port, "/b"));
        given().header("p", "20").expect().statusCode(200).body(equalTo("20")).when().get(uri(port, "/c"));
        given().header("p", "30").expect().statusCode(200).body(equalTo("30")).when().get(uri(port, "/d"));
        given().header("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().get(uri(port, "/e"));
        given().header("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().get(uri(port, "/f"));
        given().header("p", "true").expect().statusCode(200).body(equalTo("true")).when().get(uri(port, "/g"));
    }

}
