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
public class QueryParamsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/a", (r) -> Responses.ok().body(r.queryParams.getString("p").get()).completed())
                .register(GET, "/b", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getShort("p").get())).completed())
                .register(GET, "/c", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getInt("p").get())).completed())
                .register(GET, "/d", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getLong("p").get())).completed())
                .register(GET, "/e", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getFloat("p").get())).completed())
                .register(GET, "/f", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getDouble("p").get())).completed())
                .register(GET, "/g", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getBoolean("p").get())).completed())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().queryParam("p", "apple").expect().statusCode(200).body(equalTo("apple")).when().get(uri(port, "/a"));
        given().queryParam("p", "10").expect().statusCode(200).body(equalTo("10")).when().get(uri(port, "/b"));
        given().queryParam("p", "20").expect().statusCode(200).body(equalTo("20")).when().get(uri(port, "/c"));
        given().queryParam("p", "30").expect().statusCode(200).body(equalTo("30")).when().get(uri(port, "/d"));
        given().queryParam("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().get(uri(port, "/e"));
        given().queryParam("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().get(uri(port, "/f"));
        given().queryParam("p", "true").expect().statusCode(200).body(equalTo("true")).when().get(uri(port, "/g"));
    }

}
