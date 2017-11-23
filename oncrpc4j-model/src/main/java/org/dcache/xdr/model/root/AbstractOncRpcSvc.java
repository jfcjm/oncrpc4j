package org.dcache.xdr.model.root;

import static com.google.common.base.Throwables.getRootCause;
import static org.dcache.xdr.GrizzlyUtils.getSelectorPoolCfg;
import static com.google.common.base.Throwables.propagateIfPossible;
import static org.dcache.xdr.GrizzlyUtils.transportFor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.impl.AbstractGrizzlyXdrTransport;
import org.dcache.xdr.model.impl.AbstractReplyQueue;
import org.dcache.xdr.model.impl.RpcDispatcher;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.ReplyQueueItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.itf.RpcDispatchable;
import org.dcache.xdr.model.itf.RpcProtocolFilterUtf;
import org.glassfish.grizzly.CloseType;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.SocketBinder;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.Filter;
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
public abstract class AbstractOncRpcSvc<SVC_T extends RpcSvcItf<SVC_T>> extends BaseFilter implements RpcSvcItf<SVC_T>{

    private static final Logger _log = LoggerFactory.getLogger(AbstractOncRpcSvc.class);

    protected abstract void addPostTransportProtocolFilters(FilterChainBuilder filterChain, Transport t);
    
    protected abstract RpcProtocolFilterUtf<SVC_T> getRpcProtocolFilter(ReplyQueueItf<SVC_T> replyQueue);
    protected abstract Filter rpcMessageReceiverFor(Transport t);
    abstract protected void addPostRpcProtocolFilter(FilterChainBuilder filterChain) ;
    abstract protected void doBeforeStart() throws IOException;
    abstract protected void doPostCreationServerActions(Connection<InetSocketAddress> connection) throws IOException;

    protected final int _backlog;
    protected final PortRange _portRange;
    protected final String _bindAddress;
    protected final boolean _isClient;
    protected final List<NIOTransport> _transports = new ArrayList<>();
    protected final Set<Connection<InetSocketAddress>> _boundConnections = new HashSet<>();
    protected final ExecutorService _requestExecutor;
    protected final ReplyQueueItf<SVC_T> _replyQueue = new AbstractReplyQueue<>();
    protected final boolean _withSubjectPropagation;
    /**
     * mapping of registered programs.
     */
    protected final Map<OncRpcProgram, RpcDispatchable<SVC_T>> _programs = new ConcurrentHashMap<>();

    abstract protected void doPreStopActions() throws IOException ;
    public  AbstractOncRpcSvc(OncRpcSvcBuilderItf<SVC_T> builder) {
        final int protocol = builder.getProtocol();
        _log.debug("At start Protocol is {}" , protocol);
        if ((protocol & (IpProtocolType.TCP | IpProtocolType.UDP)) == 0) {
            throw new IllegalArgumentException("TCP or UDP protocol have to be defined");
        }

        IoStrategy ioStrategy = builder.getIoStrategy();
        String serviceName = builder.getServiceName();
        ThreadPoolConfig selectorPoolConfig = getSelectorPoolCfg(ioStrategy,
                serviceName,
                builder.getSelectorThreadPoolSize());

        if ((protocol & IpProtocolType.TCP) != 0) {
            _log.info("Using TCP" , protocol);
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
            _log.info("Using UDP" , protocol);
            final UDPNIOTransport udpTransport = UDPNIOTransportBuilder
                    .newInstance()
                    .setReuseAddress(true)
                    .setIOStrategy(SameThreadIOStrategy.getInstance())
                    .setSelectorThreadPoolConfig(selectorPoolConfig)
                    .setSelectorRunnersCount(selectorPoolConfig.getMaxPoolSize())
                    .build();
            _transports.add(udpTransport);
        }
        _isClient = builder.isClient();
        _portRange = builder.getMinPort() > 0 ?
                new PortRange(builder.getMinPort(), builder.getMaxPort()) : null;

        _backlog = builder.getBacklog();
        _bindAddress = builder.getBindAddress();

        if (builder.isWithJMX()) {
            final GrizzlyJmxManager jmxManager = GrizzlyJmxManager.instance();
            for (Transport t : _transports) {
                jmxManager.registerAtRoot(t.getMonitoringConfig().createManagementObject(), t.getName() + "-" + _portRange);
            }
        }
        _requestExecutor = builder.getWorkerThreadExecutorService();
        _programs.putAll(builder.getRpcServices());
        _withSubjectPropagation = builder.getSubjectPropagation();
    }
    
