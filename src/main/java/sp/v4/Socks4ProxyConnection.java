package sp.v4;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import sp.AbstractSocksProxyConnection;
import sp.SocketStreams;
import sp.SocksContants;
import sp.route.ForwardRouter;

public class Socks4ProxyConnection extends AbstractSocksProxyConnection {
	private static final Logger log = Logger.getLogger(Socks4ProxyConnection.class);

	public Socks4ProxyConnection(SocketStreams socketStreams, ForwardRouter frouter) {
		super(socketStreams, frouter);
	}

	public void run() {
		Socks4RequestConnection requestConnection = null;
		try {
			requestConnection = parseRequestConnection();

			switch (requestConnection.command) {
			case TCP_STREAM:
				checkResolveDomain(requestConnection);
				this.socketForward = frouter.route(requestConnection.destinationAddress,
						requestConnection.destinationDomain, requestConnection.destinationPort);
				try {
					this.socketForward.connect();
					sendConnectionResponse(requestConnection, SocksContants.RESPONSE4_STATUS_GRANTED);
					pipeStreams();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					sendConnectionResponse(requestConnection, SocksContants.RESPONSE4_STATUS_REJECTED);
					this.clientStreams.close();
				}
				break;

			default:
				sendConnectionResponse(requestConnection, SocksContants.RESPONSE4_STATUS_REJECTED);
				this.clientStreams.close();
				break;
			}
		} catch (UnknownHostException e) {
			log.error(e.getMessage());
			try {
				sendConnectionResponse(requestConnection, SocksContants.RESPONSE4_STATUS_REJECTED);
			} catch (IOException e1) {
			}
			this.clientStreams.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			this.clientStreams.close();
		}
	}

	private void checkResolveDomain(Socks4RequestConnection request) throws UnknownHostException {
		if (request.destinationDomain != null) {
			InetAddress iaddr = InetAddress.getByName(request.destinationDomain);
			request.destinationAddress = iaddr.getHostAddress();
			request.rawDestionationAddress = iaddr.getAddress();
		} else {
			request.destinationDomain = "";
		}
	}

	private void sendConnectionResponse(Socks4RequestConnection request, byte status) throws IOException {
		byte[] frame = new byte[8];
		frame[1] = status;
		System.arraycopy(request.rawPortNumber, 0, frame, 2, 2);
		System.arraycopy(request.rawDestionationAddress, 0, frame, 4, 4);

		this.clientStreams.outputStream().write(frame);
		if (log.isDebugEnabled())
			log.debug("connectionResponse:status=" + status);
	}

	private Socks4RequestConnection parseRequestConnection() throws IOException {
		/* Version déjà lue */
		DataInputStream dis = this.clientStreams.dataInputStream();
		Socks4RequestConnection rConnection = new Socks4RequestConnection();
		rConnection.rawCommand = dis.readByte();
		dis.readFully(rConnection.rawPortNumber);
		dis.readFully(rConnection.rawDestionationAddress);

		// rConnection.rawUserId = baos.toByteArray();
		rConnection.userId = readZTString(dis);

		// check socks 4a
		byte[] destip = rConnection.rawDestionationAddress;
		if (destip[0] == 0 && destip[1] == 0 && destip[2] == 0) {
			rConnection.destinationDomain = readZTString(dis);
		}

		rConnection.parseRawData();
		log.info("Request: " + rConnection.toString());
		return rConnection;
	}

	private String readZTString(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i;
		while ((i = in.read()) != 0) {
			baos.write(i);
		}
		return new String(baos.toByteArray(), "US-ASCII");
	}
}
