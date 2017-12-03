/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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
package org.dcache.xdr.model.root;

import static org.dcache.xdr.GrizzlyUtils.rpcMessageReceiverFor;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.impl.AbstractGrizzlyXdrTransport;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.ProtocolFactoryItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.itf.RpcSessionManagerItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.glassfish.grizzly.CloseType;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.SocketBinder;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.jmxbase.GrizzlyJmxManager;
import org.glassfish.grizzly.nio.NIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.propagateIfPossible;
import java.net.SocketAddress;
import java.util.stream.Collectors;
import static org.dcache.xdr.GrizzlyUtils.getSelectorPoolCfg;
import static org.dcache.xdr.GrizzlyUtils.transportFor;

public abstract class AbstractOncRpcSvc<SVC_T extends RpcSvcItf<SVC_T>> implements  RpcSvcItf<SVC_T>{
    
    private final static Logger _log = LoggerFactory.getLogger(AbstractOncRpcSvc.class);
    
    
    
    
    
    private ProtocolFactoryItf<SVC_T> _protocolFactory = AbstractRpcProtocolFactory();
    
    private final int _backlog;
    private final PortRange _portRange;
    private final String _bindAddress;
    private final boolean _isClient;
    private final List<NIOTransport> _transports = new ArrayList<>();
    private final Set<Connection<InetSocketAddress>> _boundConnections =
            new HashSet<>();

    private final ExecutorService _requestExecutor;

    private final AbstractReplyQueue<SVC_T> _replyQueue = new AbstractReplyQueue<>();

    private final boolean _withSubjectPropagation;
    /**
     * Handle RPCSEC_GSS
     */
    private RpcSessionManagerItf<SVC_T> _rpcSessionManager;

    /**
     * mapping of registered programs.
     */
    private final Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> _programs =
            new ConcurrentHashMap<>();

    /**
     * Name of this service
     */
    private final String _svcName;

    /**
     * Create new RPC service with defined configuration.
     * @param builder to build this service
     */
    protected <BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,BUILDER_T>>  AbstractOncRpcSvc(BUILDER_T builder) {
        final int protocol = builder.getProtocol();

        if ((protocol & (IpProtocolType.TCP | IpProtocolType.UDP)) == 0) {
            throw new IllegalArgumentException("TCP or UDP protocol have to be defined");
        }

        IoStrategy ioStrategy = builder.getIoStrategy();
        String serviceName = builder.getServiceName();
        ThreadPoolConfig selectorPoolConfig = getSelectorPoolCfg(ioStrategy,
                serviceName,
                builder.getSelectorThreadPoolSize());

        if ((protocol & IpProtocolType.TCP) != 0) {
            final TCPNIOTransport tcpTransport = TCPNIOTransportBuilder
                    .newInstance()
                    .setReuseAddress(true)
                    .setIOStrategy(SameThreadIOStrategy.getInstance())
                    .setSelectorThreadPoolConfig(selectorPoolConfig)
                    .setSelectorRunnersCount(selectorPoolConfig.getMaxPoolSize())
                    .build();
            _transports.add(tcpTransport);
        }

        if ((protocol & IpProtocolType.UDP) != 0) {
            final UDPNIOTransport udpTransport = UDPNIOTransportBuilder
                    .newInstance()
                    .setReuseAddress(true)
                    .setIOStrategy(SameThreadIOStrategy.getInstance())
                    .setSelectorThreadPoolConfig(selectorPoolConfig)
                    .setSelectorRunnersCount(selectorPoolConfig.getMaxPoolSize())
                    .build();
            _transports.add(udpTransport);
        }
        _protocolFactory = builder.getFactory();
        _isClient = builder.isClient();
        _portRange = builder.getMinPort() > 0 ?
                new PortRange(builder.getMinPort(), builder.getMaxPort()) : null;

        _backlog = builder.getBacklog();
        _bindAddress = builder.getBindAddress();

        if (builder.isWithJMX()) {
            final GrizzlyJmxManager jmxManager = GrizzlyJmxManager.instance();
	    _transports.forEach((t) -> {
		jmxManager.registerAtRoot(t.getMonitoringConfig().createManagementObject(), t.getName() + "-" + _portRange);
	    });
        }
        _requestExecutor = builder.getWorkerThreadExecutorService();
        _rpcSessionManager = builder.getRpcSessionManager();
        if (null == builder.getRpcSessionManager()) {
            _rpcSessionManager = new DefaultSessionManager<>();
        } else {
            _rpcSessionManager = builder.getRpcSessionManager();
        }
        _programs.putAll(builder.getRpcServices());
        _withSubjectPropagation = builder.getSubjectPropagation();
	_svcName = builder.getServiceName();
	    _protocolFactory.processBuilder(builder);
    }

    private ProtocolFactoryItf<SVC_T> AbstractRpcProtocolFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Register a new PRC service. Existing registration will be overwritten.
     *
     * @param prog program number
     * @param handler RPC requests handler.
     */
    public void register(OncRpcProgram prog, RpcDispatchableItf<SVC_T> handler) {
        _log.info("Registering new program {} : {}", prog, handler);
        _programs.put(prog, handler);
    }

