package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Titanite.*;

/**
 * @author Johan Siebens
 */
public class ResourcesTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .notFound(
                    compose(
                        sync(PUBLIC_RESOURCES),
                        sync(WEBJAR_RESOURCES)
                    )
                )
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("hello 1 from public")).when().get(uri(port, "/hello1.txt"));
        given().expect().statusCode(200).body(equalTo("hello 2 from webjars")).when().get(uri(port, "/hello2.txt"));
        given().expect().statusCode(404).when().get(uri(port, "/hello3.txt"));
    }


}
