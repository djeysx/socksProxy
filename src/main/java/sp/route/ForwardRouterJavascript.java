package sp.route;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import sp.forward.SocketForward;
import sp.forward.SocketForwardChain;
import sp.forward.SocketForwardDirect;
import sp.forward.SocketForwardHttpConnect;
import sp.forward.SocketForwardSocks4;

public class ForwardRouterJavascript extends ForwardRouter {
	private static final Logger log = Logger.getLogger(ForwardRouterJavascript.class);
	private static final String JS_ROUTE_METHOD = "FindProxyRoute";
	private static final String LIB_PAC_JS_RESOURCE = "/pacUtils.js";

	protected File userJsFile;
	private volatile long lastModified = 0L;

	protected ScriptEngine jsEngine;
	protected Invocable invocableEngine;

	public ForwardRouterJavascript(String jsFilename) {
		initScriptEngine();
		this.userJsFile = new File(jsFilename);
	}

	protected void initScriptEngine() {
		ScriptEngineManager engineMgr = new ScriptEngineManager();
		this.jsEngine = engineMgr.getEngineByName("ECMAScript");
		this.invocableEngine = (Invocable) jsEngine;
	}

	@Override
	public SocketForward route(String ip, String fqn, int port) {
		updateScript();
		String jsTarget = invokeScript(ip, fqn, port);
		log.info("jsTarget: " + jsTarget);

		return decodeJsTarget(jsTarget, ip, port);
	}

	protected SocketForward decodeJsTarget(String jsTarget, String ip, int port) {
		/*
		 * DIRECT; PROXY proxy-rcd1.vd.ch:8080; SOCKS localhost:9999
		 */

		String[] targets = jsTarget.split(";");
		SocketForward[] sforwards = new SocketForward[targets.length];
		InetSocketAddress isa = new InetSocketAddress(ip, port);
		for (int i = 0; i < targets.length; i++) {
			String proxy = targets[i].trim();
			if ("DIRECT".equals(proxy)) {
				sforwards[i] = new SocketForwardDirect(isa);
			} else if (proxy.startsWith("PROXY ")) {
				sforwards[i] = new SocketForwardHttpConnect(isa, decodeHostPort(proxy.substring("PROXY ".length())));
			} else if (proxy.startsWith("SOCKS ")) {
				sforwards[i] = new SocketForwardSocks4(isa, decodeHostPort(proxy.substring("SOCKS ".length())));
			} else {
				// erreur
				log.warn("Can not parse : " + proxy + " : Using Direct");
				sforwards[i] = new SocketForwardDirect(isa);
			}
		}
		return new SocketForwardChain(sforwards);
	}

	protected InetSocketAddress decodeHostPort(String hp) {
		int dd = hp.indexOf(":");
		String host = hp.substring(0, dd);
		int port = Integer.parseInt(hp.substring(dd + 1));
		return new InetSocketAddress(host, port);
	}

	protected String invokeScript(String ip, String fqn, int port) {
		if (this.lastModified > 0L) {
			String target = null;
			try {
				target = (String) this.invocableEngine.invokeFunction(JS_ROUTE_METHOD, ip, fqn, port);
			} catch (ScriptException e) {
				log.error(e);
			} catch (NoSuchMethodException e) {
				log.error(e);
			}
			return target;
		} else {
			log.error("script file not ready");
			return null;
		}

	}

	protected synchronized void updateScript() {
		try {
			if (!this.userJsFile.exists())
				throw new FileNotFoundException(this.userJsFile.getAbsolutePath());
			long curLastModified = userJsFile.lastModified();
			if (curLastModified != this.lastModified) {
				// RELOAD des scripts
				log.info("Reload file " + this.userJsFile.getAbsolutePath());
				this.lastModified = curLastModified;
				// script library pac
				Reader pacUtilsJsReader = new InputStreamReader(getClass().getResourceAsStream(LIB_PAC_JS_RESOURCE),
						"UTF8");
				CharArrayWriter fullJsWriter = new CharArrayWriter();
				copy(pacUtilsJsReader, fullJsWriter);
				pacUtilsJsReader.close();
				fullJsWriter.write("\n\n// === User script ===\n\n");
				// script utilisateur
				InputStreamReader userJsReader = new InputStreamReader(new FileInputStream(userJsFile), "UTF-8");
				copy(userJsReader, fullJsWriter);
				userJsReader.close();
				jsEngine.eval(fullJsWriter.toString());
			}
		} catch (ScriptException e) {
			this.lastModified = 0;
			log.error(e, e);
		} catch (IOException e) {
			this.lastModified = 0;
			log.error(e, e);
		}
	}

	private int copy(Reader r, Writer w) throws IOException {
		int count = 0;
		char[] buf = new char[512];
		int read = 0;
		while ((read = r.read(buf)) > 0) {
			count += read;
			w.write(buf, 0, read);
		}
		return count;
	}

	@Override
	public String getName() {
		return "JS";
	}

}
