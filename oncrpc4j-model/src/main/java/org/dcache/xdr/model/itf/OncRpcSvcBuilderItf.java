package org.dcache.xdr.model.itf;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;

public interface OncRpcSvcBuilderItf<SVC_T extends RpcSvcItf<SVC_T>> {
    OncRpcSvcBuilderItf<SVC_T> withMaxPort(int maxPort);

    OncRpcSvcBuilderItf<SVC_T> withMinPort(int minPort);

    OncRpcSvcBuilderItf<SVC_T> withPort(int port);

    OncRpcSvcBuilderItf<SVC_T> withSameThreadIoStrategy();

    OncRpcSvcBuilderItf<SVC_T> withSelectorThreadPoolSize(int threadPoolSize);

    <T extends OncRpcSvcBuilderItf<SVC_T>> T  withWorkerThreadIoStrategy();

    OncRpcSvcBuilderItf<SVC_T> withWorkerThreadPoolSize(int threadPoolSize);

    OncRpcSvcBuilderItf<SVC_T> withIoStrategy(IoStrategy ioStrategy);

    OncRpcSvcBuilderItf<SVC_T> withJMX();

    OncRpcSvcBuilderItf<SVC_T> withBacklog(int backlog);

    OncRpcSvcBuilderItf<SVC_T> withBindAddress(String address);

    OncRpcSvcBuilderItf<SVC_T> withServiceName(String serviceName);

    <T extends OncRpcSvcBuilderItf<SVC_T>> T withWorkerThreadExecutionService(ExecutorService executorService);

    <T extends OncRpcSvcBuilderItf<SVC_T>> T withClientMode();

    OncRpcSvcBuilderItf<SVC_T> withRpcService(OncRpcProgram program, RpcDispatchable<SVC_T> service);

    OncRpcSvcBuilderItf<SVC_T> withSubjectPropagation();

    OncRpcSvcBuilderItf<SVC_T> withoutSubjectPropagation();

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

    Map<OncRpcProgram, RpcDispatchable<SVC_T>> getRpcServices();

    SVC_T build();

}