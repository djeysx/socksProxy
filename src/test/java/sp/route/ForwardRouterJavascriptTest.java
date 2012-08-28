package sp.route;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.Assert;

import org.junit.Test;

public class ForwardRouterJavascriptTest {

	@Test
	public void testRoute_basic_features() {
		ForwardRouterJavascript routerJs = new ForwardRouterJavascript("src/test/resources/router-test.js");
		routerJs.updateScript();
		{
			String target = routerJs.invokeScript("192.168.1.1", "1234.test.net", 1234);
			Assert.assertEquals("start", target);
		}
		{
			String target = routerJs.invokeScript("111.222.1.1", "1234.test.net", 1234);
			Assert.assertEquals("end", target);
		}
		{
			String target = routerJs.invokeScript("10.10.10.10", "1234.test.net", 1234);
			Assert.assertEquals("none", target);
		}
	}

	
}
