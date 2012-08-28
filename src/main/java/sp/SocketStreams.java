package sp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class SocketStreams {
	private static final Logger log = Logger.getLogger(SocketStreams.class);

	private Socket socket;
	private DataInputStream dins;
	private DataOutputStream douts;

	private InputStream ins;
	private OutputStream outs;

	public SocketStreams(Socket sc) {
		this.socket = sc;
		/*		try {
					socket.setReuseAddress(true);
				} catch (SocketException e) {
				}
				*/
	}

	public Socket socket() {
		return socket;
	}

	public DataInputStream dataInputStream() throws IOException {
		if (dins == null)
			dins = new DataInputStream(inputStream());
		return dins;
	}

	public DataOutputStream dataOutputStream() throws IOException {
		if (douts == null)
			douts = new DataOutputStream(outputStream());
		return douts;
	}

	public InputStream inputStream() throws IOException {
		if (ins == null)
			ins = socket.getInputStream();
		return ins;
	}

	public OutputStream outputStream() throws IOException {
		if (outs == null)
			outs = socket.getOutputStream();
		return outs;
	}

	public void close() {
		if (log.isDebugEnabled())
			log.debug("close:" + socket.getRemoteSocketAddress());
		try {
			socket.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
