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
public class FiltersTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    private static final Filter<Request, Response, String, String> TEXT = (request, function) -> ok(function.apply(request.method.name().toLowerCase()));

    private static final Filter<String, String, String, String> TO_UPPER = (s, function) -> function.apply(s).toUpperCase();

    public static class TextController extends Routings<String, String> {

        {
            get("/controller", s -> s + " lorem ipsum");
        }

    }

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(TEXT.andThen(TO_UPPER).andThen(new TextController()))
                .post("/resource", TEXT.andThen(TO_UPPER).andThen((s) -> s + " resource"))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("GET LOREM IPSUM")).when().get(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo("POST RESOURCE")).when().post(uri(port, "/resource"));
    }

}
