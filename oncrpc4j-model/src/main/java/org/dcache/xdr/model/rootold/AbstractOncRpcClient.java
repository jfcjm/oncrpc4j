package org.dcache.xdr.model.rootold;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.model.itf.OncRpcClientItf;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public abstract class AbstractOncRpcClient<SVC_T extends RpcSvcItf<SVC_T>>  implements  OncRpcClientItf<SVC_T> {

    protected static final String DEFAULT_SERVICE_NAME = null;
    protected final InetSocketAddress _socketAddress;
    protected final RpcSvcItf<SVC_T> _rpcsvc;
    
    
    public AbstractOncRpcClient(InetAddress address, int protocol, int port) {
        this(new InetSocketAddress(address, port), protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public AbstractOncRpcClient(InetAddress address, int protocol, int port, int localPort) {
        this(new InetSocketAddress(address, port), protocol, localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public AbstractOncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }
    

    public AbstractOncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress, protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    
    



    public AbstractOncRpcClient(InetAddress host, int protocol, int port, int localPort, IoStrategy ioStrategy,
            IoStrategy ioStrategy2, String serviceName) {
        this(new InetSocketAddress(host, port), protocol, localPort, ioStrategy, serviceName);
    }
    
    public AbstractOncRpcClient(InetSocketAddress socketAddress, int protocol, int localPort, IoStrategy ioStrategy, String serviceName) {
        _socketAddress = socketAddress;
        _rpcsvc = getRpcSvcBuilder(protocol)
                .withClientMode()
                .withPort(localPort)
                .withIoStrategy(ioStrategy)
                .withServiceName(serviceName)
                .build();
    }

    abstract protected OncRpcSvcBuilderItf<SVC_T> getRpcSvcBuilder(int protocol) ;
    abstract protected OncRpcSvcBuilderItf<SVC_T> getRpcSvcBuilder() ;
    @Override
    public XdrTransportItf<SVC_T> connect() throws IOException {
        return connect(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public XdrTransportItf<SVC_T> connect(long timeout, TimeUnit timeUnit) throws IOException {
        XdrTransportItf<SVC_T> t;
        try {
        _rpcsvc.start();
            t =_rpcsvc.connect(_socketAddress, timeout, timeUnit);
        } catch (IOException e ) {
            _rpcsvc.stop();
            throw e;
        }
        return t;
    }

    @Override
    public void close() throws IOException {
        _rpcsvc.stop();
    }

}