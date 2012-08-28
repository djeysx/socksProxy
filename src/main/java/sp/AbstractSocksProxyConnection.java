package sp;

import java.io.IOException;

import org.apache.log4j.Logger;

import sp.forward.SocketForward;
import sp.route.ForwardRouter;

public abstract class AbstractSocksProxyConnection implements Runnable {
	private static final Logger log = Logger.getLogger(AbstractSocksProxyConnection.class);

	private static final DomainRegistry rejectedDomainRegistry = new DomainRegistryOptimized();

	protected SocketStreams clientStreams;
	protected SocketForward socketForward;
	protected ForwardRouter frouter;

	protected AbstractSocksProxyConnection(SocketStreams client, ForwardRouter frouter) {
		this.clientStreams = client;
		this.frouter = frouter;
	}

	protected void checkSocksVersion(SocketStreams clientChannel, byte v) throws IOException {
		byte version = clientChannel.dataInputStream().readByte();
		if (version != v)
			throw new IOException("Wrong Socks version : " + version);
	}

	protected void pipeStreams() {
		if (log.isDebugEnabled())
			log.debug("pipeStreams:");

		SocketPipeRunner spr1 = new SocketPipeRunner(clientStreams, socketForward.socketStreams());
		SocketPipeRunner spr2 = new SocketPipeRunner(socketForward.socketStreams(), clientStreams);
		// thread 1
		Thread tspr1 = new Thread(spr1);
		tspr1.setDaemon(true);
		tspr1.setName(clientStreams.socket().getLocalPort() + ":"
				+ clientStreams.socket().getRemoteSocketAddress().toString());
		// thread 2
		Thread tspr2 = new Thread(spr2);
		tspr2.setDaemon(true);
		tspr2.setName(socketForward.socketStreams().socket().getLocalPort() + ":"
				+ socketForward.socketStreams().socket().getRemoteSocketAddress().toString());
		// start
		tspr1.start();
		tspr2.start();
	}

	protected boolean checkRejectedDomain(String domain) {
		return rejectedDomainRegistry.match(domain);
	}
}
