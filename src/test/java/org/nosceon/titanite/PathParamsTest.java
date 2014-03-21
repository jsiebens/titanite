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
public class PathParamsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/a/{p}", (r) -> ok().body(r.pathParams.get("p")).toFuture())
                .register(GET, "/b/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getShort("p"))).toFuture())
                .register(GET, "/c/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getInt("p"))).toFuture())
                .register(GET, "/d/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getLong("p"))).toFuture())
                .register(GET, "/e/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getFloat("p"))).toFuture())
                .register(GET, "/f/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getDouble("p"))).toFuture())
                .register(GET, "/g/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getBoolean("p"))).toFuture())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
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
