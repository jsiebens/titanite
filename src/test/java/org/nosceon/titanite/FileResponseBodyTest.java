package org.nosceon.titanite;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class FileResponseBodyTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private Shutdownable shutdownable;

    private int port;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        File file = temporaryFolder.newFile("hello.txt");
        FileUtils.writeStringToFile(file, TEXT);

        port = findFreePort();
        shutdownable =
            newServer()
                .get("/file", (r) -> ok().file(file))
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() throws IOException {
        given()
            .expect().statusCode(200).body(equalTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")).when().get(uri(port, "/file"));
    }

}
