package sp;

import java.io.IOException;

import org.apache.log4j.Logger;

import sp.forward.SocketForward;
import sp.route.ForwardRouter;

public abstract class AbstractSocksProxyConnection implements Runnable {
    private static final Logger log = Logger.getLogger(AbstractSocksProxyConnection.class);

    protected SocketStreams clientStreams;
    protected SocketForward socketForward;
    protected ForwardRouter frouter;

    protected AbstractSocksProxyConnection(SocketStreams client, ForwardRouter frouter) {
        this.clientStreams = client;
        this.frouter = frouter;
    }

    protected void checkSocksVersion(SocketStreams clientChannel, byte v) throws IOException {
        byte version = clientChannel.dataInputStream().readByte();
        if (version != v)
            throw new IOException("Wrong Socks version : " + version);
    }

    protected void pipeStreams() {
        if (log.isDebugEnabled())
            log.debug("pipeStreams:");

        SocketPipeRunner spr1 = new SocketPipeRunner(clientStreams, socketForward.socketStreams());
        SocketPipeRunner spr2 = new SocketPipeRunner(socketForward.socketStreams(), clientStreams);

        SocksServer.executor.execute(spr1);
        SocksServer.executor.execute(spr2);
    }

    /* disabled feature */
    protected boolean isRejectedDomain(String domain) {
        return false;
    }
}
