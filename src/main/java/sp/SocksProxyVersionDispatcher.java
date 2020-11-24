package sp;

import java.io.IOException;

import org.apache.log4j.Logger;

import sp.route.ForwardRouter;
import sp.v4.Socks4ProxyConnection;
import sp.v5.Socks5ProxyConnection;

public class SocksProxyVersionDispatcher implements Runnable {
    private static final Logger log = Logger.getLogger(SocksProxyVersionDispatcher.class);

    private SocketStreams socketStreams;
    private ForwardRouter frouter;

    public SocksProxyVersionDispatcher(SocketStreams socketStreams, ForwardRouter frouter) {
        this.socketStreams = socketStreams;
        this.frouter = frouter;
    }

    public void run() {
        try {
            Runnable proxyImpl = null;
            int version = getSocksVersion(socketStreams);
            if (log.isDebugEnabled())
                log.debug("socksVersion=" + version);
            switch (version) {
            case SocksContants.SOCKS_VERSION_4:
                proxyImpl = new Socks4ProxyConnection(socketStreams, frouter);
                break;
            case SocksContants.SOCKS_VERSION_5:
                proxyImpl = new Socks5ProxyConnection(socketStreams, frouter);
                break;

            default:
                throw new IOException("Mauvaise version socks: " + version);
            }

            proxyImpl.run();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private int getSocksVersion(SocketStreams clientChannel) throws IOException {
        return clientChannel.dataInputStream().readByte();
    }

}
