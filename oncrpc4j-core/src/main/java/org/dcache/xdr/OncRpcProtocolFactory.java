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
import org.dcache.xdr.model.root.AbstractOncRpcClient;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractRpcProtocolFactory;
import org.dcache.xdr.portmap.GenericPortmapClient;
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


    /**
     * Register services in portmap.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    private void publishToPortmap(Connection<InetSocketAddress> connection, Set<OncRpcProgram> programs) throws IOException {

        OncRpcClient rpcClient = new OncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
        XdrTransportItf<IOncRpcSvc> transport = rpcClient.connect();

        try {
            OncPortmapClient<?> portmapClient = new GenericPortmapClient<>(transport);

            Set<String> netids = new HashSet<>();
            String username = System.getProperty("user.name");
            Transport t = connection.getTransport();
            String uaddr = InetSocketAddresses.uaddrOf(connection.getLocalAddress());

            String netidBase;
            if (t instanceof TCPNIOTransport) {
                netidBase = "tcp";
            } else if (t instanceof UDPNIOTransport) {
                netidBase = "udp";
            } else {
                // must never happens
                throw new RuntimeException("Unsupported transport type: " + t.getClass().getCanonicalName());
            }

            InetAddress localAddress = connection.getLocalAddress().getAddress();
            if (localAddress instanceof Inet6Address) {
                netids.add(netidBase + "6");
                if (((Inet6Address)localAddress).isIPv4CompatibleAddress()) {
                    netids.add(netidBase);
                }
            } else {
                netids.add(netidBase);
            }

            for (OncRpcProgram program : programs) {
                for (String netid : netids) {
                    try {
                        portmapClient.setPort(program.getNumber(), program.getVersion(),
                                netid, uaddr, username);
                    } catch (OncRpcException | TimeoutException e) {
                        _log.warn("Failed to register program: {}", e.getMessage());
                    }
                }
            }
        } catch (RpcProgUnavailable e) {
            _log.warn("Failed to register at portmap: {}", e.getMessage());
        } finally {
            rpcClient.close();
        }
    }

    /**
     * UnRegister services in portmap.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    private void clearPortmap(Set<OncRpcProgram> programs) throws IOException {

        AbstractOncRpcClient<IOncRpcSvc> rpcClient = new OncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
        XdrTransportItf<IOncRpcSvc> transport = rpcClient.connect();

        try {
            OncPortmapClient<IOncRpcSvc> portmapClient = new GenericPortmapClient<>(transport);

            String username = System.getProperty("user.name");

            for (OncRpcProgram program : programs) {
                try {
                    portmapClient.unsetPort(program.getNumber(),
                            program.getVersion(), username);
                } catch (OncRpcException | TimeoutException e) {
                    _log.info("Failed to unregister program: {}", e.getMessage());
                }
            }
        } catch (RpcProgUnavailable e) {
            _log.info("portmap service not available");
        } finally {
            rpcClient.close();
        }
    }
}
