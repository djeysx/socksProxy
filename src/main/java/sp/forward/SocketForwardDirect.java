package sp.forward;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import sp.SocketStreams;

public class SocketForwardDirect implements SocketForward {
	private static final Logger log = Logger.getLogger(SocketForwardDirect.class);

	private InetSocketAddress socketAddress;
	private SocketStreams socksStreams;

	public SocketForwardDirect(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public void connect() throws IOException {
		log.info("connect: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
		Socket sc = new Socket();
		sc.setReuseAddress(true);
		sc.connect(socketAddress);
		socksStreams = new SocketStreams(sc);
	}

	public SocketStreams socketStreams() {
		return socksStreams;
	}

	public void close() {
		if (log.isDebugEnabled())
			log.debug("close:");
		socksStreams.close();
	}

	@Override
	public String toString() {
		return "DIRECT";
	}
}
