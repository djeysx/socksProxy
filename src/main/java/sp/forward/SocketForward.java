package sp.forward;

import java.io.IOException;

import sp.SocketStreams;

public interface SocketForward {

	public void connect() throws IOException;

	public SocketStreams socketStreams();

	public void close();
}