    @Override
    public void start() throws IOException {
        doBeforeStart();
        
        for (Transport t : _transports) {
            _log.info("Init transport; "  + t.getName());
            FilterChainBuilder filterChain = FilterChainBuilder.stateless();
            filterChain.add(new TransportFilter());
            addPostTransportProtocolFilters(filterChain,t);
            filterChain.add(rpcMessageReceiverFor(t));
            filterChain.add(getRpcProtocolFilter(_replyQueue));
            addPostRpcProtocolFilter(filterChain);
            filterChain.add(new RpcDispatcher<SVC_T>(_requestExecutor, _programs, _withSubjectPropagation));
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
                doPostCreationServerActions(connection);
            }
            t.start();

        }
    }
    
    @Override
    public void register(OncRpcProgram prog, RpcDispatchable<SVC_T> handler) {
        _log.info("Registering new program {} : {}", prog, handler);
        _programs.put(prog, handler);
    }

    @Override
    public void unregister(OncRpcProgram prog) {
        _log.info("Unregistering program {}", prog);
        _programs.remove(prog);
    }

    @Override
    @Deprecated
    public void setPrograms(Map<OncRpcProgram, RpcDispatchable<SVC_T>> services) {
        _programs.putAll(services);
    }


    @Override
    public void stop() throws IOException {
        doPreStopActions();
    
        for (Transport t : _transports) {
            t.shutdownNow();
        }
    
        _replyQueue.shutdown();
        _requestExecutor.shutdown();
    }

    @Override
    public void stop(long gracePeriod, TimeUnit timeUnit) throws IOException {
    
    
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

    @Override
    public XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress) throws IOException {
        return connect(socketAddress, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public XdrTransportItf<SVC_T> connect(InetSocketAddress socketAddress, long timeout, TimeUnit timeUnit) throws IOException {
    
        // in client mode only one transport is defined
        NIOTransport transport = _transports.get(0);
    
        Future<Connection> connectFuture;
        _log.debug("Transport of type {} will connect to host {} on port {}",transport.getName(), socketAddress.getAddress(),socketAddress.getPort());
        if (_portRange != null) {
            InetSocketAddress localAddress = new InetSocketAddress(_portRange.getLower());
            connectFuture = transport.connect(socketAddress, localAddress);
        } else {
            connectFuture = transport.connect(socketAddress);
        }
        try {
            //noinspection unchecked
            Connection<InetSocketAddress> connection = connectFuture.get(timeout, timeUnit);
            return getTransportImplementation(connection);
        } catch (ExecutionException e) {
            Throwable t = getRootCause(e);
            propagateIfPossible(t, IOException.class);
            throw new IOException(e.toString(), e);
        } catch (TimeoutException | InterruptedException e) {
            throw new IOException(e.toString(), e);
        }
    }

    protected abstract AbstractGrizzlyXdrTransport<SVC_T> getTransportImplementation(Connection<InetSocketAddress> connection) ;

    @Override
    public InetSocketAddress getInetSocketAddress(int protocol) {
        Class< ? extends Transport> transportClass = transportFor(protocol);
        for (Connection<InetSocketAddress> connection: _boundConnections) {
            if(connection.getTransport().getClass() == transportClass)
                return connection.getLocalAddress();
        }
        return null;
    }

}