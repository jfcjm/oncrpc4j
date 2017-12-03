package org.dcache.xdr.model.itf;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;

public interface OncRpcSvcBuilderItf<SVC_T extends RpcSvcItf<SVC_T>,BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,BUILDER_T>> {
    BUILDER_T withMaxPort(int maxPort);

    BUILDER_T withMinPort(int minPort);

    BUILDER_T withPort(int port);

    BUILDER_T withSameThreadIoStrategy();

    BUILDER_T withSelectorThreadPoolSize(int threadPoolSize);

    BUILDER_T  withWorkerThreadIoStrategy();

    BUILDER_T withWorkerThreadPoolSize(int threadPoolSize);

    BUILDER_T withIoStrategy(IoStrategy ioStrategy);

    BUILDER_T withJMX();

    BUILDER_T withBacklog(int backlog);

    BUILDER_T withBindAddress(String address);

    BUILDER_T withServiceName(String serviceName);

    BUILDER_T  withWorkerThreadExecutionService(ExecutorService executorService);

    BUILDER_T  withClientMode();

    BUILDER_T withRpcService(OncRpcProgram program, RpcDispatchableItf<SVC_T> service);

    BUILDER_T withRpcSessionManager(RpcSessionManagerItf<SVC_T> sessionManager);
    
    BUILDER_T withSubjectPropagation();

    BUILDER_T withoutSubjectPropagation();

    boolean getSubjectPropagation();

    int getProtocol();

    int getMinPort();

    int getMaxPort();

    IoStrategy getIoStrategy();

    boolean isWithJMX();

    int getBacklog();

    String getBindAddress();

    String getServiceName();

    ExecutorService getWorkerThreadExecutorService();

    int getSelectorThreadPoolSize();

    int getWorkerThreadPoolSize();

    boolean isClient();

    Map<OncRpcProgram, RpcDispatchableItf<SVC_T>> getRpcServices();

    SVC_T build();

}