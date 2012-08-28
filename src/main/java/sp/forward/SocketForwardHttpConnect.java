package sp.forward;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import sp.SocketStreams;
import sp.SocksContants;

public class SocketForwardHttpConnect implements SocketForward {
	private static final Logger log = Logger.getLogger(SocketForwardHttpConnect.class);

	private InetSocketAddress proxySockAddress;

	private InetSocketAddress socketAddress;
	private SocketStreams socksStreams;

	public SocketForwardHttpConnect(InetSocketAddress isa, InetSocketAddress inetSocketAddress) {
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
		String frame = "CONNECT " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort()
				+ " HTTP/1.0" + "\r\n\r\n";
		// if (log.isDebugEnabled())
		// log.debug("HTTP request: " + frame);
		socksStreams.outputStream().write(frame.getBytes(SocksContants.CHARSET_ISO8859_1));
		int httpCode = readHttpResponse();
		if (httpCode != 200) {
			close();
			throw new IOException("HTTP Proxy has refused CONNECT : " + httpCode);
		}
	}

	private int readHttpResponse() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int curLineCharNumber = 0;
		int curChar = 0;
		InputStream in = socksStreams.inputStream();
		int LF = 10;
		do {
			if (curChar == LF) // char precedent
				curLineCharNumber = 0;
			curChar = in.read();
			baos.write(curChar);
			curLineCharNumber++;
		} while (!(curLineCharNumber == 2 && curChar == LF)); // CR=13 LF=10
		// toute la réponse est lu

		// cherche la 1ere ligne
		byte[] buffer = baos.toByteArray();
		int eolPos = 0;
		for (; eolPos < buffer.length && buffer[eolPos] != LF; eolPos++) {
		}
		String firstLine = new String(buffer, 0, eolPos - 1, SocksContants.CHARSET_ISO8859_1);
		String[] firstLineElems = firstLine.split(" ");
		String httpCode = firstLineElems[1];
		if (log.isDebugEnabled())
			log.debug("HTTP response: " + firstLine);
		return Integer.valueOf(httpCode);
	}

	public SocketStreams socketStreams() {
		return socksStreams;
	}

	@Override
	public String toString() {
		return "PROXY " + proxySockAddress.toString();
	}
}
