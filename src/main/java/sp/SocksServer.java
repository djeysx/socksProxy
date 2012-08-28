package sp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import sp.route.ForwardRouter;
import sp.route.ForwardRouterJavascript;

public class SocksServer implements Runnable {
	private static final Logger log = Logger.getLogger(SocksServer.class);

	private int port;
	private String bind;
	private ForwardRouter frouter;

	public static SocksServer create(String[] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		String bind = args[1];
		String jsFilename = args[2];
		return new SocksServer(port, bind, jsFilename);
	}

	public static void main(String[] args) throws Exception {
		SocksServer app = create(args);
		app.run();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public SocksServer(int port, String bind, String jsFilename) {
		this.port = port;
		this.bind = bind;
		frouter = new ForwardRouterJavascript(jsFilename);
	}

	public void run() {
		try {
			ServerSocket ssc = new ServerSocket();
			// ssc.setReuseAddress(true);
			InetSocketAddress isa = new InetSocketAddress(bind, port);
			ssc.bind(isa, 50);
			log.info("Listening to " + ssc.getLocalSocketAddress());

			// int count = 0;
			while (true) {
				Socket client = ssc.accept();
				//client.setReuseAddress(true);
				// log.info("Accept #" + (++count) + " from " +
				// client.getRemoteSocketAddress());
				SocketStreams sc = new SocketStreams(client);
				SocksProxyVersionDispatcher spc = new SocksProxyVersionDispatcher(sc, frouter);
				Thread t = new Thread(spc);
				t.setDaemon(true);
				t.setName(client.getRemoteSocketAddress().toString());
				t.start();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		System.exit(1);
	}

	public int getPort() {
		return port;
	}

	public String getBind() {
		return bind;
	}

	public String getCodeRouter() {
		return frouter.getName();
	}

}