    /**
     * Unregister program.
     *
     * @param prog
     */
    public void unregister(OncRpcProgram prog) {
        _log.info("Unregistering program {}", prog);
        _programs.remove(prog);
    }

    /**
     * Add programs to existing services.
     * @param services
     * @deprecated use {@link AbstractOncRpcSvcBuilder#withRpcService} instead.
     */
    @Deprecated
    public void setPrograms(Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> services) {
        _programs.putAll(services);
    }



    public void start() throws IOException {
        _protocolFactory.preStopActions(this);
        
        for (Transport t : _transports) {

            FilterChainBuilder filterChain = FilterChainBuilder.stateless();
            filterChain.add(new TransportFilter());
            filterChain.add(rpcMessageReceiverFor(t));
            filterChain.add(new AbstractRpcProtocolFilter<>(_replyQueue,_protocolFactory));
            // use GSS if configures
            filterChain.add(_rpcSessionManager);
            filterChain.add(new AbstractRpcDispatcher<>(_requestExecutor, _programs, _withSubjectPropagation));

            final FilterChain filters = filterChain.build();

            t.setProcessor(filters);
            t.getConnectionMonitoringConfig().addProbes(new ConnectionProbe.Adapter() {
                @Override
                public void onCloseEvent(Connection connection) {
                    if (connection.getCloseReason().getType() == CloseType.REMOTELY) {
                        _replyQueue.handleDisconnect((SocketAddress)connection.getLocalAddress());
                    }
                }
            });

            if(!_isClient) {
                Connection<InetSocketAddress> connection = _portRange == null ?
                        ((SocketBinder) t).bind(_bindAddress, 0, _backlog) :
                        ((SocketBinder) t).bind(_bindAddress, _portRange, _backlog);

                _boundConnections.add(connection);
            }
            _protocolFactory.doPreStartAction(this);
            t.start();

        }
    }

    public void stop() throws IOException {
        _protocolFactory.preStopActions(this);

        for (Transport t : _transports) {
            t.shutdownNow();
        }

        _replyQueue.shutdown();
        _requestExecutor.shutdown();
    }

    public void stop(long gracePeriod, TimeUnit timeUnit) throws IOException {
        _protocolFactory.preStopActions(this);
        
        List<GrizzlyFuture<Transport>> transportsShuttingDown = new ArrayList<>();
        for (Transport t : _transports) {
            transportsShuttingDown.add(t.shutdown(gracePeriod, timeUnit));
        }

        for (GrizzlyFuture<Transport> transportShuttingDown : transportsShuttingDown) {
            try {
                transportShuttingDown.get();
            } catch (InterruptedException e) {
                _log.info("Waiting for graceful shut down interrupted");
            } catch (ExecutionException e) {
                Throwable t = getRootCause(e);
                _log.warn("Exception while waiting for transport to shut down gracefully",t);
            }
        }

        _requestExecutor.shutdown();
    }

    public XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress) throws IOException {
        return connect(socketAddress, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress, long timeout, TimeUnit timeUnit) throws IOException {

        // in client mode only one transport is defined
        NIOTransport transport = _transports.get(0);

        Future<Connection> connectFuture;
        if (_portRange != null) {
            InetSocketAddress localAddress = new InetSocketAddress(_portRange.getLower());
            connectFuture = transport.connect(socketAddress, localAddress);
        } else {
            connectFuture = transport.connect(socketAddress);
        }

        try {
            //noinspection unchecked
            Connection<InetSocketAddress> connection = connectFuture.get(timeout, timeUnit);
            return new AbstractGrizzlyXdrTransport<SVC_T>(connection, _replyQueue,_protocolFactory);
        } catch (ExecutionException e) {
            Throwable t = getRootCause(e);
            propagateIfPossible(t, IOException.class);
            throw new IOException(e.toString(), e);
        } catch (TimeoutException | InterruptedException e) {
            throw new IOException(e.toString(), e);
        }
    }

    /**
     * Returns the address of the endpoint this service is bound to,
     * or <code>null</code> if it is not bound yet.
     * @param protocol
     * @return a {@link InetSocketAddress} representing the local endpoint of
     * this service, or <code>null</code> if it is not bound yet.
     */
    public InetSocketAddress getInetSocketAddress(int protocol) {
        Class< ? extends Transport> transportClass = transportFor(protocol);
	return _boundConnections.stream()
		.filter(c -> c.getTransport().getClass() == transportClass)
		.map(Connection::getLocalAddress)
		.findAny()
		.orElse(null);
    }

    /**
     * Get name of this service.
     * @return name of this service.
     */
    public String getName() {
	return _svcName;
    }

    @Override
    public String toString() {
	return _boundConnections.stream()
		.map(Connection::getLocalAddress)
		.map(Object::toString)
		.collect(Collectors.joining(",", getName() +"-[", "]"));
    }

    public abstract SVC_T getThis() ;
}
