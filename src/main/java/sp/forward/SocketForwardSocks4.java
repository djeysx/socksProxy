package sp.forward;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import sp.SocketStreams;
import sp.SocksContants;

public class SocketForwardSocks4 implements SocketForward {
	private static final Logger log = Logger.getLogger(SocketForwardSocks4.class);

	private InetSocketAddress proxySockAddress;

	private InetSocketAddress socketAddress;
	private SocketStreams socksStreams;

	public SocketForwardSocks4(InetSocketAddress isa, InetSocketAddress inetSocketAddress) {
		this.socketAddress = isa;
		this.proxySockAddress = inetSocketAddress;
	}

	public void close() {
		if (log.isDebugEnabled())
			log.debug("close:");
		socksStreams.close();
	}

	public void connect() throws IOException {
		log.info("connect:" + socketAddress + " through " + proxySockAddress);
		Socket sc = new Socket();
		sc.setReuseAddress(true);
		sc.connect(proxySockAddress);
		socksStreams = new SocketStreams(sc);

		DataOutputStream dos = socksStreams.dataOutputStream();
		dos.writeByte(SocksContants.SOCKS_VERSION_4);
		dos.writeByte(SocksContants.COMMAND_TCP_STREAM);
		// portnumber
		dos.writeByte((socketAddress.getPort() & 0x0000FF00) >> 8);
		dos.writeByte((socketAddress.getPort() & 0x000000FF));
		// ipaddress
		dos.write(socketAddress.getAddress().getAddress());
		// userid
		dos.writeByte(0);
		dos.flush();

		// ---response---

		DataInputStream dis = socksStreams.dataInputStream();
		dis.readByte();
		byte status = dis.readByte();
		if (status != SocksContants.RESPONSE4_STATUS_GRANTED)
			throw new IOException("socks proxy refuse connection");

		byte[] ignoredData = new byte[6];
		dis.readFully(ignoredData);

	}

	public SocketStreams socketStreams() {
		return socksStreams;
	}

	@Override
	public String toString() {
		return "PROXY " + proxySockAddress.toString();
	}
}
