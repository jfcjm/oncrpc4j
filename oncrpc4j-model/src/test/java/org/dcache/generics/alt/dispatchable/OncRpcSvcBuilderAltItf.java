package org.dcache.generics.alt.dispatchable;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.dcache.xdr.IoStrategy;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSessionManagerItf;

public interface OncRpcSvcBuilderAltItf<SVC_T extends RpcSvcAltItf<
                                        SVC_T,CALL_T>,CALL_T extends RpcCallAltItf<SVC_T,
                                        CALL_T>,BUILDER_T extends OncRpcSvcBuilderAltItf<SVC_T,CALL_T,BUILDER_T>> {
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

    BUILDER_T withRpcService(OncRpcProgram program, RpcDispatchableAltItf<SVC_T,CALL_T> service);

    BUILDER_T withRpcSessionManager(RpcSessionManagerAltItf<SVC_T,CALL_T> sessionManager);
    
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
    Map<OncRpcProgram, RpcDispatchableAltItf<SVC_T,CALL_T>> getRpcServices();
    RpcSessionManagerAltItf<SVC_T,CALL_T> getRpcSessionManager();
    SVC_T build();

    

}