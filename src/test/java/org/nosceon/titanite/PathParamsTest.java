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
public class PathParamsTest extends AbstractE2ETest {

    private Stopable stopable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        stopable =
            newServer()
                .get("/a/{p}", (r) -> ok(r.pathParams.getString("p").get()))
                .get("/b/{p}", (r) -> ok(String.valueOf(r.pathParams.getShort("p").get())))
                .get("/c/{p}", (r) -> ok(String.valueOf(r.pathParams.getInt("p").get())))
                .get("/d/{p}", (r) -> ok(String.valueOf(r.pathParams.getLong("p").get())))
                .get("/e/{p}", (r) -> ok(String.valueOf(r.pathParams.getFloat("p").get())))
                .get("/f/{p}", (r) -> ok(String.valueOf(r.pathParams.getDouble("p").get())))
                .get("/g/{p}", (r) -> ok(String.valueOf(r.pathParams.getBoolean("p").get())))
                .start(port);
    }

    @After
    public void tearDown() {
        stopable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("apple")).when().get(uri(port, "/a/apple"));
        given().expect().statusCode(200).body(equalTo("10")).when().get(uri(port, "/b/10"));
        given().expect().statusCode(200).body(equalTo("20")).when().get(uri(port, "/c/20"));
        given().expect().statusCode(200).body(equalTo("30")).when().get(uri(port, "/d/30"));
        given().expect().statusCode(200).body(equalTo("40.0")).when().get(uri(port, "/e/40"));
        given().expect().statusCode(200).body(equalTo("50.0")).when().get(uri(port, "/f/50"));
        given().expect().statusCode(200).body(equalTo("true")).when().get(uri(port, "/g/true"));
    }

}
