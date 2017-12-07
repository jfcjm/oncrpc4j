package org.dcache.xdr.model.itf;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;
public interface OncRpcSvcBuilderItf
    <
           SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
           CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
           BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T>,
           TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
           REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>> {
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

    BUILDER_T withRpcService(OncRpcProgram program, RpcDispatchableItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> service);

    BUILDER_T withRpcSessionManager(RpcSessionManagerItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> sessionManager);
    
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
    /**
     * Retourne le gestionnaire de session, par défaut devrait retourner un element
     * null dont l'interface est comatibel avec GssSessionManager.
     * et si possible avec un gestonnaire de ssession SASL.
     * @return
     */
    Map<OncRpcProgram, RpcDispatchableItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>> getRpcServices();
    RpcSessionManagerItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> getRpcSessionManager();
    SVC_T build();

    

}