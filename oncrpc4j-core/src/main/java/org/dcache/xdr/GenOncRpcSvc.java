/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
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
import org.dcache.xdr.gss.GssProtocolFilter;
import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.GenItfOncRpcClient;
import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfReplyQueue;
import org.dcache.xdr.model.itf.GenItfXdrTransport;
import org.dcache.xdr.model.itf.GenXdrTransport;
import org.dcache.xdr.model.root.GenAbstractOncRpcSvc;
import org.dcache.xdr.portmap.GenGenericPortmapClient;
import org.dcache.xdr.portmap.GenericPortmapClient;
import org.dcache.xdr.portmap.OncPortmapClient;
import org.dcache.xdr.portmap.OncRpcPortmap;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class GenOncRpcSvc extends GenAbstractOncRpcSvc<GenOncRpcSvc> {
    final static Logger _log = LoggerFactory.getLogger(GenOncRpcSvc.class);
    /**
     * Handle RPCSEC_GSS
     */
    protected final GssSessionManager _gssSessionManager;
    /**
     * Create new RPC service with defined configuration.
     * @param builder to build this service
     */
    public  GenOncRpcSvc(GenOncRpcSvcBuilder builder) {
        super(builder);
        _gssSessionManager = builder.getGssSessionManager();
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.GenItfRpcSvc#start()
     */
    @Override
    protected Filter rpcMessageReceiverFor(Transport t) {
        if (t instanceof TCPNIOTransport) {
            return new GenRpcMessageParserTCP2();
        }

        if (t instanceof UDPNIOTransport) {
            return new RpcMessageParserUDP();
        }

        throw new RuntimeException("Unsupported transport: " + t.getClass().getName());
    }

    @Override
    protected Filter getRpcProtocolFilter(GenItfReplyQueue<GenOncRpcSvc> replyQueue) {
        return new GenRpcProtocolFilter(replyQueue);
    }

    @Override
    protected void addPostTransportProtocolFilters(FilterChainBuilder filterChain, Transport t) {
    }

    @Override
    protected void doPostCreationServerActions(Connection<InetSocketAddress> connection) throws IOException {
        if (_publish) {
            publishToPortmap(connection, _programs.keySet());
        }
    }


    @Override
    protected void doPreStopActions() throws IOException {

        if (!_isClient && _publish) {
            clearPortmap(_programs.keySet());
        }
        
    }
    /**
     * Register services in portmap.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    protected void publishToPortmap(Connection<InetSocketAddress> connection, Set<OncRpcProgram> programs) throws IOException {
    
        GenOncRpcClient rpcClient = new GenOncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
         GenXdrTransport<GenOncRpcSvc> transport = rpcClient.connect();
    
        try {
            OncPortmapClient portmapClient = new GenGenericPortmapClient(transport);
    
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
    protected void clearPortmap(Set<OncRpcProgram> programs) throws IOException {
    
         GenOncRpcClient rpcClient = new GenOncRpcClient(InetAddress.getByName(null),
                IpProtocolType.UDP, OncRpcPortmap.PORTMAP_PORT);
         GenXdrTransport<GenOncRpcSvc> transport = rpcClient.connect();
    
        try {
            OncPortmapClient portmapClient = new GenGenericPortmapClient(transport);
    
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

    @Override
    protected void doBeforeStart() throws IOException {

        if(!_isClient && _publish) {
            clearPortmap(_programs.keySet());
        }
        
    }
    @Override
    protected void addPostRpcProtocolFilter(FilterChainBuilder filterChain) {
        // use GSS if configures
        if (_gssSessionManager != null) {
            filterChain.add(new GssProtocolFilter(_gssSessionManager));
        }
        
    }
}
