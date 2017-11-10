package org.dcache.xdr.model.root;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.model.itf.GenItfOncRpcClient;
import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.dcache.xdr.model.itf.GenItfRpcSvc;
import org.dcache.xdr.model.itf.GenItfXdrTransport;

public abstract class GenAbstractOncRpcClient<SVC_T extends GenItfRpcSvc<SVC_T>>  implements  GenItfOncRpcClient<SVC_T> {

    protected static final String DEFAULT_SERVICE_NAME = null;
    protected final InetSocketAddress _socketAddress;
    protected final GenItfRpcSvc<SVC_T> _rpcsvc;
    
    
    public GenAbstractOncRpcClient(InetAddress address, int protocol, int port) {
        this(new InetSocketAddress(address, port), protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public GenAbstractOncRpcClient(InetAddress address, int protocol, int port, int localPort) {
        this(new InetSocketAddress(address, port), protocol, localPort, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    public GenAbstractOncRpcClient(InetAddress address, int protocol, int port, int localPort, IoStrategy ioStrategy) {
        this(new InetSocketAddress(address, port), protocol, localPort, ioStrategy, DEFAULT_SERVICE_NAME);
    }
    

    public GenAbstractOncRpcClient(InetSocketAddress socketAddress, int protocol) {
        this(socketAddress, protocol, 0, IoStrategy.SAME_THREAD, DEFAULT_SERVICE_NAME);
    }

    
    public GenAbstractOncRpcClient(InetSocketAddress socketAddress, int protocol, int localPort, IoStrategy ioStrategy, String serviceName) {
        _socketAddress = socketAddress;
        _rpcsvc = getOncRpcSvcBuilder()
                .withClientMode()
                .withPort(localPort)
                .withIpProtocolType(protocol)
                .withIoStrategy(ioStrategy)
                .withServiceName(serviceName)
                .build();
    }
    
    



    abstract protected GenItfOncRpcSvcBuilder<SVC_T> getOncRpcSvcBuilder() ;

    @Override
    public GenItfXdrTransport<SVC_T> connect() throws IOException {
        return connect(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public GenItfXdrTransport<SVC_T> connect(long timeout, TimeUnit timeUnit) throws IOException {
        GenItfXdrTransport<SVC_T> t;
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