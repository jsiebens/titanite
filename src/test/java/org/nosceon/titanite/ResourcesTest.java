package org.nosceon.titanite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Chain.newChain;

import static org.nosceon.titanite.service.ResourceService.*;

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
                .notFound(newChain(PUBLIC_RESOURCES).fallbackTo(WEBJAR_RESOURCES))
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
