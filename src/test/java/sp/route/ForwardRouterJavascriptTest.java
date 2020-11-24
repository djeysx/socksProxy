package sp.route;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

@Ignore
public class ForwardRouterJavascriptTest {

    ForwardRouterJavascript routerJs = new ForwardRouterJavascript("src/test/resources/router-test.js");

    @Test
    public void testRoute_basic_features_start() {
        routerJs.updateScript();
        String target = routerJs.invokeScript("192.168.1.1", "1234.test.net", 1234);
        Assert.assertEquals("start", target);
    }

    @Test
    public void testRoute_basic_features_end() {
        ForwardRouterJavascript routerJs = new ForwardRouterJavascript("src/test/resources/router-test.js");
        routerJs.updateScript();
        String target = routerJs.invokeScript("111.222.1.1", "1234.test.net", 1234);
        Assert.assertEquals("end", target);
    }

    @Test
    public void testRoute_basic_features_none() {
        ForwardRouterJavascript routerJs = new ForwardRouterJavascript("src/test/resources/router-test.js");
        routerJs.updateScript();
        String target = routerJs.invokeScript("10.10.10.10", "1234.test.net", 1234);
        Assert.assertEquals("none", target);
    }

}
