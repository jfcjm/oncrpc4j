package org.dcache.xdr.model.root;

import org.dcache.xdr.model.itf.RpcSessionManagerItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.glassfish.grizzly.filterchain.BaseFilter;

public class DefaultSessionManager<SVC_T extends RpcSvcItf<SVC_T>> extends BaseFilter implements RpcSessionManagerItf<SVC_T> {

}
