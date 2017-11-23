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

import com.google.common.util.concurrent.MoreExecutors;
import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.glassfish.grizzly.threadpool.FixedThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static org.dcache.xdr.GrizzlyUtils.getWorkerPoolCfg;
import static org.dcache.xdr.IpProtocolType.*;


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
public class OncRpcSvcBuilder <SVC_T extends RpcSvcItf<SVC_T>> implements OncRpcSvcBuilderItf<SVC_T>{

    private int _protocol = 0;
    private int _minPort = 0;
    private int _maxPort = 0;
    private boolean _autoPublish = true;
    private IoStrategy _ioStrategy = IoStrategy.SAME_THREAD;
    private boolean _withJMX = false;
    private int _backlog = 4096;
    private String _bindAddress = "0.0.0.0";
    private String _serviceName = "OncRpcSvc";
    private GssSessionManager _gssSessionManager;
    private ExecutorService _workerThreadExecutionService;
    private boolean _isClient = false;
    private final Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> _programs = new HashMap<>();
    private int _selectorThreadPoolSize = 0;
    private int _workerThreadPoolSize = 0;
    private boolean _subjectPropagation = false;

    public OncRpcSvcBuilder<SVC_T> withAutoPublish() {
        _autoPublish = true;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withoutAutoPublish() {
        _autoPublish = false;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withMaxPort(int maxPort) {
        checkArgument(maxPort >= 0, "Illegal max port value");
        _maxPort = maxPort;
        _minPort = Math.min(_minPort, _maxPort);
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withMinPort(int minPort) {
        checkArgument(minPort >= 0, "Illegal min port value");
        _minPort = minPort;
        _maxPort = Math.max(_minPort, _maxPort);
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withPort(int port) {
        checkArgument(port >= 0, "Illegal port value");
        _minPort = _maxPort = port;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withTCP() {
        _protocol |= TCP;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withUDP() {
        _protocol |= UDP;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withIpProtocolType(int protocolType) {
        _protocol = protocolType;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withSameThreadIoStrategy() {
        _ioStrategy = IoStrategy.SAME_THREAD;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withSelectorThreadPoolSize(int threadPoolSize) {
        checkArgument(threadPoolSize > 0, "thread pool size must be positive");
        _selectorThreadPoolSize = threadPoolSize;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withWorkerThreadIoStrategy() {
        _ioStrategy = IoStrategy.WORKER_THREAD;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withWorkerThreadPoolSize(int threadPoolSize) {
        checkArgument(threadPoolSize > 0, "thread pool size must be positive");
        _workerThreadPoolSize = threadPoolSize;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withIoStrategy(IoStrategy ioStrategy) {
        _ioStrategy = ioStrategy;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withJMX() {
        _withJMX = true;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withBacklog(int backlog) {
        _backlog = backlog;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withBindAddress(String address) {
        _bindAddress = address;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withServiceName(String serviceName) {
        _serviceName = serviceName;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T>withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withWorkerThreadExecutionService(ExecutorService executorService) {
        _workerThreadExecutionService = executorService;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withClientMode() {
        _isClient = true;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withRpcService(OncRpcProgram program, RpcDispatchableItf<SVC_T> service) {
        _programs.put(program, service);
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withSubjectPropagation() {
        _subjectPropagation = true;
        return this;
    }

    public OncRpcSvcBuilder<SVC_T> withoutSubjectPropagation() {
        _subjectPropagation = false;
        return this;
    }

    public boolean getSubjectPropagation() {
        return _subjectPropagation;
    }

    public int getProtocol() {
        return _protocol;
    }

    public int getMinPort() {
        return _minPort;
    }

    public int getMaxPort() {
        return _maxPort;
    }

    public boolean isAutoPublish() {
        return _autoPublish;
    }

    public IoStrategy getIoStrategy() {
        return _ioStrategy;
    }

    public boolean isWithJMX() {
        return _withJMX;
    }

    public int getBacklog() {
        return _backlog;
    }

    public String getBindAddress() {
        return _bindAddress;
    }

    public String getServiceName() {
        return _serviceName;
    }

    public GssSessionManager getGssSessionManager() {
        return _gssSessionManager;
    }

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

    public int getSelectorThreadPoolSize() {
        return _selectorThreadPoolSize;
    }

    public int getWorkerThreadPoolSize() {
        return _workerThreadPoolSize;
    }

    public boolean isClient() {
        return _isClient;
    }

    public Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> getRpcServices() {
        return _programs;
    }

    public RpcSvcItf<SVC_T> build() {

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

        return new OncRpcSvc<SVC_T>(this);
    }
}
