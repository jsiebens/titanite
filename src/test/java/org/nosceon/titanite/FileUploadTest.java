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
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Johan Siebens
 */
public class FileUploadTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private Shutdownable shutdownable;

    private int port;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File uploadFolder;

    @Before
    public void setUp() throws IOException {
        uploadFolder = temporaryFolder.newFolder("upload");

        port = findFreePort();
        shutdownable =
            newServer()
                .post("/post", (r) -> {
                    r.body.asForm().getMultiPart("file").ifPresent(mp -> mp.renameTo(new File(uploadFolder, mp.filename())));
                    return ok().body(r.body.asForm().getString("lorem").get());
                })
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() throws IOException {
        File file = temporaryFolder.newFile("hello.txt");
        FileUtils.writeStringToFile(file, TEXT);

        given()
            .formParam("lorem", "ipsum")
            .multiPart("file", file).expect().statusCode(200).body(equalTo("ipsum")).when().post(uri(port, "/post"));

        File uploadedFile = new File(uploadFolder, "hello.txt");

        assertThat(uploadedFile.exists(), is(true));
        assertThat(readFileToString(uploadedFile), equalTo(TEXT));
    }

}
