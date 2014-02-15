# titanite

__titanite__ is a small web framework for Java 8, inspired by others like [Sinatra](http://www.sinatrarb.com/) and [Finatra](http://finatra.info/), running on top of [Netty](http://netty.io/)

```
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
```