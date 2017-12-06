package org.dcache.xdr;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.dcache.utils.net.InetSocketAddresses;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractRpcProtocolFactory;
import org.dcache.xdr.portmap.OncPortmapClient;
import org.dcache.xdr.portmap.OncRpcPortmap;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OncRpcProtocolFactory extends AbstractRpcProtocolFactory<IOncRpcSvc> implements IOncProtocolFactory{
    


    private final static Logger _log = LoggerFactory.getLogger(OncRpcProtocolFactory.class);
    private boolean _publish;
    private boolean _isClient;
    
    @Override
    public void processBuilder(OncRpcSvcBuilder builder) {
        _publish = builder.isAutoPublish();
        _isClient =builder.isClient();
    }
    /**
     * Register to portmapper
     * @throws IOException 
     */
    @Override
    public void preStopActions(RpcSvcItf<IOncRpcSvc> rpcSvcItf) throws IOException {
        if (!_isClient && _publish) {
            clearPortmap(getPrograms().keySet());
        }
    }
    @Override
    public void doPreStartAction(IOncRpcSvc svc) throws IOException {
        if (_publish) {
            Connection<InetSocketAddress> connection = svc.getConnection();
            Set<OncRpcProgram> programs = svc.getPrograms();
            publishToPortmap(connection, getPrograms().keySet());
        }
    }
}
