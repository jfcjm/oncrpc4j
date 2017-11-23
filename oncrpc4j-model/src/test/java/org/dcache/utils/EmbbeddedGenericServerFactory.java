package org.dcache.utils;

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface EmbbeddedGenericServerFactory<SVC_T extends RpcSvcItf<SVC_T>> {

    public RpcCallItf<SVC_T> createRpcCaller(int prognum, int progver, XdrTransportItf<SVC_T> transport) ;

    public OncRpcSvcBuilderItf<SVC_T> createOncSvcBuilder();

}
