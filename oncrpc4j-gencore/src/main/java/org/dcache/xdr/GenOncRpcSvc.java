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
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.portmap.GenericPortmapClient;
import org.dcache.xdr.portmap.OncPortmapClient;
import org.dcache.xdr.portmap.OncRpcPortmap;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Problème de type
 * @author jmk
 *
 * @param <SVC_T>
 */
public class GenOncRpcSvc<SVC_T extends RpcSvcItf<IOncRpcSvc>> extends AbstractOncRpcSvc<IOncRpcSvc,IOncRpcSvcBuilder> implements IOncRpcSvc{
    private final static Logger _log = LoggerFactory.getLogger(GenOncRpcSvc.class);
    private boolean _publish;

    protected <BUILDER_T extends IOncRpcSvcBuilder> GenOncRpcSvc(BUILDER_T builder) {
        super(builder);
    }

    @Override
    public IOncRpcSvc getThis() {
        return this;
    }
    
    
    
    
    //TODO JMK Modifier le nom, cela ne s'applique qu'au serveur
    @Override
    protected void doPreStartAction(Connection<InetSocketAddress> c) throws IOException {
        if ( _publish) {
            publishToPortmap(c, getPrograms().keySet());
        }
    }

    @Override
    protected void preStopActions() throws IOException {
        if(!isClient() && _publish) {
            clearPortmap(getPrograms().keySet());
        }
        
    }

    @Override
    protected void processBuilder(IOncRpcSvcBuilder builder) {
        _publish = builder.isAutoPublish();
    }




    /**
     * Register services in portmap.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    private void publishToPortmap(Connection<InetSocketAddress> connection, Set<OncRpcProgram> programs) throws IOException {

        GenOncRpcClient rpcClient = new GenOncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
        XdrTransportItf<IOncRpcSvc> transport = rpcClient.connect();

        try {
            OncPortmapClient portmapClient = new GenericPortmapClient<IOncRpcSvc>(transport);

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

        GenOncRpcClient rpcClient = new GenOncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
        XdrTransportItf<IOncRpcSvc> transport = rpcClient.connect();

        try {
            OncPortmapClient portmapClient = new GenericPortmapClient<>(transport);

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
