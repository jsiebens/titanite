---
layout: post
title:  "Session and Flash scopes"
---

Since last week, Session and Flash scope support is added in the current **1.0.0-SNAPSHOT** version and will be available in the upcoming **1.0.0** release.  
The implementation is based on the technique used in Play Framework, so it is important to note that Session and Flash data is not stored by the server but serialized, signed and saved in a browser cookie. Although it is rather easy to provide another Session and Flash implementation backed by, for example, Redis.


The Session and Flash scopes are available as request attributes, which are added to Titanite for this new feature but will become very useful for implementing, for example, authentication. Using request attributes, data can be passed from filter to the next filter/handler in the chain. 

### Enable Session and/or Flash

Session and Flash scope are not available by default.  
Before a Session or Flash scope can be used, it must be enabled by registering a SessionFilter and/or a FlashFilter which will read the cookie from the request, set Session and/or Flash as request attribute and will afterwards write this new data as cookie.

{% highlight java %}new HttpServer()
    .setFilter(new SessionFilter("my-secret"), new FlashFilter())
    .start();{% endhighlight %}

**Tip:** static methods `enableSessions()` and `enableFlash()` to create a SessionFilter or a FlashFilter are available in `org.nosceon.titanite.scope.Session` and `org.nosceon.titanite.scope.Flash`  


### Using Session and/or Flash

When Session and/or Flash is enabled, they are available as request attribute with key `Session.ATTRIBUTE_ID` and `Flash.ATTRIBUTE_ID`.  

{% highlight java %}request -> {
    Session session = request.attributes().get(Session.ATTRIBUTE_ID);
    int c = session.getInt("counter", 0);
    session.set("counter", c + 1);
    return ok().text(String.valueOf(c)).toFuture();
}){% endhighlight %}

**Tip:** static methods `session(Request request)` and `flash(Request request)` will hide the use of ATTRIBUTE_ID and give a better api usage

{% highlight java %}import static org.nosceon.titanite.scope.Flash.flash; 
...                
req -> {
    int c = flash(req).getInt("counter", 0);
    flash(req).set("counter", c + 1);
    return ok().text(String.valueOf(c)).toFuture();
}){% endhighlight %}

### Complete example

{% highlight java %}import java.util.Optional;
import java.util.UUID;

import static org.nosceon.titanite.Method.*;
import static org.nosceon.titanite.Titanite.*;
import static org.nosceon.titanite.Titanite.Responses.*;
import static org.nosceon.titanite.scope.Flash.*;
import static org.nosceon.titanite.scope.Session.*;

public class ScopesExample {

    public static void main(String[] args) {

        httpServer()
            .setFilter(
                enableSessions("my-secret"),
                enableFlash()
            )
            .register(GET, "/session", req -> {
                int c = session(req).getInt("counter", 0);
                session(req).set("counter", c + 1);
                return ok().text(String.valueOf(c)).toFuture();
            })
            .register(GET, "/flash", req -> {
                flash(req).set("random", UUID.randomUUID().toString());
                return seeOther("/flashed").toFuture();
            })
            .register(GET, "/flashed", req -> {
                String flashMessage = Optional.ofNullable(flash(req).getString("random")).orElse("N/A");
                return ok().text(flashMessage).toFuture();
            })
            .start();

    }

}{% endhighlight %}