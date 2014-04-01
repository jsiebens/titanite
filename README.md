# Titanite

__titanite__ is a small web framework for Java 8, inspired by others like [Sinatra](http://www.sinatrarb.com/) and [Finatra](http://finatra.info/), running on top of [Netty](http://netty.io/)

## examples

```
import static org.nosceon.titanite.Titanite.*;
import static org.nosceon.titanite.Method.*;

public class HelloWorld {

    public static void main(String[] args) {

        httpServer()
            .register(GET, "/hello/{name}", r -> {
                String name = r.pathParams.get("name");
                return ok().text("hello " + name).toFuture();
            })
            .start(8080);

    }

}
```
```
import org.nosceon.titanite.Controller;
import static org.nosceon.titanite.Titanite.httpServer;

public class HelloWorldController extends Controller {

    {
        get("/hello/{name}", req -> {
            String name = req.pathParams.get("name");
            return ok().text("hello " + name.toUpperCase()).toFuture();
        });
    }

    public static void main(String[] args) {
        httpServer()
            .register(HelloWorldController.class)
            .start(8080);
    }

}
```