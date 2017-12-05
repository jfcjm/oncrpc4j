package org.dcache.utils;

import java.io.IOException;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;

public interface EmbeddedGenericServerFactory<SVC_T extends RpcSvcItf<SVC_T>, BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,BUILDER_T>> {

    public EmbeddedGenericServer<SVC_T,BUILDER_T> createEmbeddedServer(int port) throws IOException ;

}
