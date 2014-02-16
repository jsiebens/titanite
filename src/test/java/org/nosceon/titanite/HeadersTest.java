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
public class HeadersTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/a", (r) -> ok().body(r.headers.getString("p").get()).header("m", r.headers.getString("p").get()))
                .register(GET, "/b", (r) -> ok().body(String.valueOf(r.headers.getShort("p").get())))
                .register(GET, "/c", (r) -> ok().body(String.valueOf(r.headers.getInt("p").get())))
                .register(GET, "/d", (r) -> ok().body(String.valueOf(r.headers.getLong("p").get())))
                .register(GET, "/e", (r) -> ok().body(String.valueOf(r.headers.getFloat("p").get())))
                .register(GET, "/f", (r) -> ok().body(String.valueOf(r.headers.getDouble("p").get())))
                .register(GET, "/g", (r) -> ok().body(String.valueOf(r.headers.getBoolean("p").get())))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
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
