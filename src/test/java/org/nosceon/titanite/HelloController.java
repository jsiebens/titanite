package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public class HelloController extends Controller {

    {
        get("/hello/{name}", (request) -> {
            String name = request.pathParams.getString("name").orElse("default user");
            return ok("hello " + name);
        });
    }

    public static void main(String[] args) {
        new HttpServer()
            .register(new HelloController())
            .start(8080);
    }

}
