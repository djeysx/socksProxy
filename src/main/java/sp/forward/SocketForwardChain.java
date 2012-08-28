package sp.forward;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import sp.SocketStreams;

public class SocketForwardChain implements SocketForward {
	private static final Logger log = Logger.getLogger(SocketForwardChain.class);

	private SocketForward[] socketForwards;
	private SocketForward currentForward;

	public SocketForwardChain(SocketForward[] socketForwards) {
		this.socketForwards = socketForwards;
	}

	public void close() {
		this.currentForward.close();
	}

	public void connect() throws IOException {
		IOException excep = null;
		for (SocketForward sf : socketForwards) {
			this.currentForward = sf;
			try {
				sf.connect();
				excep = null;
				break;
			} catch (IOException e) {
				excep = e;
				log.warn("Failed: " + sf.toString() + " : " + e.getMessage());
			}
		}
		if (excep != null)
			throw new IOException("Proxy chain failed: " + toString());
	}

	public SocketStreams socketStreams() {
		return currentForward.socketStreams();
	}

	@Override
	public String toString() {
		return Arrays.toString(socketForwards);
	}
}
