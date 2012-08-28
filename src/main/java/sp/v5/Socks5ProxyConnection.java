package sp.v5;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import sp.AbstractSocksProxyConnection;
import sp.SocketStreams;
import sp.SocksContants;
import sp.SocksUtils;
import sp.route.ForwardRouter;

public class Socks5ProxyConnection extends AbstractSocksProxyConnection {
	private static final Logger log = Logger.getLogger(Socks5ProxyConnection.class);

	// private static final ConcurrentMap<String, AtomicInteger> stats = new
	// ConcurrentHashMap<String, AtomicInteger>();

	public Socks5ProxyConnection(SocketStreams channel, ForwardRouter frouter) {
		super(channel, frouter);
	}

	public void run() {
		Socks5RequestConnection requestConnection = null;
		try {
			parseRequestHello();
			// pas d'authentification. en dur
			sendAuthNoAuth();

			requestConnection = parseRequestConnection();

			switch (requestConnection.command) {
			case TCP_STREAM:
				checkResolveDomain(requestConnection);
				// updateStats(requestConnection.destinationDomain);

				if (checkRejectedDomain(requestConnection.destinationDomain)) {
					log.info("Request rejected: " + requestConnection.toString());
					// + " | " +
					// stats.get(requestConnection.destinationDomain));
					sendConnectionResponse(requestConnection, SocksContants.RESPONSE5_STATUS_CONNECTION_NOT_ALLOWED);
					this.clientStreams.close();

				} else {
					log.info("Request accepted: " + requestConnection.toString());
					// + " | " +
					// stats.get(requestConnection.destinationDomain));
					this.socketForward = frouter.route(requestConnection.destinationAddress,
							requestConnection.destinationDomain, requestConnection.destinationPort);
					try {

						this.socketForward.connect();
						sendConnectionResponse(requestConnection, SocksContants.RESPONSE5_STATUS_GRANTED);
						pipeStreams();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
						sendConnectionResponse(requestConnection, SocksContants.RESPONSE5_STATUS_HOST_UNREACHABLE);
						this.clientStreams.close();
					}
				}
				break;

			default:
				sendConnectionResponse(requestConnection, SocksContants.RESPONSE5_STATUS_COMMAND_NOT_SUPPORTED);
				this.clientStreams.close();
				break;
			}

		} catch (UnknownHostException e) {
			log.error(e, e);
			try {
				sendConnectionResponse(requestConnection, SocksContants.RESPONSE5_STATUS_HOST_UNREACHABLE);
			} catch (IOException e1) {
			}
			this.clientStreams.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * private void updateStats(String destinationDomain) { if
	 * (destinationDomain != null) { AtomicInteger oldValue =
	 * stats.putIfAbsent(destinationDomain, new AtomicInteger(1)); if (oldValue
	 * != null) oldValue.incrementAndGet(); } }
	 */
	private void checkResolveDomain(Socks5RequestConnection request) throws UnknownHostException {
		if (request.destinationDomain != null) {
			InetAddress iaddr = InetAddress.getByName(request.destinationDomain);
			request.destinationAddress = iaddr.getHostAddress();
			request.rawDestionationAddress = iaddr.getAddress();
		} else {
			request.destinationDomain = "";
		}
	}

	private void sendConnectionResponse(Socks5RequestConnection request, byte status) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);

		out.writeByte(SocksContants.SOCKS_VERSION_5);
		out.writeByte(status);
		out.writeByte(0);

		// bind addr & port
		if (this.socketForward != null) {
			InetSocketAddress localIsa = (InetSocketAddress) this.socketForward.socketStreams().socket()
					.getLocalSocketAddress();
			byte[] localAddress = localIsa.getAddress().getAddress();
			switch (localAddress.length) {
			case 4:
				out.writeByte(AddressType.IPV4.getAddressCode());
				break;
			case 16:
				out.writeByte(AddressType.IPV6.getAddressCode());
				break;
			}
			out.write(localAddress);
			out.writeShort(localIsa.getPort());
		} else {
			out.writeByte(AddressType.IPV4.getAddressCode());
			out.write(new byte[] { 0, 0, 0, 0 });
			out.writeShort(request.destinationPort);
		}

		byte[] frame = baos.toByteArray();
		this.clientStreams.outputStream().write(frame);
		// this.clientStreams.outputStream().flush();
	}

	private Socks5RequestConnection parseRequestConnection() throws IOException {
		/* field 1 socks version */
		checkSocksVersion(this.clientStreams, SocksContants.SOCKS_VERSION_5);
		DataInputStream dis = this.clientStreams.dataInputStream();
		Socks5RequestConnection rConnection = new Socks5RequestConnection();
		/* field 2 command code */
		rConnection.rawCommand = dis.readByte();
		/* field 3 reserved */
		dis.readByte();
		/* field 4 address type */
		rConnection.rawAddressType = dis.readByte();

		/* field 5 destination address */
		switch (rConnection.rawAddressType) {
		case SocksContants.ADDRESS_TYPE_IPV4:
			rConnection.rawDestionationAddress = new byte[4];
			dis.readFully(rConnection.rawDestionationAddress);
			break;
		case SocksContants.ADDRESS_TYPE_DOMAIN:
			int length = dis.readUnsignedByte();
			if (log.isDebugEnabled())
				log.debug("rCnx:addressType:DomainLength=" + length);
			rConnection.rawDestionationAddress = new byte[length + 1];
			rConnection.rawDestionationAddress[0] = (byte) length;
			dis.readFully(rConnection.rawDestionationAddress, 1, length);
			/* do the parsing */
			rConnection.destinationDomain = new String(rConnection.rawDestionationAddress, 1, length, "US-ASCII");
			break;
		case SocksContants.ADDRESS_TYPE_IPV6:
			rConnection.rawDestionationAddress = new byte[16];
			dis.readFully(rConnection.rawDestionationAddress);
			break;

		default:
			break;
		}

		/* field 6: network byte order port number */
		dis.readFully(rConnection.rawPortNumber);

		rConnection.parseRawData();
		if (log.isDebugEnabled())
			log.debug("Request: " + rConnection.toString());
		return rConnection;
	}

	private void sendAuthNoAuth() throws IOException {
		OutputStream out = this.clientStreams.outputStream();
		byte[] frame = { SocksContants.SOCKS_VERSION_5, SocksContants.AUTH_METHOD_NOAUTH };
		out.write(frame);
	}

	private Socks5RequestHello parseRequestHello() throws IOException {
		// version deja lue
		DataInputStream dis = this.clientStreams.dataInputStream();
		Socks5RequestHello rHello = new Socks5RequestHello();
		rHello.authMethodNumber = dis.readByte();
		if (log.isDebugEnabled())
			log.debug("rHello:authMethodNumber=" + rHello.authMethodNumber);
		rHello.authMethods = new byte[rHello.authMethodNumber];
		for (int i = 0; i < rHello.authMethodNumber; i++) {
			rHello.authMethods[i] = dis.readByte();
		}
		if (log.isDebugEnabled())
			log.debug("rHello:authMethods=" + SocksUtils.bytes2String(rHello.authMethods));
		return rHello;
	}

}
