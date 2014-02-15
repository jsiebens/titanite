package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class StreamingOutputResponseTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .get("/resource", (r) -> ok().stream(o -> {
                    IOUtils.copy(new ByteArrayInputStream(TEXT.getBytes()), o);
                }))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given()
            .expect()
            .header(HttpHeaders.Names.TRANSFER_ENCODING, "chunked")
            .statusCode(200).body(equalTo(TEXT)).when().get(uri(port, "/resource"));
    }

}
