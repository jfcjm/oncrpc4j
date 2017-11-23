package org.dcache.xdr.model.root;

import static com.google.common.base.Preconditions.checkArgument;
import static org.dcache.xdr.GrizzlyUtils.getWorkerPoolCfg;
import static org.dcache.xdr.IpProtocolType.TCP;
import static org.dcache.xdr.IpProtocolType.UDP;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.dcache.utils.ConversionUtils;
import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.RpcDispatchable;
import org.glassfish.grizzly.threadpool.FixedThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * A builder of {@link OncRpcSvc} instance having any combination of:
 * <ul>
 *   <li>protocol type</li>
 *   <li>min port number</li>
 *   <li>max port number</li>
 *   <li>autopublish</li>
 * </ul>
 *
 * Usage example:
 * <pre>
 *   OncRpcSvc svc = new OncRpcSvcBuilder()
 *     .withMinPort(2400)
 *     .withMaxPort(2500)
 *     .withTCP()
 *     .withUDP()
 *     .withAutoPublish()
 *     .withRpcService(program1, service1)
 *     .withRpcService(program3, service2)
 *     .withWorkerThreadPoolSize(64)
 *     .build();
 * </pre>
 * @since 2.0
 */
public abstract class AbstractOncRpcSvcBuilder<SVC_T extends RpcSvcItf<SVC_T>> implements OncRpcSvcBuilderItf<SVC_T>{

    private int _protocol = 0;
    private int _minPort = 0;
    private int _maxPort = 0;
    private IoStrategy _ioStrategy = IoStrategy.SAME_THREAD;
    private boolean _withJMX = false;
    private int _backlog = 4096;
    private String _bindAddress = "0.0.0.0";
    private String _serviceName = "OncRpcSvc";
    private ExecutorService _workerThreadExecutionService;
    private boolean _isClient = false;
    private final Map<OncRpcProgram, RpcDispatchable<SVC_T>> _programs = new HashMap<>();
    private int _selectorThreadPoolSize = 0;
    private int _workerThreadPoolSize = 0;
    private boolean _subjectPropagation = false;
    
    protected abstract SVC_T getNewOncRpcSvc();
    
    
    
    public AbstractOncRpcSvcBuilder() {
        super();
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withMaxPort(int maxPort) {
        checkArgument(maxPort >= 0, "Illegal max port value");
        _maxPort = maxPort;
        _minPort = Math.min(_minPort, _maxPort);
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withMinPort(int minPort) {
        checkArgument(minPort >= 0, "Illegal min port value");
        _minPort = minPort;
        _maxPort = Math.max(_minPort, _maxPort);
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withPort(int port) {
        checkArgument(port >= 0, "Illegal port value");
        _minPort = _maxPort = port;
        return this;
    }

    
    protected   OncRpcSvcBuilderItf<SVC_T> withTCP() {
        _protocol |= TCP;
        return this;
    }

    
    protected <T extends OncRpcSvcBuilderItf<SVC_T>> T withUDP() {
        _protocol |= UDP;
        return ConversionUtils.helperCAST(this);
    }

   
    protected OncRpcSvcBuilderItf<SVC_T> withIpProtocolType(int protocolType) {
        _protocol = protocolType;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withSameThreadIoStrategy() {
        _ioStrategy = IoStrategy.SAME_THREAD;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withSelectorThreadPoolSize(int threadPoolSize) {
        checkArgument(threadPoolSize > 0, "thread pool size must be positive");
        _selectorThreadPoolSize = threadPoolSize;
        return this;
    }

    @Override
    public <T extends OncRpcSvcBuilderItf<SVC_T>> T withWorkerThreadIoStrategy() {
        _ioStrategy = IoStrategy.WORKER_THREAD;
        return  ConversionUtils.helperCAST(this);
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withWorkerThreadPoolSize(int threadPoolSize) {
        checkArgument(threadPoolSize > 0, "thread pool size must be positive");
        _workerThreadPoolSize = threadPoolSize;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withIoStrategy(IoStrategy ioStrategy) {
        _ioStrategy = ioStrategy;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withJMX() {
        _withJMX = true;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withBacklog(int backlog) {
        _backlog = backlog;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withBindAddress(String address) {
        _bindAddress = address;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withServiceName(String serviceName) {
        _serviceName = serviceName;
        return this;
    }

    @Override
    public <T extends OncRpcSvcBuilderItf<SVC_T>> T withWorkerThreadExecutionService(ExecutorService executorService) {
        _workerThreadExecutionService = executorService;
        return ConversionUtils.helperCAST(this);
    }

    @Override
    public <T extends OncRpcSvcBuilderItf<SVC_T>> T withClientMode() {
        _isClient = true;
        return ConversionUtils.helperCAST(this);
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withRpcService(OncRpcProgram program, RpcDispatchable<SVC_T> service) {
        _programs.put(program, service);
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withSubjectPropagation() {
        _subjectPropagation = true;
        return this;
    }

    @Override
    public OncRpcSvcBuilderItf<SVC_T> withoutSubjectPropagation() {
        _subjectPropagation = false;
        return this;
    }

    @Override
    public boolean getSubjectPropagation() {
        return _subjectPropagation;
    }

    @Override
    public int getProtocol() {
        return _protocol;
    }

    @Override
    public int getMinPort() {
        return _minPort;
    }

    @Override
    public int getMaxPort() {
        return _maxPort;
    }

    @Override
    public IoStrategy getIoStrategy() {
        return _ioStrategy;
    }

    @Override
    public boolean isWithJMX() {
        return _withJMX;
    }

    @Override
    public int getBacklog() {
        return _backlog;
    }

    @Override
    public String getBindAddress() {
        return _bindAddress;
    }

    @Override
    public String getServiceName() {
        return _serviceName;
    }

    @Override
    public ExecutorService getWorkerThreadExecutorService() {
        if (_ioStrategy == IoStrategy.SAME_THREAD ) {
            return MoreExecutors.newDirectExecutorService();
        }
    
        if (_workerThreadExecutionService != null) {
            return _workerThreadExecutionService;
        }
    
        ThreadPoolConfig workerPoolConfig = getWorkerPoolCfg(_ioStrategy,
                _serviceName, _workerThreadPoolSize);
        return new FixedThreadPool(workerPoolConfig);
    }

    @Override
    public int getSelectorThreadPoolSize() {
        return _selectorThreadPoolSize;
    }

    @Override
    public int getWorkerThreadPoolSize() {
        return _workerThreadPoolSize;
    }

    @Override
    public boolean isClient() {
        return _isClient;
    }

    @Override
    public Map<OncRpcProgram, RpcDispatchable<SVC_T>> getRpcServices() {
        return _programs;
    }

    @Override
    public SVC_T build() {
    
        if (_protocol == 0 || (((_protocol & TCP) != TCP) && ((_protocol & UDP) != UDP))) {
            throw new IllegalArgumentException("invalid protocol: " + _protocol);
        }
    
        if (_isClient && (_protocol == (TCP | UDP)) ) {
            throw new IllegalArgumentException("Client mode can't be TCP and UDP at the same time");
        }
    
        if (_isClient && (_maxPort != _minPort)) {
            throw new IllegalArgumentException("Can't use port range in client mode");
        }
    
        if (_workerThreadExecutionService != null && _workerThreadPoolSize > 0) {
            throw new IllegalArgumentException("Can't set worker thread pool size with external execution service");
        }
    
        return getNewOncRpcSvc();
    }

}