package sp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class SocketPipeRunner implements Runnable {
	private static final Logger log = Logger.getLogger(SocketPipeRunner.class);

	private static final int BUFFER_SIZE = 1024 * 4;
	private SocketStreams socksStreams;
	private SocketStreams otherSideStreams;

	public SocketPipeRunner(SocketStreams socksStreams, SocketStreams otherSideStreams) {
		this.socksStreams = socksStreams;
		this.otherSideStreams = otherSideStreams;
	}

	public void run() {
		if (log.isDebugEnabled())
			log.debug("run: " + socksStreams.socket());
		int bytesRead = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			InputStream in = socksStreams.inputStream();
			OutputStream out = otherSideStreams.outputStream();

			while ((bytesRead = in.read(buffer)) > 0) {
//				if (log.isDebugEnabled())
//					log.debug("read: " + bytesRead);
				out.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			if (log.isDebugEnabled())
				log.debug(e.getMessage());
		} finally {
			socksStreams.close();
			otherSideStreams.close();
		}
	}

}
