package sp.route;

import java.net.InetSocketAddress;

import sp.forward.SocketForward;
import sp.forward.SocketForwardDirect;

public class ForwardRouterDirect extends ForwardRouter {

    @Override
    public SocketForward route(String ip, String fqn, int port) {
        InetSocketAddress isa = new InetSocketAddress(ip, port);
        return new SocketForwardDirect(isa);
    }

    @Override
    public String getName() {
        return "DIRECT";
    }

}
