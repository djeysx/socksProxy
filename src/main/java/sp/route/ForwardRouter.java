package sp.route;

import sp.forward.SocketForward;

public abstract class ForwardRouter {

	public abstract SocketForward route(String ip, String fqn, int port);
	public abstract String getName();
}
