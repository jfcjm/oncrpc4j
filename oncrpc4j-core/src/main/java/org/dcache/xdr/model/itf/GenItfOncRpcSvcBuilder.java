package org.dcache.xdr.model.itf;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.dcache.xdr.GenOncRpcSvcBuilder;
import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;

public interface GenItfOncRpcSvcBuilder<SVC_T extends GenItfRpcSvc<SVC_T>> {
    GenItfOncRpcSvcBuilder<SVC_T> withMaxPort(int maxPort);

    GenItfOncRpcSvcBuilder<SVC_T> withMinPort(int minPort);

    GenItfOncRpcSvcBuilder<SVC_T> withPort(int port);

    GenItfOncRpcSvcBuilder<SVC_T> withSameThreadIoStrategy();

    GenItfOncRpcSvcBuilder<SVC_T> withSelectorThreadPoolSize(int threadPoolSize);

    GenItfOncRpcSvcBuilder<SVC_T> withWorkerThreadIoStrategy();

    GenItfOncRpcSvcBuilder<SVC_T> withWorkerThreadPoolSize(int threadPoolSize);

    GenItfOncRpcSvcBuilder<SVC_T> withIoStrategy(IoStrategy ioStrategy);

    GenItfOncRpcSvcBuilder<SVC_T> withJMX();

    GenItfOncRpcSvcBuilder<SVC_T> withBacklog(int backlog);

    GenItfOncRpcSvcBuilder<SVC_T> withBindAddress(String address);

    GenItfOncRpcSvcBuilder<SVC_T> withServiceName(String serviceName);

    GenItfOncRpcSvcBuilder<SVC_T> withWorkerThreadExecutionService(ExecutorService executorService);

    GenItfOncRpcSvcBuilder<SVC_T> withClientMode();

    GenItfOncRpcSvcBuilder<SVC_T> withRpcService(OncRpcProgram program, GenRpcDispatchable<SVC_T> service);

    GenItfOncRpcSvcBuilder<SVC_T> withSubjectPropagation();

    GenItfOncRpcSvcBuilder<SVC_T> withoutSubjectPropagation();

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

    Map<OncRpcProgram, GenRpcDispatchable<SVC_T>> getRpcServices();

    SVC_T build();

}